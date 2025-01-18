package com.snakehandgestures

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// The value of the prize for the score
const val PRIZE_VAL = 10

private fun _convertIndexToCoordinates(i: Int) = Pair(i % GRID_HEIGHT, i / GRID_WIDTH)
private fun _convertCoordinatesToIndex(x: Int, y: Int) = y * GRID_WIDTH + x


class SnakeCell(val x: Int, val y: Int) {
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
        val (x, y) = _convertIndexToCoordinates(it)
        SnakeCell(x, y)
    }.toMutableStateList()
    val cells: List<SnakeCell>
        get() = _cells

    // Whether the user is currently playing or not
    private var _gameStatus by mutableStateOf(GameStatus.PLAYING)
    val gameStatus: GameStatus
        get() = _gameStatus

    // Whether the user is currently playing or not
    // private var _prizeCell: SnakeCell? by mutableStateOf(null)
    private var _prizeCell: SnakeCell? = null
    val prizeCell: SnakeCell?
        get() = _prizeCell

    // The score achieved by the player
    private var _score: Int by mutableIntStateOf(0)
    val score: Int
        get() = _score

    // The current direction of the snake movements
    private var _direction: SnakeDirection by mutableStateOf(_snakeLogic.currentDirection)
    val direction: SnakeDirection
        get() = _direction

    // Function to start the periodic task
    fun startGameLogic(difficulty: GameDifficulty) {
        viewModelScope.launch {
            Log.i("VIEWMODELSCOPE", "started")
            _snakeLogic.startGame(difficulty)
            { snakeCells, newGameStatus, newPrizeCell ->
                // Initialize a representation of the grid, where the false items represent empty cells at the new timestep
                var emptyCells = MutableList(GRID_WIDTH * GRID_HEIGHT) { false }
                // Update the cells in _cells that contain the snake
                for (snakeCell in snakeCells) {
                    var uiCellInd = _convertCoordinatesToIndex(snakeCell.x, snakeCell.y)
                    if (snakeCell.content != _cells[uiCellInd].content) {
                        // The content of this cell of the UI changed, then update it
                        _cells[uiCellInd].content = snakeCell.content
                    }
                    if (snakeCell.content != CellContent.EMPTY) {
                        emptyCells[_convertCoordinatesToIndex(snakeCell.x, snakeCell.y)] = true
                    }
                }
                // Empty the cells that need to be emptied
                if (_prizeCell != null)
                    emptyCells[_convertCoordinatesToIndex(_prizeCell!!.x, _prizeCell!!.y)] = true
                for ((i, eCell) in emptyCells.withIndex()) {
                    if (!eCell)
                        _cells[i].content = CellContent.EMPTY
                }

                // Update the game status if necessary
                if (newGameStatus != _gameStatus) {
                    _gameStatus = newGameStatus
                }
                // Update the snake direction if necessary
                if (_snakeLogic.currentDirection != _direction) {
                    _direction = _snakeLogic.currentDirection
                }

                // Check whether the prize was reached or needs to be initialized
                if ((_prizeCell?.x != newPrizeCell?.x && _prizeCell?.y != newPrizeCell?.y) ||
                    _prizeCell == null
                ) {
                    if (_prizeCell != null) {
                        // The prize was reached, then increase the score. The increase is inversely proportional to the difficulty.
                        _score += (PRIZE_VAL * (1000.0 / difficulty.speed)).roundToInt()
                    }

                    // Select the new prize cell randomly among the free cells
                    val newPrizeCellInd = emptyCells.withIndex().filter { !it.value }.random().index
                    val (newPrizeCellX, newPrizeCellY) = _convertIndexToCoordinates(newPrizeCellInd)
                    // Put the prize in the new cell
                    _snakeLogic.updatePrizeCell(newPrizeCellX, newPrizeCellY)
                    _cells[newPrizeCellInd].content = CellContent.PRIZE
                    _prizeCell = _cells[newPrizeCellInd]
                }
            }
        }
    }

    fun changeDirection(newDirection: SnakeDirection) = _snakeLogic.changeDirection(newDirection)

    fun restart() {
        // viewModelScope.cancel("Game Restarted")
        viewModelScope.coroutineContext.cancelChildren()
        // Clear the data in the logic data structure
        _snakeLogic.clearData()
        // Clear the data in the view model
        for (i in _cells.indices) _cells[i].content = CellContent.EMPTY
        for (snakeCell in _snakeLogic.occupiedCells) {
            var uiCellInd = _convertCoordinatesToIndex(snakeCell.x, snakeCell.y)
            _cells[uiCellInd].content = snakeCell.content
        }
        _prizeCell = null
        _gameStatus = GameStatus.PLAYING
        _direction = _snakeLogic.currentDirection
    }
}