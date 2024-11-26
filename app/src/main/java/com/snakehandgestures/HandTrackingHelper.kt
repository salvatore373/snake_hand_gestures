package com.snakehandgestures

import android.content.Context
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker.HandLandmarkerOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import android.graphics.Bitmap
import android.os.SystemClock
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.core.BaseOptions
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * MediaPipe Hand Landmarks Index Reference
 *
 * Each hand has 21 landmarks provided by the MediaPipe Hand model.
 * The indices and their corresponding positions are as follows:
 *
 * Index | Landmark        | Finger/Part
 * ------|-----------------|---------------------------
 *  0    | Wrist           | Base of the hand
 *  1    | Thumb CMC       | Lower thumb joint
 *  2    | Thumb MCP       | Thumb middle joint
 *  3    | Thumb IP        | Thumb upper joint
 *  4    | Thumb Tip       | Fingertip of the thumb
 *  5    | Index MCP       | Base of index finger
 *  6    | Index PIP       | Middle joint of index finger
 *  7    | Index DIP       | Upper joint of index finger
 *  8    | Index Tip       | Fingertip of the index finger
 *  9    | Middle MCP      | Base of middle finger
 *  10   | Middle PIP      | Middle joint of middle finger
 *  11   | Middle DIP      | Upper joint of middle finger
 *  12   | Middle Tip      | Fingertip of the middle finger
 *  13   | Ring MCP        | Base of ring finger
 *  14   | Ring PIP        | Middle joint of ring finger
 *  15   | Ring DIP        | Upper joint of ring finger
 *  16   | Ring Tip        | Fingertip of the ring finger
 *  17   | Pinky MCP       | Base of pinky finger
 *  18   | Pinky PIP       | Middle joint of pinky finger
 *  19   | Pinky DIP       | Upper joint of pinky finger
 *  20   | Pinky Tip       | Fingertip of the pinky finger
 *
 * Notes:
 * - MCP: Metacarpophalangeal joint (base of the finger)
 * - PIP: Proximal interphalangeal joint (middle joint of the finger)
 * - DIP: Distal interphalangeal joint (upper joint of the finger)
 * - Tip: Fingertip of each finger
 */


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

fun getDirection(x: Float, y: Float): String {
    return when {
        y >= x && y >= 1 - x -> "Right"
        y >= x && y < 1 - x -> "Top"
        y < x && y < 1 - x -> "Left"
        y < x && y >= 1 - x -> "Bottom"
        else -> "Error" // Never occurs
    }
}

// Euclidean distance between two landmarks
fun distance(point1: NormalizedLandmark, point2: NormalizedLandmark): Double {
    return sqrt((point1.x() - point2.x()).pow(2) + (point1.y() - point2.y()).pow(2) + (point1.z() - point2.z()).pow(2)).toDouble()
}

fun isHandOpen(landmarks: List<NormalizedLandmark>): Boolean {
    val wrist = landmarks[0]
    val fingertips = listOf(landmarks[8], landmarks[12], landmarks[16], landmarks[20])

    // Estimate hand size with the distance between index and pinky
    val handSize = distance(landmarks[6], landmarks[18])

    // Calculate normalized finger spreads
    val normalizedSpreads = fingertips.map { fingertip ->
        distance(wrist, fingertip) / handSize
    }
    val averageSpread = normalizedSpreads.average()

    // Log.d("SPREAD", averageSpread.toString())

    val spreadThreshold = 1.2

    // open if spread is wide enough
    return averageSpread > spreadThreshold
}