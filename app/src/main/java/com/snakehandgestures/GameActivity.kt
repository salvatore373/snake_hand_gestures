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
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.Executors
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.lazy.grid.items
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

const val GRID_WIDTH = 5
const val GRID_HEIGHT = 5

class GameActivity : ComponentActivity() {
    val snakeLogic = SnakeLogic(GRID_WIDTH, GRID_HEIGHT)

    // A list storing the content of the grid cells in column-major order
    var gridCells by mutableStateOf<List<Cell>>(List<Cell>(GRID_HEIGHT * GRID_WIDTH) { ind ->
        Cell(
            ind / GRID_HEIGHT,
            ind % GRID_WIDTH,
            CellContent.EMPTY
        )
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // if needed request permission to use camera
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1)
        } else {
            // Start the game logic coroutine
//            lifecycleScope.launch {
//                var difficulty = GameDifficulty.EASY  // TODO:
//                snakeLogic.startGame(difficulty) { snakeOccupiedCells, newGameStatus, newPrizeCell ->
//                    {
//                        println("callback executed")
//                        // Update the content of gridCells
//                        for (cell in snakeOccupiedCells) {
//                            gridCells[cell.y * GRID_WIDTH + cell.x].content = cell.content
//                        }
//
//                        // TODO: prizeCell = newPrizeCell
//                        // TODO: gameStatus = newGameStatus
//                    }
//                }
//            }
            var snakeGridViewModel = SnakeGridViewModel()
            snakeGridViewModel.startGame(GameDifficulty.EASY)

            setContent {
                GameApp()
            }
        }
    }

    @Composable
    fun GameApp() {
        val context = LocalContext.current
        setupCamera(context)

        // graphics here
        // Surface { SnakeGrid(gridCells) }
    }

    // Returns the grid where the snake will be
    @Composable
    fun SnakeGrid(cells: List<Cell>) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(GRID_WIDTH),
            userScrollEnabled = false,
            modifier = Modifier.size(width = 208.dp, height = 208.dp),
            verticalArrangement = Arrangement.Center,
            horizontalArrangement = Arrangement.Center,
        ) {
            items(
                cells,
                key = { cell -> cell.id() }
            ) { cell ->
                SnakeGridCell(cell)
            }
        }
    }

    @Composable
    fun SnakeGridCell(cell: Cell) {
        var contentText = when (cell.content) {
            CellContent.FILLED_HEAD -> "H"
            CellContent.PRIZE -> "P"
            CellContent.FILLED_BODY -> "."
            else -> ""
        }

        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    color = Color.Gray,
                    shape = RoundedCornerShape(2.dp)
                )
        ) {
            Text(contentText)
        }
    }

    @Preview(showBackground = true, widthDp = 120, heightDp = 120)
    @Composable
    fun GridPreview() {
        Surface {
            SnakeGrid(gridCells)
        }
    }

    private fun setupCamera(context: Context) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        val tracker = HandTrackingHelper(context)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // select front camera
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build()

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) // avoid queueing frames
                .build()

            // analyzer to process each frame
            imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                processImage(tracker, imageProxy)
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this as LifecycleOwner,
                    cameraSelector,
                    imageAnalysis
                )
            } catch (exc: Exception) {
                Log.e("CameraX", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }
}

fun processImage(tracker: HandTrackingHelper, imageProxy: ImageProxy) {
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

        Log.d("POS", "Hand $handIndex Center: x=$centerX, y=$centerY, dir=${getDirection(centerX, centerY)}")
    }

    imageProxy.close()
}

fun getDirection(x: Float, y: Float): String {
    return when {
        y >= x && y >= 1 - x -> "Right"
        y >= x && y < 1 - x -> "Top"
        y < x && y < 1 - x -> "Left"
        y < x && y >= 1 - x -> "Bottom"
        else -> "Error"
    }
}