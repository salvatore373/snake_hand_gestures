package com.snakehandgestures

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


class SnakeCell(
    val x: Int, val y: Int,
) {
    var content by mutableStateOf(CellContent.EMPTY)

    fun id(): String {
        return "${x}_${y}"
    }
}

class SnakeGridViewModel() : ViewModel() {
    private val _snakeLogic = SnakeLogic(GRID_WIDTH, GRID_HEIGHT)

    // Initialize the list containing the data about all the cells of the grid
    // (in column-major order).
    private val _cells = List(GRID_HEIGHT * GRID_WIDTH) {
        SnakeCell(
            x = it % GRID_HEIGHT, y = it / GRID_WIDTH,
        )
    }.toMutableStateList()
    val cells: List<SnakeCell>
        get() = _cells

    // Function to start the periodic task
    // fun startGameLogic() {
    init {
        viewModelScope.launch {
            _snakeLogic.startGame(GameDifficulty.EASY) // TODO get difficulty from UI
            { snakeCells, gameStatus, prizeCell -> {
                    1 / 0
                    println("hello")
                    // Update the cells in _cells that changed their content
                    for (snakeCell in snakeCells) {
                        var uiCellInd = snakeCell.x * GRID_HEIGHT + snakeCell.y
                        if (snakeCell.content != _cells[uiCellInd].content) {
                            // The content of this cell of the UI changed, then update it
                            _cells[uiCellInd].content = snakeCell.content
                        }
                    }

                    // TODO: handle game status
                    // TODO: handle prize cell
                }
            }
        }
    }
}
