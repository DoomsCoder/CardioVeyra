package com.example.cardioveyra.ui.screens.scan

enum class ScanQualityState(val headline: String, val guidance: String) {
    FACE_LOST("Find your face", "Look at the camera with your forehead and cheeks visible."),
    MULTIPLE_FACES("One face at a time", "Keep other faces out of the camera view."),
    ALIGN_FACE("Center your face", "Face the camera directly and move closer if needed."),
    MOTION_WARNING("Hold still", "Keep your head steady while tracking settles."),
    POOR_LIGHTING("Adjust lighting", "Use even light and avoid strong shadows or glare."),
    OPTIMAL("Face aligned", "Keep your forehead and cheeks visible."),
    PERMISSION_DENIED("Camera access required", "Allow camera access in Android settings to scan."),
    CAMERA_ERROR("Camera unavailable", "Close and reopen the scan to retry.")
}
