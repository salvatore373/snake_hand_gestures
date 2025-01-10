package com.snakehandgestures

// import androidx.compose.ui.tooling.preview.Preview
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.snakehandgestures.ui.theme.SnakeHandGesturesTheme
import java.util.concurrent.Executors
import androidx.compose.ui.graphics.Path

const val GRID_WIDTH = 5
const val GRID_HEIGHT = 5

private lateinit var snakeViewModel: SnakeGridViewModel

class GameActivity : ComponentActivity() {
    var snakeTailSvgPath = R.drawable.snake_tail_green
    var snakeHeadSvgPath = R.drawable.snake_head_green

    var handPosXGlob: Float = 0f
    var handPosYGlob: Float = 0f
    var isHandOpenGlob: Boolean = true

    // The direction selected by the user as input
    lateinit var selectedDirectionGlob: MutableState<SnakeDirection?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the selected avatar color
        val avatarColorId = intent.getIntExtra("avatarColor", 0)
        val selectedAvatarColor = AvatarColors.fromId(avatarColorId)!!
        snakeHeadSvgPath = when (selectedAvatarColor) {
            AvatarColors.GREEN -> R.drawable.snake_head_green
            AvatarColors.YELLOW -> R.drawable.snake_head_yellow
            AvatarColors.BROWN -> R.drawable.snake_head_brown
        }
        snakeTailSvgPath = when (selectedAvatarColor) {
            AvatarColors.GREEN -> R.drawable.snake_tail_green
            AvatarColors.YELLOW -> R.drawable.snake_tail_yellow
            AvatarColors.BROWN -> R.drawable.snake_tail_brown
        }
        // Get the selected game mode TODO: use it
        val gameModeId = intent.getIntExtra("gameMode", 0)
        val selectedGameMode = GameMode.fromId(gameModeId)!!
        // Get selected difficulty
        val speed = intent.getIntExtra("difficulty", GameDifficulty.EASY.speed)
        var selectedDifficulty = GameDifficulty.fromSpeed(speed)!!

        // DEBUG
        println("Got: $selectedAvatarColor")
        println("Got: $selectedGameMode")
        println("Got: $selectedDifficulty")

        // if needed request permission to use camera
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1)
        }

        snakeViewModel = ViewModelProvider(this).get(SnakeGridViewModel::class.java)
        setContent {
            SnakeHandGesturesTheme {
                GameplayScreen(snakeGridViewModel = snakeViewModel)
            }
        }

        // Start the game
        snakeViewModel.startGameLogic(selectedDifficulty)

    }

    @Composable
    fun GameplayScreen(
        snakeGridViewModel: SnakeGridViewModel = viewModel()
    ) {
        val context = LocalContext.current
        selectedDirectionGlob = remember{ mutableStateOf(null) }
        // setupCamera(context)

        // graphics here
        Surface {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                CameraPreview()
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
    fun CameraPreview() {
        var handPosX by remember { mutableFloatStateOf(handPosXGlob) }
        var handPosY by remember { mutableFloatStateOf(handPosYGlob) }
        var isHandOpen by remember { mutableStateOf(isHandOpenGlob) }
        // var selectedDirection by remember { mutableStateOf(selectedDirectionGlob) }
        var selectedDirection = selectedDirectionGlob.value

        val lifecycleOwner = LocalLifecycleOwner.current
        Box(
            modifier = Modifier
                .height(250.dp)
                .fillMaxWidth()
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize(),
                factory = { context ->
                    val previewView = PreviewView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        // setBackgroundColor(Color.BLACK)
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        scaleType = PreviewView.ScaleType.FILL_START
                    }
                    previewView.also { previewView ->
                        setupCamera(context)
                    }

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder()
                            // .setTargetAspectRatio(RATIO_16_9)
                            .build()

                        val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                        preview.surfaceProvider = previewView.surfaceProvider

                        try {
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview
                            )
                        } catch (e: Exception) {
                            Log.e("CameraPreview", "Camera binding failed: ${e.message}", e)
                        }
                    }, ContextCompat.getMainExecutor(context))

                    previewView
                }
            )

            // Overlay with Diagonal Lines
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                // Draw the lines on the diagonals
                drawLine(
                    color = Color.Red,
                    start = Offset(0f, 0f),
                    end = Offset(width, height),
                    strokeWidth = 4f
                )
                drawLine(
                    color = Color.Red,
                    start = Offset(0f, height),
                    end = Offset(width, 0f),
                    strokeWidth = 4f
                )

                // Highlight the selected portion of the rectangle
                if (selectedDirection == SnakeDirection.UP) {
                    drawPath(
                        path = Path().apply {
                            moveTo(0f, 0f)
                            lineTo(width, 0f)
                            lineTo(width / 2, height / 2)
                            close()
                        },
                        color = Color.Yellow.copy(alpha = 0.5f)
                    )
                }
                if (selectedDirection == SnakeDirection.DOWN) {
                    drawPath(
                        path = Path().apply {
                            moveTo(0f, height)
                            lineTo(width, height)
                            lineTo(width / 2, height / 2)
                            close()
                        },
                        color = Color.Green.copy(alpha = 0.5f)
                    )
                }
                if (selectedDirection == SnakeDirection.LEFT) {
                    drawPath(
                        path = Path().apply {
                            moveTo(0f, 0f)
                            lineTo(0f, height)
                            lineTo(width / 2, height / 2)
                            close()
                        },
                        color = Color.Red.copy(alpha = 0.5f)
                    )
                }
                if (selectedDirection == SnakeDirection.RIGHT) {
                    drawPath(
                        path = Path().apply {
                            moveTo(width, 0f)
                            lineTo(width, height)
                            lineTo(width / 2, height / 2)
                            close()
                        },
                        color = Color.Blue.copy(alpha = 0.5f)
                    )
                }

                // Draw center dot
                drawCircle(
                    color = Color.Blue,
                    radius = 10f, // Adjust size of the dot
                    center = Offset(handPosX, handPosY)
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
                    painter = painterResource(id = snakeTailSvgPath),
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
                    painter = painterResource(id = snakeHeadSvgPath),
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

            imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                processImage(tracker, imageProxy,
                    onHandFound = { handPosX, handPosY, isHandOpen ->
                        Log.v("GameActivity.kt", "Hand Position ${handPosX}, $handPosY")
                        handPosXGlob = handPosX
                        handPosYGlob = handPosY
                        isHandOpenGlob = isHandOpen
                    },
                    onHandClosed = { newDir ->
                        if(newDir != selectedDirectionGlob.value) selectedDirectionGlob.value = newDir
                        snakeViewModel.changeDirection(newDir) })
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

fun processImage(
    tracker: HandTrackingHelper, imageProxy: ImageProxy,
    onHandFound: (Float, Float, Boolean) -> Unit,
    onHandClosed: (SnakeDirection) -> Unit,
) {
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

        val newDir: SnakeDirection = getDirection(centerX, centerY)
        Log.d("POS", "Hand $handIndex Center: x=$centerX, y=$centerY, dir=${newDir}")

        val isOpen: Boolean = isHandOpen(handLandmarks)
        Log.d("OPEN", isOpen.toString())

        onHandFound(centerX, centerY, isOpen)

        if (!isOpen) {
            onHandClosed(newDir)
            // TODO: snakeViewModel.changeDirection(newDir)
        }
    }

    imageProxy.close()
}

