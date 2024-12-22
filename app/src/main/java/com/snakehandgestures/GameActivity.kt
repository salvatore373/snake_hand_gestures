package com.snakehandgestures

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.Executors
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.snakehandgestures.ui.theme.SnakeHandGesturesTheme

const val GRID_WIDTH = 5
const val GRID_HEIGHT = 5

class GameActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // if needed request permission to use camera
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1)
        } else {
            var snakeViewModel = ViewModelProvider(this).get(SnakeGridViewModel::class.java)
            setContent {
                SnakeHandGesturesTheme {
                    GameplayScreen(snakeGridViewModel = snakeViewModel)
                }
            }

            // Start the game
            snakeViewModel.startGameLogic(GameDifficulty.MEDIUM) // TODO: get difficulty from UI
        }
    }

    @Composable
    fun GameplayScreen(
        snakeGridViewModel: SnakeGridViewModel = viewModel()
    ) {
        val context = LocalContext.current
        setupCamera(context)

        // graphics here
        Surface {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                SnakeGrid(
                    cells = snakeGridViewModel.cells,
                    snakeDirection = snakeGridViewModel.direction
                )
                SnakeCommands(snakeGridViewModel) // DEBUG
                Text(// DEBUG
                    if (snakeGridViewModel.gameStatus == GameStatus.PLAYING) "Playing" else "Game Over",
                    // "Playing",
                    fontSize = 30.sp
                )
                Text( // DEBUG
                    "Score: ${snakeGridViewModel.score}",
                    fontSize = 30.sp
                )
            }
        }
    }

    @Composable
    fun SnakeCommands(
        snakeGridViewModel: SnakeGridViewModel
    ) {
        Row {
            IconButton(onClick = { snakeGridViewModel.changeDirection(SnakeDirection.UP) }) {
                Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Go up")
            }
            IconButton(onClick = { snakeGridViewModel.changeDirection(SnakeDirection.DOWN) }) {
                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Go down")
            }
            IconButton(onClick = { snakeGridViewModel.changeDirection(SnakeDirection.LEFT) }) {
                Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Go left")
            }
            IconButton(onClick = { snakeGridViewModel.changeDirection(SnakeDirection.RIGHT) }) {
                Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Go right")
            }
        }
    }

    // Returns the grid where the snake will be
    @Composable
    fun SnakeGrid(cells: List<SnakeCell>, snakeDirection: SnakeDirection) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(GRID_WIDTH),
            userScrollEnabled = false,
            // modifier = Modifier.border(width = 16.dp, color = MaterialTheme.colorScheme.tertiary)
        ) {
            items(
                cells,
                key = { cell -> cell.id() }
            ) { cell ->
                SnakeGridCell(cell.content, snakeDirection)
            }
        }
    }

    @Composable
    fun SnakeGridCell(cellContent: CellContent, snakeDirection: SnakeDirection) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .aspectRatio(1f)
                .size(24.dp)
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                )
                .border(width = 0.25.dp, color = MaterialTheme.colorScheme.surface)
        ) {
            if (cellContent == CellContent.PRIZE) {
                Image(
                    painter = painterResource(id = R.drawable.egg),
                    contentDescription = "Prize",
                    modifier = Modifier.fillMaxSize(),
                )
            } else if (cellContent == CellContent.FILLED_BODY) {
                Image(
                    painter = painterResource(id = R.drawable.snake_tail_green),
                    contentDescription = "Snake Body",
                    modifier = Modifier.fillMaxSize(),
                )
            } else if (cellContent == CellContent.FILLED_HEAD) {
                var rotationDegrees = when (snakeDirection) {
                    SnakeDirection.UP -> 180f
                    SnakeDirection.DOWN -> 0f
                    SnakeDirection.LEFT -> 90f
                    SnakeDirection.RIGHT -> -90f
                }

                Image(
                    painter = painterResource(id = R.drawable.snake_head_green),
                    contentDescription = "Snake Head",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationZ = rotationDegrees },
                )
            } else {
                Text("", fontSize = 30.sp)
            }

        }
    }

    @Preview(showBackground = true, widthDp = 120, heightDp = 120)
    @Composable
    fun ScreenPreview() {
        SnakeHandGesturesTheme {
            GameplayScreen(snakeGridViewModel = viewModel())
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

        val newDir: String = getDirection(centerX, centerY)
        Log.d("POS", "Hand $handIndex Center: x=$centerX, y=$centerY, dir=${newDir}")

        val isOpen: Boolean = isHandOpen(handLandmarks)
        Log.d("OPEN", isOpen.toString())

        if (!isOpen) {
            // TODO: change direction here
        }
    }

    imageProxy.close()
}