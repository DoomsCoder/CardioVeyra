# CardioVeyra implementation plan

## Scope

Build a local webcam scan on PC and a native camera scan on Android. Both use three regions of interest (ROIs): the forehead, left cheek and right cheek. Deliver face positioning, scan progress, a live rPPG waveform, an estimated heart rate and a post-scan result. This milestone uses a pre-trained face detector and classical pulse-peak detection. The synopsis's trained TFLite peak detector is deferred.

| Platform | Planned implementation |
| --- | --- |
| Android | Reuse CameraX, ML Kit, Kotlin and Compose. Extend the existing analyzer, signal buffer and scan screens. |
| PC | Build a Windows-first local Python webcam app under `desktop/` using OpenCV for capture and MediaPipe Face Landmarker for tracking. Show the preview, scan state, waveform and result in the desktop app. Use the Python signal-processing implementation as the reference for Kotlin. |

Use the same region definitions, scan rules and signal fixtures across platforms. Detector-specific landmark indices and coordinate transforms stay inside each platform's capture layer. Package or provision the pre-trained detector assets before offline testing.

## 1. Detect one face and track the forehead and both cheeks

- **Android** — Extend `FaceAnalyzer.kt` to define three inset skin regions using ML Kit facial features and face geometry. Preserve the current CameraX preview. Verify the model is available on a fresh offline install and use bundled delivery if needed.
- **PC** — Open the webcam locally and map MediaPipe landmarks to the same three anatomical areas. Handle missing cameras, denied access and webcam disconnection with a clear error.
- Keep the forehead between the eyebrows and hairline. Place each cheek below the eye and outside the nose and mouth. Reject out-of-frame regions, regions with insufficient visible skin and extreme head pose. Smooth ROI positions while tracking the same face and reset smoothing on face loss, subject change or multiple faces.
- Add a debug overlay showing the face and all three ROIs. Map analysis coordinates to the displayed preview, accounting for Android rotation, front-camera mirroring and preview crop. Verify PC preview mirroring independently. Average RGB per ROI in the source frame.
- **Acceptance** — On a physical Android phone and PC webcam, check face entry and exit, slight head turns, partial occlusion, multiple faces and offline startup. The overlay stays over skin on each usable region and sample collection pauses when a single face cannot be tracked reliably.

## 2. Drive scan progress from usable capture

- Timestamp each frame with a monotonic capture time. Store one sample record containing its timestamp, the available per-ROI RGB means and quality flags. Extend the Android `SignalBuffer.kt` and provide an equivalent buffer on PC.
- Track face alignment, motion, lighting, clipped pixels and timing gaps. Require one clearly tracked face and at least one usable ROI. Track each region independently so an occluded cheek can be excluded while the other areas remain usable.
- Use explicit scan states for alignment, collection, pause, processing and result. Start with a 30-second usable-capture target and a 60-second wall-clock timeout. Pause progress on invalid frames. End the active signal segment at a significant gap, discard incomplete analysis windows and resume with a new segment. A completed scan still needs sufficient accepted signal windows.
- Keep frame analysis off Android's UI thread and throttle waveform updates on both platforms. Release the camera and clear in-memory samples on cancel, completion or exit.
- **Acceptance** — Cover a cheek, cover all usable regions, move out of frame and interrupt the camera. Check that valid regions can continue contributing, invalid capture pauses progress, and no analyzed window spans an interruption.

## 3. Produce a live waveform and estimated heart rate

- Build the Python reference first and port it to Kotlin using shared timestamped RGB fixtures. Resample contiguous windows to a uniform time grid. Start at 30 Hz where effective capture supports it and flag sustained low capture rates. Start with 10–15 seconds of usable data per analysis window.
- Run POS independently on each usable ROI, then apply a Butterworth bandpass starting around 0.7–4 Hz. Use sliding windows with a fixed processing stride. Handle near-constant channels and filter startup transients before accepting a waveform.
- Score each ROI by usable coverage, pulse-band signal strength and beat consistency. Select the highest-quality region for each analysis segment as the initial strategy. Treat ROI switches as segment boundaries for beat intervals. Add CHROM as an optional benchmark after POS works on both platforms.
- Use classical peak detection with minimum spacing and an adequate number of valid intervals. Derive estimated BPM from the accepted intervals. Show the processed waveform and replace Android's fixed `72` with the measured estimate. Show a collecting or low-quality state when a window fails.
- **Acceptance** — Confirm expected BPM and rejection behavior using synthetic fixtures. Compare simultaneous resting scans on each platform against a pulse oximeter in normal indoor lighting. Report BPM absolute error, valid scans and total attempts.

## 4. Show the result of the completed scan

- Create a session result containing estimated BPM, elapsed duration, usable capture duration and signal quality. Calculate final BPM from accepted intervals within segments. Count overlapping detections once and exclude intervals spanning capture gaps or ROI changes.
- Connect the Android scan and details screens to the current session data. On PC, present the same fields in the result view. Show a specific rescan prompt if usable data is insufficient. Keep raw frames and RGB traces in memory during the scan.
- Compute SDNN and RMSSD as evaluation outputs only. Validate beat timing against a beat-level reference before showing variability to users.
- **Acceptance** — Complete two scans with different outcomes on each platform. Confirm that a passing scan shows its own result, an interrupted or low-quality scan offers a rescan, and cancel or restart clears the previous result.

## Delivery order

1. Android and PC previews with verified forehead and cheek overlays.
2. Timestamped ROI samples and quality-aware progress on both platforms.
3. Matching Python and Kotlin signal fixtures, followed by live waveform and BPM.
4. Real post-scan results, physical-device checks and a small paired HR comparison.
