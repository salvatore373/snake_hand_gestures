package com.snakehandgestures

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

enum class GameMode {
    HAND_GESTURES,
    ACCELEROMETER
}

enum class AvatarColors {
    GREEN,
    YELLOW,
    BROWN
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
                    GameParametersStack(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }

    @Composable
    fun PlayButton() {
        val context = LocalContext.current

        ExtendedFloatingActionButton(
            onClick = {
                val intent = Intent(context, GameActivity::class.java)
                context.startActivity(intent)
            },
            icon = { Icon(Icons.Filled.PlayArrow, "Play button") },
            text = { Text("Play") },
        )
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
            GameParametersSection("Snake Color") {
                AvatarSection()
            }
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
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.tertiary,
                    ),
                    selected = selectedGameMode == GameMode.HAND_GESTURES,
                    onClick = { selectedGameMode = GameMode.HAND_GESTURES },
                )
                Text("Hand Gestures")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.tertiary,
                    ),
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
                    containerColor = if (selectedDifficulty == GameDifficulty.EASY) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceDim,
                    contentColor = if (selectedDifficulty == GameDifficulty.EASY) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurface
                ),
                onClick = { selectedDifficulty = GameDifficulty.EASY },
            ) {
                Text("Easy")
            }
            FilledTonalButton(
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedDifficulty == GameDifficulty.MEDIUM) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceDim,
                    contentColor = if (selectedDifficulty == GameDifficulty.MEDIUM) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurface
                ),
                onClick = { selectedDifficulty = GameDifficulty.MEDIUM },
            ) {
                Text("Medium")
            }
            FilledTonalButton(
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedDifficulty == GameDifficulty.HARD) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceDim,
                    contentColor = if (selectedDifficulty == GameDifficulty.HARD) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurface
                ),
                onClick = { selectedDifficulty = GameDifficulty.HARD },
            ) {
                Text("Hard")
            }
        }
    }

    @Composable
    fun AvatarSection() {
        var selectedColor by rememberSaveable { mutableStateOf(AvatarColors.GREEN) }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        )
        {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .border(4.dp,
                        if (selectedColor== AvatarColors.GREEN) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceDim,
                        CircleShape)
                    .padding(8.dp)
                    .clip(CircleShape)
                    .clickable { selectedColor = AvatarColors.GREEN },
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.snake_head_green),
                    contentDescription = "Green Avatar",
                    modifier = Modifier.fillMaxSize()
                )
            }
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .border(4.dp,
                        if (selectedColor== AvatarColors.YELLOW) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceDim,
                        CircleShape)
                    .padding(8.dp)
                    .clip(CircleShape)
                    .clickable { selectedColor = AvatarColors.YELLOW },
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.snake_head_green),
                    contentDescription = "Yellow Avatar",
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .border(4.dp,
                        if (selectedColor== AvatarColors.BROWN) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceDim,
                        CircleShape)
                    .padding(8.dp)
                    .clip(CircleShape)
                    .clickable { selectedColor = AvatarColors.BROWN },
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.snake_head_green),
                    contentDescription = "Brown Avatar",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}