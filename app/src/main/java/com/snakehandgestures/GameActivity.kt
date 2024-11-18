package com.snakehandgestures

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner

class GameActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // if needed request permission to use camera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1)
        } else {
            setContent {
                CameraApp()
            }
        }
    }
}

@Composable
fun CameraApp() {
    val context = LocalContext.current
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

    AndroidView(
        factory = { context ->
            val previewView = PreviewView(context)
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build()
            val imageAnalyzer = ImageAnalysis.Builder().build()
                .apply {
                    setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy: ImageProxy ->
                        processImage(context, imageProxy)
                    }
                }

            val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build()

            cameraProvider.bindToLifecycle(
                context as LifecycleOwner,
                cameraSelector,
                preview,
                imageAnalyzer
            )

            preview.setSurfaceProvider(previewView.surfaceProvider)
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

fun processImage(context: Context, imageProxy: ImageProxy) {
    val tracker = HandTrackingHelper(context)
    val res = tracker.detectHands(imageProxy.toBitmap())
    //Log.d("RES", res?.results[0]?.handednesses())

    res?.results[0]?.landmarks()?.forEachIndexed { handIndex, handLandmarks ->
        var sumX = 0f
        var sumY = 0f
        //var sumZ = 0f

        // find center based on the average of all 21 landmarks
        handLandmarks.toList().forEach { landmark ->
            sumX += landmark.x()
            sumY += landmark.y()
            //sumZ += landmark.z()
        }
        val centerX = sumX / handLandmarks.toList().size
        val centerY = sumY / handLandmarks.toList().size
        //val centerZ = sumZ / handLandmarks.toList().size

        Log.d("POS", "Hand $handIndex Center: x=$centerX, y=$centerY")
    }

    imageProxy.close()
}