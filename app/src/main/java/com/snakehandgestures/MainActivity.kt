package com.snakehandgestures

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.snakehandgestures.ui.theme.SnakeHandGesturesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SnakeHandGesturesTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LandingView(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }

        init()
        getSortedScores { sortedScores ->
            // Quando i dati sono pronti, questa funzione viene chiamata con i risultati
            sortedScores.forEach {
                println("${it.first}: ${it.second}")
            }
        }
    }
}

@Composable
fun LandingView(
    modifier: Modifier = Modifier,
) {

    val context = LocalContext.current
    Box(
        modifier = modifier.fillMaxSize(), // make the Box take the entire screen
        contentAlignment = Alignment.Center // center all elements inside the Box
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.snake_head_green),
                contentDescription = "App Logo",
                modifier = Modifier.size(128.dp),
            )
            Text(
                text = "Welcome to the new generation Snake!"
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Button(
                    onClick = {
                        val intent = Intent(context, GameParametersActivity::class.java)
                        context.startActivity(intent)
                    },
                ) {
                    Text(text = "Play")
                }
                OutlinedButton(
                    onClick = {
                        val intent = Intent(context, LeaderboardActivity::class.java)
                        context.startActivity(intent)
                    },
                ) {
                    Text(text = "Leaderboard")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SnakeHandGesturesTheme {
        LandingView()
    }
}