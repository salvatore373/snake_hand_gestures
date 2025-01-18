package com.snakehandgestures

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// import androidx.compose.ui.tooling.preview.Preview
import android.Manifest
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.snakehandgestures.ui.theme.SnakeHandGesturesTheme
import java.util.concurrent.Executors
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import kotlin.math.abs

const val GRID_WIDTH = 5
const val GRID_HEIGHT = 5

private lateinit var snakeViewModel: SnakeGridViewModel

class GameActivity : ComponentActivity(), SensorEventListener {
    var snakeTailSvgPath = R.drawable.snake_tail_green
    var snakeHeadSvgPath = R.drawable.snake_head_green

    // The direction selected by the user as input
    lateinit var selectedDirectionGlob: MutableState<SnakeDirection?>
    lateinit var selectedDifficulty: GameDifficulty
    lateinit var selectedGameMode: GameMode

    // The variables needed to get the sensors data
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null
    private var gravity: FloatArray? = null
    private var geomagnetic: FloatArray? = null
    private var orientationAngles = FloatArray(3)
    private val roll = mutableStateOf(0f)
    private val pitch = mutableStateOf(0f)

    // Authentication variables
    private lateinit var googleSignInManager: GoogleSignInManager
    private var authUserName by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Lock to portrait mode
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        googleSignInManager = GoogleSignInManager(this)
        if (googleSignInManager.isSignedIn()) {
            authUserName = googleSignInManager.getSignedInUserName()
        }

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
        // Get the selected game mode
        val gameModeId = intent.getIntExtra("gameMode", 0)
        selectedGameMode = GameMode.fromId(gameModeId)!!
        // Get selected difficulty
        val speed = intent.getIntExtra("difficulty", GameDifficulty.EASY.speed)
        selectedDifficulty = GameDifficulty.fromSpeed(speed)!!

        if (selectedGameMode == GameMode.ACCELEROMETER) {
            // Initialize SensorManager and sensors
            sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        }

