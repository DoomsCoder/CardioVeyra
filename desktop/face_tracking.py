"""Face geometry, quality gates and RGB sampling for the desktop camera."""

from dataclasses import dataclass, field
from enum import Enum
import math

import numpy as np

MAX_FRAME_GAP_NS = 250_000_000
SETTLE_NS = 300_000_000
_GRID = np.array([(x / 7, y / 7) for y in range(-7, 8) for x in range(-7, 8)
                  if x * x + y * y <= 49])
_ANGLES = np.arange(24) * 2 * np.pi / 24
_CONTOUR = np.column_stack((np.cos(_ANGLES), np.sin(_ANGLES)))


class Quality(Enum):
    FACE_LOST = "Look at the camera"
    MULTIPLE_FACES = "One face at a time"
    ALIGN_FACE = "Center your face and face the camera"
    MOTION_WARNING = "Hold still"
    POOR_LIGHTING = "Use even lighting and avoid glare"
    OPTIMAL = "Face aligned"


@dataclass(frozen=True)
class FaceFrame:
    timestamp_ns: int
    quality: Quality
    bounds: tuple[int, int, int, int] | None = None
    outlines: dict[str, np.ndarray] = field(default_factory=dict)
    rgb: dict[str, tuple[float, float, float]] = field(default_factory=dict)


def sample_regions(frame, anchors):
    """Sample equally spaced points inside inset ellipses on the original RGB image."""
    height, width = frame.shape[:2]
    first_eye, second_eye, forehead_tip, left_cheek, right_cheek = anchors
    eye_line = second_eye - first_eye
    normal = np.array([-eye_line[1], eye_line[0]])
    midpoint = (first_eye + second_eye) * 0.5
    forehead = midpoint + normal * np.clip(
        np.dot(forehead_tip - midpoint, normal) / np.dot(normal, normal) * 0.48, -0.62, -0.40
    )
    regions = (
        ("forehead", forehead, 0.40, 0.19),
        ("left_cheek", left_cheek, 0.20, 0.18),
        ("right_cheek", right_cheek, 0.20, 0.18),
    )
    outlines = {}
    colors = {}
    for name, center, across_scale, down_scale in regions:
        axes = np.stack((eye_line * across_scale, normal * down_scale))
        outline = center + _CONTOUR @ axes
        if np.any(outline < 0) or np.any(outline >= (width, height)):
            continue
        outlines[name] = outline
        points = (center + _GRID @ axes).astype(int)
        # The discrete grid and the displayed contour need independent bounds checks.
        if np.any(points < 0) or np.any(points >= (width, height)):
            continue
        pixels = frame[points[:, 1], points[:, 0]]
        luma = pixels @ np.array([0.299, 0.587, 0.114])
        if luma.mean() < 15 or np.mean((luma < 5) | (luma > 250)) > 0.25:
            continue
        colors[name] = tuple(float(channel) for channel in pixels.mean(axis=0))
    return outlines, colors


class FaceTracker:
    def __init__(self):
        self.reset()

    def reset(self):
        self._previous = None
        self._smoothed = None
        self._last_timestamp = 0
        self._stable_since = 0

    def evaluate(self, frame, faces, transforms, timestamp_ns):
        if not faces and self._previous is not None and timestamp_ns - self._last_timestamp <= 120_000_000:
            # Allow tracking to recover on the next frame without sampling a stale face.
            return FaceFrame(timestamp_ns, Quality.FACE_LOST)
        if len(faces) != 1:
            self.reset()
            quality = Quality.MULTIPLE_FACES if faces else Quality.FACE_LOST
            return FaceFrame(timestamp_ns, quality)

        height, width = frame.shape[:2]
        points = np.array([(point.x * width, point.y * height) for point in faces[0]])
        if len(points) < 468 or not np.isfinite(points).all():
            self.reset()
            return FaceFrame(timestamp_ns, Quality.ALIGN_FACE)

        bounds = (*points.min(axis=0).astype(int), *points.max(axis=0).astype(int))
        first_eye = points[[33, 133]].mean(axis=0)
        second_eye = points[[362, 263]].mean(axis=0)
        eyes = sorted((first_eye, second_eye), key=lambda point: point[0])
        anchors = np.array([*eyes, points[10], points[280], points[50]])
        center = anchors[:2].mean(axis=0)

        within_frame = np.all(np.abs(center - (width / 2, height / 2)) < np.array([width, height]) * 0.35)
        if not within_frame or np.linalg.norm(eyes[1] - eyes[0]) < 40 or not self._frontal(transforms):
            self.reset()
            return FaceFrame(timestamp_ns, Quality.ALIGN_FACE, bounds)

        tracked, stable = self._stabilize(anchors, timestamp_ns)
        outlines, rgb = sample_regions(frame, tracked)
        if not outlines:
            quality = Quality.ALIGN_FACE
        elif not rgb:
            quality = Quality.POOR_LIGHTING
        elif not stable:
            quality = Quality.MOTION_WARNING
        else:
            quality = Quality.OPTIMAL
        return FaceFrame(timestamp_ns, quality, bounds, outlines, rgb if stable else {})

    @staticmethod
    def _frontal(transforms):
        if len(transforms) != 1:
            return False
        matrix = transforms[0]
        forward = matrix[:3, 2]
        yaw = math.degrees(math.atan2(forward[0], abs(forward[2])))
        pitch = math.degrees(math.atan2(forward[1], math.hypot(forward[0], forward[2])))
        roll = math.degrees(math.atan2(matrix[1, 0], matrix[0, 0]))
        return abs(yaw) <= 25 and abs(pitch) <= 25 and abs(roll) <= 22

    def _stabilize(self, anchors, timestamp_ns):
        dt = timestamp_ns - self._last_timestamp
        last = self._previous
        restart = last is None or dt <= 0 or dt > MAX_FRAME_GAP_NS
        if not restart:
            old_axis = last[1] - last[0]
            scale = np.linalg.norm(old_axis)
            new_axis = anchors[1] - anchors[0]
            translation = np.linalg.norm((anchors[:2] - last[:2]).mean(axis=0)) / scale
            restart = (translation > 0.11
                       or abs(np.linalg.norm(new_axis) / scale - 1) > 0.14
                       or np.linalg.norm(new_axis - old_axis) / scale > 0.15)

        if restart:
            self._stable_since = timestamp_ns
            self._smoothed = anchors
        else:
            alpha = -math.expm1(-dt / 85_000_000)
            self._smoothed = self._smoothed + alpha * (anchors - self._smoothed)

        self._last_timestamp = timestamp_ns
        self._previous = anchors
        return self._smoothed, timestamp_ns - self._stable_since >= SETTLE_NS
