package com.snakehandgestures

import android.content.Context
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker.HandLandmarkerOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import android.graphics.Bitmap
import android.os.SystemClock
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions

class HandTrackingHelper(context: Context) {
    // !!!!!!!!!!!
    // vedi https://github.com/google-ai-edge/mediapipe-samples/blob/main/examples/hand_landmarker/android/app/src/main/java/com/google/mediapipe/examples/handlandmarker/HandLandmarkerHelper.kt

    private var handLandmarker: HandLandmarker? = null

    init {
        val baseOptionBuilder = BaseOptions.builder()
        baseOptionBuilder.setModelAssetPath("hand_landmarker.task")
        val baseOptions = baseOptionBuilder.build()

        val options = HandLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            //.setMinHandDetectionConfidence(minHandDetectionConfidence)
            //.setMinTrackingConfidence(minHandTrackingConfidence)
            //.setMinHandPresenceConfidence(minHandPresenceConfidence)
            .setRunningMode(RunningMode.IMAGE)
            .setNumHands(1)
            .build()
        handLandmarker = HandLandmarker.createFromOptions(context, options)
    }

    fun detectHands(bitmap: Bitmap): ResultBundle? {
        val mpImage = BitmapImageBuilder(bitmap).build()
        val startTime = SystemClock.uptimeMillis()

        handLandmarker?.detect(mpImage)?.also { landmarkResult ->
            val inferenceTimeMs = SystemClock.uptimeMillis() - startTime
            return ResultBundle(
                listOf(landmarkResult),
                inferenceTimeMs,
                bitmap.height,
                bitmap.width
            )
        }

        // error
        return null
    }

    fun close() {
        handLandmarker?.close()
    }

    data class ResultBundle(
        val results: List<HandLandmarkerResult>,
        val inferenceTime: Long,
        val inputImageHeight: Int,
        val inputImageWidth: Int,
    )
}