        // if needed request permission to use camera
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
            && selectedGameMode == GameMode.HAND_GESTURES
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1)
        }

        snakeViewModel = ViewModelProvider(this).get(SnakeGridViewModel::class.java)
        setContent {
            SnakeHandGesturesTheme {
                GameplayScreen(snakeGridViewModel = snakeViewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isEndDialogVisible && googleSignInManager.isSignedIn()) {
            authUserName = googleSignInManager.getSignedInUserName()
        }

        // Register sensors
        accelerometer?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
        magnetometer?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        // Unregister sensors to save resources
        if (this::sensorManager.isInitialized) {
            sensorManager.unregisterListener(this)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> gravity = event.values
            Sensor.TYPE_MAGNETIC_FIELD -> geomagnetic = event.values
        }

        if (gravity != null && geomagnetic != null) {
            val rotationMatrix = FloatArray(9)
            if (SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
                // Compute the current roll and pitch values
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                roll.value = Math.toDegrees(orientationAngles[2].toDouble()).toFloat() // Roll angle
                pitch.value =
                    Math.toDegrees(orientationAngles[1].toDouble()).toFloat() // Pitch angle

                // Compute the snake's new direction
                var newDir = if (abs(abs(roll.value) - abs(pitch.value)) < 20f) {
                    // Do not change the snake's direction if it is uncertain
                    selectedDirectionGlob.value ?: SnakeDirection.DOWN
                } else if (abs(roll.value) > abs(pitch.value)) {
                    if (roll.value > 0) SnakeDirection.RIGHT else SnakeDirection.LEFT
                } else {
                    if (pitch.value > 0) SnakeDirection.UP else SnakeDirection.DOWN
                }

                // Change the direction of the snake (if needed)
                if (newDir != selectedDirectionGlob.value) {
                    selectedDirectionGlob.value = newDir
                    snakeViewModel.changeDirection(newDir)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No action needed when accuracy changes
    }

    var isEndDialogVisible by mutableStateOf(false)
    var scoreToSave = 0

    @Composable
    fun GameplayScreen(
        snakeGridViewModel: SnakeGridViewModel = viewModel()
    ) {
        selectedDirectionGlob = remember { mutableStateOf(null) }

        var isStartDialogVisible by remember { mutableStateOf(true) }
        var userName by remember { mutableStateOf("") }

        if (snakeGridViewModel.gameStatus == GameStatus.GAME_OVER) {
            isEndDialogVisible = true
        }

        // TODO: on Cancel in GameOver dialog, get back to GameParametersActivity

        // graphics here
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopBar(
                    title = "Move the Snake",
                    onBackPressed = { onBackPressedDispatcher.onBackPressed() }
                )
            },
        ) { innerPadding ->
            Surface {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (selectedGameMode == GameMode.HAND_GESTURES) {
                        CameraPreview()
                    } else { // GameMode.ACCELEROMETER
                        SensorsPreview()
                    }

                    SnakeGrid(
                        cells = snakeGridViewModel.cells,
                        snakeDirection = snakeGridViewModel.direction
                    )
                    // SnakeCommands(snakeGridViewModel) // DEBUG
//                    Text(
//                        if (snakeGridViewModel.gameStatus == GameStatus.PLAYING) "Playing" else "Game Over",
//                        // "Playing",
//                        fontSize = 30.sp
//                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                "Score:",
                                fontSize = 24.sp
                            )
                            Text(
                                "${snakeGridViewModel.score}",
                                fontSize = 38.sp
                            )
                        }

                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                "Restart:",
                                fontSize = 24.sp
                            )
                            IconButton(
                                onClick = {
                                    snakeGridViewModel.restart()
                                    isStartDialogVisible = true
                                }
                            ) {
                                Icon(
                                    painterResource(R.drawable.restart_icon),
                                    contentDescription = ""
                                )
                            }
                        }
                    }

                    if (isStartDialogVisible) {
                        AlertDialog(
                            onDismissRequest = {
                                isStartDialogVisible = false // Close dialog when dismissed
                                // Start the game
                                snakeViewModel.startGameLogic(selectedDifficulty)
                            },
                            title = {
                                Text(text = "How to play")
                            },
                            text = {
                                Text(text = if (selectedGameMode == GameMode.HAND_GESTURES) "Move your hand in the up section of the rectangle to move the snake up, in the right section to move it right, and so on..." else "Roll or pitch your phone to move the snake!")
                            },
                            confirmButton = {
                            },
                            dismissButton = {
                                Button(onClick = {
                                    isStartDialogVisible = false // Close dialog on dismissal
                                    // Start the game
                                    snakeViewModel.startGameLogic(selectedDifficulty)
                                }) {
                                    Text(text = "Start")
                                }
                            }
                        )
                    }

                    if (isEndDialogVisible) {
                        Dialog(
                            onDismissRequest = {
                                isEndDialogVisible = false // Close dialog when dismissed
                            },
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                shape = RoundedCornerShape(16.dp),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text(
                                        text = "Game Over",
                                        style = MaterialTheme.typography.displaySmall
                                    )
                                    Text(
                                        text = "Your snake died!\nYour score is ${snakeGridViewModel.score}. Press \"Save\" to be in in the Leaderboard.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center
                                    )
                                    OutlinedTextField(
                                        value = authUserName ?: userName,
                                        enabled = authUserName == null,
                                        onValueChange = { userName = it },
                                        label = { Text("Username") }
                                    )

                                    if (!googleSignInManager.isSignedIn()) {
                                        TextButton(
                                            onClick = {
                                                scoreToSave = snakeViewModel.score
                                                googleSignInManager.signIn()
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Or log in with Google")
                                        }
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                    ) {
                                        TextButton(onClick = {
                                            isEndDialogVisible = false // Close dialog on dismissal
                                        }) {
                                            Text(
                                                text = "Cancel",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        TextButton(onClick = {
                                            addScore(
                                                authUserName ?: userName,
                                                snakeGridViewModel.score
                                            )
                                            isEndDialogVisible =
                                                false // Close dialog on confirmation
                                        }) {
                                            Text(
                                                text = "Save",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun CameraPreview() {
        var selectedDirection = selectedDirectionGlob.value

        val lifecycleOwner = LocalLifecycleOwner.current
        Box(
            modifier = Modifier
                .height(250.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
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
                        // Constrain the preview to fit exactly in the Box
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                    previewView.also { previewView ->
                        setupCamera(context)
                    }

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder()
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
            }
        }
    }

    @Composable
    fun SensorsPreview() {
        var selectedDirection = selectedDirectionGlob.value

        Box(
            modifier = Modifier
                .height(250.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
        ) {


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
            modifier = Modifier
                .border(
                    width = 2.dp, color = MaterialTheme.colorScheme.tertiary,
                    shape = RoundedCornerShape(8.dp)
                )
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
                        // Log.v("GameActivity.kt", "Hand Position ${handPosX}, $handPosY")
                    },
                    onHandClosed = { newDir ->
                        if (newDir != selectedDirectionGlob.value) selectedDirectionGlob.value =
                            newDir
                        snakeViewModel.changeDirection(newDir)
                    })
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
        }
    }

    imageProxy.close()
}

