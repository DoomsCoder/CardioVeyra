package com.example.cardioveyra.ui.screens.scan

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.view.View
import android.widget.FrameLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.camera.view.TransformExperimental
import androidx.camera.view.transform.CoordinateTransform
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.cardioveyra.analyzer.FaceAnalyzer
import com.example.cardioveyra.analyzer.FaceFrame
import java.util.concurrent.Executors

@Composable
internal fun CameraXLivePreview(
    modifier: Modifier = Modifier,
    onStateChanged: (ScanQualityState) -> Unit,
    onCapture: (FaceFrame) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val onState by rememberUpdatedState(onStateChanged)
    val onFrame by rememberUpdatedState(onCapture)
    fun cameraGranted() = ContextCompat.checkSelfPermission(
        context, Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    var granted by remember { mutableStateOf(cameraGranted()) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        granted = it
        if (!it) onState(ScanQualityState.PERMISSION_DENIED)
    }
    LaunchedEffect(Unit) {
        if (!granted) launcher.launch(Manifest.permission.CAMERA)
    }
    if (!granted) return

    AndroidView(
        modifier = modifier,
        factory = { CameraFaceView(it, lifecycleOwner, { state -> onState(state) }, { frame -> onFrame(frame) }) },
        onReset = null,
        onRelease = { it.release() }
    )
}

@OptIn(TransformExperimental::class)
private class CameraFaceView(
    context: Context,
    private val owner: LifecycleOwner,
    private val onState: (ScanQualityState) -> Unit,
    private val onCapture: (FaceFrame) -> Unit
) : FrameLayout(context), DefaultLifecycleObserver {
    private val previewView = PreviewView(context).apply {
        scaleType = PreviewView.ScaleType.FILL_CENTER
        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
    }
    private val overlay = FaceOverlay(context, previewView)
    private val executor = Executors.newSingleThreadExecutor()
    private val mainExecutor = ContextCompat.getMainExecutor(context)
    private var provider: ProcessCameraProvider? = null
    private var preview: Preview? = null
    private var analysis: ImageAnalysis? = null
    private var analyzer: FaceAnalyzer? = null
    private var released = false
    private val staleFrame = Runnable {
        overlay.frame = null
        onState(ScanQualityState.FACE_LOST)
    }

    init {
        addView(previewView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        addView(overlay, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        owner.lifecycle.addObserver(this)
        previewView.doOnLayout {
            if (!released) {
                val future = ProcessCameraProvider.getInstance(context)
                future.addListener({
                    if (!released) {
                        try {
                            bindCamera(future.get())
                        } catch (failure: Exception) {
                            android.util.Log.e("CardioVeyraCamera", "Camera binding failed", failure)
                            release()
                            onState(ScanQualityState.CAMERA_ERROR)
                        }
                    }
                }, mainExecutor)
            }
        }
    }

    private fun bindCamera(cameraProvider: ProcessCameraProvider) {
        val viewPort = checkNotNull(previewView.viewPort)
        provider = cameraProvider
        val cameraPreview = Preview.Builder().build().also {
            preview = it
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        val faceAnalyzer = FaceAnalyzer { frame ->
            mainExecutor.execute {
                if (!released && owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    overlay.frame = frame
                    onState(frame.quality)
                    onCapture(frame)
                    removeCallbacks(staleFrame)
                    if (frame.quality != ScanQualityState.CAMERA_ERROR) postDelayed(staleFrame, 650L)
                }
            }
        }.also { analyzer = it }

        val cameraAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build().also {
                analysis = it
                it.setAnalyzer(executor, faceAnalyzer)
            }
        val useCases = UseCaseGroup.Builder()
            .setViewPort(viewPort)
            .addUseCase(cameraPreview)
            .addUseCase(cameraAnalysis)
            .build()
        cameraProvider.bindToLifecycle(owner, CameraSelector.DEFAULT_FRONT_CAMERA, useCases)
    }

    override fun onStop(owner: LifecycleOwner) {
        removeCallbacks(staleFrame)
        overlay.frame = null
        onState(ScanQualityState.FACE_LOST)
    }

    fun release() {
        if (released) return
        released = true
        owner.lifecycle.removeObserver(this)
        removeCallbacks(staleFrame)
        overlay.frame = null
        analysis?.clearAnalyzer()
        provider?.unbind(*listOfNotNull(preview, analysis).toTypedArray())
        analyzer?.close()
        executor.shutdown()
    }
}

@OptIn(TransformExperimental::class)
private class FaceOverlay(context: Context, private val preview: PreviewView) : View(context) {
    var frame: FaceFrame? = null
        set(value) {
            field = value
            invalidate()
        }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = resources.displayMetrics.density * 1.5f
    }
    private val path = Path()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val result = frame ?: return
        val source = result.transform ?: return
        val target = preview.outputTransform ?: return
        val coordinates = CoordinateTransform(source, target)

        result.bounds?.let {
            val bounds = RectF(it)
            coordinates.mapRect(bounds)
            paint.color = Color.argb(200, 240, 246, 252)
            canvas.drawRoundRect(bounds, 10f, 10f, paint)
        }
        for ((region, outline) in result.outlines) {
            val points = outline.copyOf()
            coordinates.mapPoints(points)
            path.rewind()
            path.moveTo(points[0], points[1])
            for (index in 2 until points.size step 2) path.lineTo(points[index], points[index + 1])
            path.close()
            paint.color = if (region in result.rgb) Color.rgb(52, 211, 153) else Color.rgb(245, 158, 11)
            canvas.drawPath(path, paint)
        }
    }
}
