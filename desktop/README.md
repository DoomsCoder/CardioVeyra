# PC face tracking

Set up the desktop app once from the repository root in PowerShell. The pre-trained model is downloaded during setup. Webcam frames are analyzed locally with no application upload or recording.

```powershell
python -m venv desktop/.venv
& ./desktop/.venv/Scripts/python.exe -m pip install -r desktop/requirements.txt
New-Item -ItemType Directory -Force desktop/models
Invoke-WebRequest -Uri https://storage.googleapis.com/mediapipe-models/face_landmarker/face_landmarker/float16/1/face_landmarker.task -OutFile desktop/models/face_landmarker.task
& ./desktop/.venv/Scripts/python.exe desktop/webcam.py
```

Press **Q** or **Esc** to exit. Pass `--camera 1` if another webcam is available. Use `--model` to point to an existing local model. MediaPipe includes the OpenCV dependency used by the webcam preview.

The default preview stays live until you close it or press **Q**. The waveform and estimated HR appear after about ten seconds of usable capture. The HRV estimate (PRV) shows RMSSD and SDNN once there are 30 seconds of clean pulse data with sufficiently regular beat timing. Estimates clear when the current signal can no longer support them.

Add `--timed` for a scan that targets 30 seconds of usable capture and ends after at most 60 seconds with a result or rescan prompt. Press **R** from that summary to start again.

From `D:\CardioVeyra`, enter the project directory with `cd .\CardioVeyra` before using the commands above. Run the desktop regression tests from that project directory with `& ./desktop/.venv/Scripts/python.exe -B -m unittest discover -s desktop -v`.
