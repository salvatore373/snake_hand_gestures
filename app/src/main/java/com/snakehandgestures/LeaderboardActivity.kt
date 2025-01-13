package com.snakehandgestures

import android.os.Bundle
import android.provider.CalendarContract.Colors
import android.view.Surface
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.ktx.Firebase
import com.snakehandgestures.ui.theme.SnakeHandGesturesTheme
import me.onebone.toolbar.CollapsingToolbarScaffold
import me.onebone.toolbar.ScrollStrategy
import me.onebone.toolbar.rememberCollapsingToolbarScaffoldState

class LeaderboardActivity : ComponentActivity() {
    val possibleIcons = listOf<Int>(
        R.drawable.snake_head_green,
        R.drawable.snake_head_yellow,
        R.drawable.snake_head_brown
    )
    val playerEntryCornerDim = 24.dp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SnakeHandGesturesTheme {
                CollapsingToolbarScaffold(
                    state = rememberCollapsingToolbarScaffoldState(),
                    scrollStrategy = ScrollStrategy.ExitUntilCollapsed,
                    modifier = Modifier.fillMaxSize(),
                    toolbar = {
                        TopBar(
                            title = "Leaderboard",
                            barColor = Color(0xFFF9CA86),
                            onBackPressed = {
                                onBackPressedDispatcher.onBackPressed()
                            })
                        Image(
                            painter = painterResource(id = R.drawable.leaderboard_art),
                            contentDescription = "Art",
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .fillMaxWidth()
                                .parallax(ratio = 0.06f)
                                .padding(top = 56.dp),
                        )
                    })
                {
                    Scoreboard()
                }
            }
        }
    }


    @Composable
    fun PlayerEntry(entry: Pair<String, Int>, playerPosition: Int) {
        Box(
            modifier = Modifier
                // .fillMaxWidth()
                .padding(8.dp) // Outer padding around the item
                .clip(RoundedCornerShape(playerEntryCornerDim)) // Rounded corners
                .border(
                    width = 1.dp,
                    color = Color.Gray, // Border color
                    shape = RoundedCornerShape(playerEntryCornerDim)
                )
                .background(color = MaterialTheme.colorScheme.surfaceVariant) // Background color of the item
                .padding(8.dp), // Inner padding inside the item
        ) {
            ListItem(
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                headlineContent = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(  // Position
                            playerPosition.toString(),
                            modifier = Modifier.padding(end = 16.dp),
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            // Name
                            entry.first,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                leadingContent = {  // Avatar icon
                    Image(
                        painter = painterResource(id = possibleIcons.random()),
                        contentDescription = "Avatar",
                        modifier = Modifier.width(48.dp)
                    )
                },
                trailingContent = {  // Score
                    Text(
                        entry.second.toString(),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            )
        }
    }

    @Composable
    fun Scoreboard() {
        // Define state variables
        var isLoading by remember { mutableStateOf(true) }
        var players by remember { mutableStateOf<List<Pair<String, Int>>>(emptyList()) }
        // DEBUG var players = List(20) { Pair("Player $it", 100 - it) }

        // Fetch data asynchronously
        LaunchedEffect(Unit) {
            // Retrieve the leaderboard
            getSortedScores { res ->
                players = res
            }

            // Once data is fetched, set loading to false
            isLoading = false
        }

        Box(modifier = Modifier.fillMaxSize()) {
            // Show loading indicator when data is loading
            if (isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Text("Loading the players' scores...")
                }
            }

            // Show the LazyColumn when data is loaded
            LazyColumn(
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(36.dp, 36.dp, 0.dp, 0.dp))
            ) {
                itemsIndexed(players) { position, entry ->
                    PlayerEntry(entry, position + 1)
                }
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun ScoreboardPreview() {
        SnakeHandGesturesTheme {
            Scoreboard()
        }
    }
}