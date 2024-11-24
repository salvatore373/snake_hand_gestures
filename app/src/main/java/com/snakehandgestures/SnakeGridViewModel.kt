package com.snakehandgestures

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SnakeGridViewModel() : ViewModel() {
    val snakeLogic = SnakeLogic(GRID_WIDTH, GRID_HEIGHT)

    // A list storing the content of the grid cells in column-major order
    var gridCells = List<Cell>(GRID_HEIGHT * GRID_WIDTH) { ind ->
        Cell(
            ind / GRID_HEIGHT,
            ind % GRID_WIDTH,
            CellContent.EMPTY
        )
    }

    // The position of the cell containing the prize
    var prizeCell: Cell? = null

    // The current status of the game
    var gameStatus: GameStatus = GameStatus.PLAYING


    fun startGame(difficulty: GameDifficulty) {
        viewModelScope.launch(Dispatchers.IO) {
            snakeLogic.startGame(
                difficulty
            ) { snakeOccupiedCells, newGameStatus, newPrizeCell ->
                {
                    // Update the content of gridCells
                    for (cell in snakeOccupiedCells) {
                        gridCells[cell.y * GRID_WIDTH + cell.x].content = cell.content
                    }

                    prizeCell = newPrizeCell
                    gameStatus = newGameStatus
                }
            }
        }
    }
}