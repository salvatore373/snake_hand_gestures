package com.snakehandgestures

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.snakehandgestures.ui.theme.SnakeHandGesturesTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment

enum class GameMode {
    HAND_GESTURES,
    ACCELEROMETER
}

class GameParametersActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SnakeHandGesturesTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = { TopBar() },
                    floatingActionButton = { PlayButton() }
                ) { innerPadding ->
                    GameParametersStack(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    @Composable
    fun PlayButton() {
        val context = LocalContext.current

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val intent = Intent(context, GameActivity::class.java)
                context.startActivity(intent)
            }) {
            Text("Play")
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TopBar() {
        val context = LocalContext.current

        CenterAlignedTopAppBar(
            title = {
                Text(
                    "Choose Game Parameters",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = {
                        val intent = Intent(context, MainActivity::class.java)
                        context.startActivity(intent)
                    }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Go Back to MainActivity"
                    )
                }
            }
        )
    }

    @Composable
    fun GameParametersStack(modifier: Modifier) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            GameParametersSection("Game Mode") {
                GameModeSection()
            }
            GameParametersSection("Difficulty") {
                DifficultySection()
            }
            GameParametersSection("Snake Color") {}
        }
    }


    @Composable
    fun GameParametersSection(title: String, content: @Composable (() -> Unit)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            content()
        }
    }

    @Composable
    fun GameModeSection() {
        var selectedGameMode by rememberSaveable { mutableStateOf(GameMode.HAND_GESTURES) }

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedGameMode == GameMode.HAND_GESTURES,
                    onClick = { selectedGameMode = GameMode.HAND_GESTURES },
                )
                Text("Hand Gestures")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedGameMode == GameMode.ACCELEROMETER,
                    onClick = { selectedGameMode = GameMode.ACCELEROMETER },
                )
                Text("Accelerometer")
            }
        }
    }

    @Composable
    fun DifficultySection() {
        var selectedDifficulty by rememberSaveable { mutableStateOf(GameDifficulty.EASY) }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        )
        {
            FilledTonalButton(
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedDifficulty == GameDifficulty.EASY) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceDim,
                    contentColor = if (selectedDifficulty == GameDifficulty.EASY) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                ),
                onClick = { selectedDifficulty = GameDifficulty.EASY },
            ) {
                Text("Easy")
            }
            FilledTonalButton(
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedDifficulty == GameDifficulty.MEDIUM) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceDim,
                    contentColor = if (selectedDifficulty == GameDifficulty.MEDIUM) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                ),
                onClick = { selectedDifficulty = GameDifficulty.MEDIUM },
            ) {
                Text("Medium")
            }
            FilledTonalButton(
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedDifficulty == GameDifficulty.HARD) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceDim,
                    contentColor = if (selectedDifficulty == GameDifficulty.HARD) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                ),
                onClick = { selectedDifficulty = GameDifficulty.HARD },
            ) {
                Text("Hard")
            }
        }
    }

//    @Composable
//    fun AvatarSection() {
//        var selectedColor by rememberSaveable { mutableStateOf(GameDifficulty.EASY) }
//
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceEvenly
//        )
//        {
//            FilledTonalButton(
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = if (selectedDifficulty == GameDifficulty.EASY) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceDim,
//                    contentColor = if (selectedDifficulty == GameDifficulty.EASY) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
//                ),
//                onClick = { selectedDifficulty = GameDifficulty.EASY },
//            ) {
//                Text("Easy")
//            }
//            FilledTonalButton(
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = if (selectedDifficulty == GameDifficulty.MEDIUM) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceDim,
//                    contentColor = if (selectedDifficulty == GameDifficulty.MEDIUM) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
//                ),
//                onClick = { selectedDifficulty = GameDifficulty.MEDIUM },
//            ) {
//                Text("Medium")
//            }
//            FilledTonalButton(
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = if (selectedDifficulty == GameDifficulty.HARD) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceDim,
//                    contentColor = if (selectedDifficulty == GameDifficulty.HARD) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
//                ),
//                onClick = { selectedDifficulty = GameDifficulty.HARD },
//            ) {
//                Text("Hard")
//            }
//        }
//    }

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview2() {
        SnakeHandGesturesTheme {
            GameModeSection()
        }
    }
}