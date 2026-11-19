"""Run the CardioVeyra PC webcam preview."""

import argparse
from pathlib import Path
import sys
import time

import cv2
import mediapipe as mp
import numpy as np

from face_tracking import FaceTracker, Quality
from pulse_scan import PulseScan

DEFAULT_MODEL = Path(__file__).resolve().parent / "models" / "face_landmarker.task"
WINDOW = "CardioVeyra"
ACTIVE = (119, 213, 52)
PAUSED = (11, 158, 245)


def create_landmarker(model):
    return mp.tasks.vision.FaceLandmarker.create_from_options(
        mp.tasks.vision.FaceLandmarkerOptions(
            base_options=mp.tasks.BaseOptions(model_asset_path=str(model)),
            running_mode=mp.tasks.vision.RunningMode.VIDEO,
            num_faces=2,
            min_face_detection_confidence=0.50,
            min_face_presence_confidence=0.55,
            min_tracking_confidence=0.55,
            output_facial_transformation_matrixes=True,
        )
    )


_LOW_LIGHT_LUTS = {
    gamma: np.rint(255 * (np.arange(256) / 255) ** gamma).astype(np.uint8)
    for gamma in (0.60, 0.78)
}


def detector_image(rgb):
    height, width = rgb.shape[:2]
    center = rgb[height // 4:3 * height // 4:8, width // 4:3 * width // 4:8]
    if not center.size:
        return rgb
    luma = center @ np.array([0.299, 0.587, 0.114])
    level = np.percentile(luma, 70)
    if level < 8 or level >= 90:
        return rgb
    return cv2.LUT(rgb, _LOW_LIGHT_LUTS[0.60 if level < 50 else 0.78])


def analyze(landmarker, tracker, rgb, timestamp_ns, detector_timestamp_ms):
    image = mp.Image(image_format=mp.ImageFormat.SRGB, data=detector_image(rgb))
    result = landmarker.detect_for_video(image, detector_timestamp_ms)
    return tracker.evaluate(
        rgb, result.face_landmarks, result.facial_transformation_matrixes, timestamp_ns
    )


def draw_preview(frame, result):
    preview = cv2.flip(frame, 1)
    right_edge = frame.shape[1] - 1
    if result.bounds:
        x1, y1, x2, y2 = result.bounds
        cv2.rectangle(preview, (right_edge - x2, y1), (right_edge - x1, y2), (230, 230, 230), 1)
    for region, outline in result.outlines.items():
        points = np.rint(outline).astype(np.int32)
        points[:, 0] = right_edge - points[:, 0]
        color = ACTIVE if region in result.rgb else PAUSED
        cv2.polylines(preview, [points], True, color, 2, cv2.LINE_AA)
    cv2.rectangle(preview, (0, 0), (right_edge, 40), (24, 24, 24), -1)
    cv2.putText(preview, result.quality.value, (12, 27), cv2.FONT_HERSHEY_SIMPLEX,
                0.62, ACTIVE if result.rgb else (235, 235, 235), 1, cv2.LINE_AA)
    return preview


def draw_scan(frame, scan, summary):
    width = max(frame.shape[1], 480)
    if frame.shape[1] != width:
        frame = cv2.resize(frame, (width, frame.shape[0]))
    panel = np.full((150, width, 3), 24, dtype=np.uint8)
    if summary:
        title = f"Estimated pulse: {summary.bpm} BPM" if summary.bpm else "Signal too weak. Rescan."
        details = f"Duration: {summary.elapsed_seconds}s    Signal quality: {summary.quality}"
        lines = (title, details, "Press R to rescan or Q to exit.")
        for index, line in enumerate(lines):
            cv2.putText(panel, line, (12, 32 + index * 35), cv2.FONT_HERSHEY_SIMPLEX,
                        0.57, (235, 235, 235), 1, cv2.LINE_AA)
    else:
        live = scan.live
        title = f"Estimated HR: {live.bpm} BPM" if live else "Waiting for a stable heart rate signal"
        if scan.timed:
            title += f"  |  {round(scan.progress * 100)}%"
        cv2.putText(panel, title, (12, 28), cv2.FONT_HERSHEY_SIMPLEX,
                    0.55, (235, 235, 235), 1, cv2.LINE_AA)
        variability = scan.variability
        if not scan.timed:
            note = "HRV estimate (PRV): waiting for 30 s of clean capture"
            if variability:
                note = f"HRV estimate (PRV): RMSSD {variability.rmssd_ms:.0f} ms  |  SDNN {variability.sdnn_ms:.0f} ms"
            cv2.putText(panel, note, (12, 55), cv2.FONT_HERSHEY_SIMPLEX,
                        0.46, (205, 205, 205), 1, cv2.LINE_AA)
        else:
            cv2.rectangle(panel, (12, 42), (width - 12, 47), (65, 65, 65), -1)
        if scan.timed and scan.progress:
            cv2.rectangle(panel, (12, 42), (12 + round((width - 24) * scan.progress), 47), ACTIVE, -1)
        if live:
            points = np.column_stack((np.linspace(12, width - 12, len(live.waveform)),
                                      105 - live.waveform * 38)).astype(np.int32)
            cv2.polylines(panel, [points], False, ACTIVE, 2, cv2.LINE_AA)
    return np.vstack((frame, panel))


def run(camera_index, model, *, timed=False):
    if not model.is_file():
        raise RuntimeError(f"Face Landmarker model missing at {model}. See desktop/README.md.")

    camera = cv2.VideoCapture(camera_index)
    try:
        if not camera.isOpened():
            raise RuntimeError(f"Unable to open webcam {camera_index}. Check Windows camera access and camera index.")
        camera.set(cv2.CAP_PROP_FRAME_WIDTH, 640)
        camera.set(cv2.CAP_PROP_FRAME_HEIGHT, 480)
        camera.set(cv2.CAP_PROP_BUFFERSIZE, 1)

        tracker = FaceTracker()
        last_ms = -1
        scan = PulseScan(timed=timed)
        started = time.monotonic_ns()
        summary = None
        preview = None
        with create_landmarker(model) as landmarker:
            while True:
                if summary is None:
                    success, frame = camera.read()
                    timestamp = time.monotonic_ns()
                    if not success or frame is None:
                        raise RuntimeError("The webcam stopped returning frames.")
                    last_ms = max(last_ms + 1, timestamp // 1_000_000)
                    rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
                    result = analyze(landmarker, tracker, rgb, timestamp, last_ms)
                    scan.add(timestamp, result.rgb, missed_detection=result.quality is Quality.FACE_LOST)
                    if timed:
                        summary = scan.result((timestamp - started) // 1_000_000_000)
                    preview = draw_preview(frame, result)
                    if summary is not None:
                        camera.release()
                        preview = np.full_like(frame, 24)
                cv2.imshow(WINDOW, draw_scan(preview, scan, summary))
                key = cv2.waitKey(16 if summary else 1) & 0xFF
                if key in (27, ord("q")):
                    break
                if key == ord("r") and summary is not None:
                    if not camera.open(camera_index):
                        raise RuntimeError(f"Unable to reopen webcam {camera_index}.")
                    camera.set(cv2.CAP_PROP_FRAME_WIDTH, 640)
                    camera.set(cv2.CAP_PROP_FRAME_HEIGHT, 480)
                    camera.set(cv2.CAP_PROP_BUFFERSIZE, 1)
                    tracker.reset()
                    scan = PulseScan(timed=timed)
                    started = time.monotonic_ns()
                    summary = None
                if cv2.getWindowProperty(WINDOW, cv2.WND_PROP_VISIBLE) < 1:
                    break
    finally:
        camera.release()
        cv2.destroyAllWindows()


def main():
    parser = argparse.ArgumentParser(description="Run a local pulse scan on a PC webcam.")
    parser.add_argument("--camera", type=int, default=0, help="OpenCV webcam index")
    parser.add_argument("--model", type=Path, default=DEFAULT_MODEL, help="Path to the locally provisioned model")
    parser.add_argument('--timed', action='store_true', help='End with the original timed scan summary')
    args = parser.parse_args()
    try:
        run(args.camera, args.model, timed=args.timed)
    except (OSError, RuntimeError, ValueError, cv2.error) as error:
        print(f"CardioVeyra: {error}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
