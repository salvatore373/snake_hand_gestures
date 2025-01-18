package com.snakehandgestures

import kotlinx.coroutines.delay

enum class CellContent {
    EMPTY,  // Represents an empty cell of the grid
    FILLED_BODY,  // Represents a grid cell containing the body of the snake
    FILLED_HEAD,  // Represents a grid cell containing the head of the snake
    PRIZE,  // Represents a grid cell containing the prize
}

// The directions in which the snake can travel
enum class SnakeDirection {
    UP, DOWN, RIGHT, LEFT,
}

enum class GameStatus {
    PLAYING, GAME_OVER
}

/**
 * The possible difficulty levels of the game. The speed is the duration of the timestep in
 * milliseconds.
 */
enum class GameDifficulty(val speed: Int) {
    EASY(1000), MEDIUM(750), HARD(500);

    companion object {
        fun fromSpeed(value: Int): GameDifficulty? =
            GameDifficulty.entries.firstOrNull { it.speed == value }
    }
}

// A class representing a cell of the grid
data class Cell(val x: Int, val y: Int, var content: CellContent) {
    /**
     * Creates a new Cell with the same parameters of this instance, except for the ones
     * specified as arguments.
     */
    fun from(x: Int? = null, y: Int? = null, content: CellContent? = null): Cell {
        return Cell(x ?: this.x, y ?: this.y, content ?: this.content)
    }

    /**
     * Returns whether this cell and anotherCell are at the same position.
     */
    fun isAtSamePosition(anotherCell: Cell): Boolean {
        return x == anotherCell.x && y == anotherCell.y
    }
}

class SnakeLogic(private var gridWidth: Int, private var gridHeight: Int) {
    // The list of the grid cells occupied by the snake. The order is relevant: the first element
    // is the tail and the last is the head.
    var occupiedCells = mutableListOf<Cell>(
        Cell(0, 0, CellContent.FILLED_BODY), Cell(1, 0, CellContent.FILLED_HEAD),
    )
        private set

    // The cell containing the prize
    var prizeCell: Cell? = null
        private set

    // The current direction of the snake
    var currentDirection: SnakeDirection = SnakeDirection.RIGHT

    fun increaseTimestep(): GameStatus {
        var isPlaying: Boolean = true;

        // Update the position of the snake's head
        var headCell = occupiedCells.last()
        var newCell: Cell? = null
        when (currentDirection) {
            SnakeDirection.UP -> {
                val newY = headCell.y - 1
                if (newY < 0) { // Game over if the snake goes out of the boundaries
                    isPlaying = false
                } else {
                    newCell = headCell.from(y = newY)
                }

            }

            SnakeDirection.DOWN -> {
                val newY = headCell.y + 1
                if (newY >= gridHeight) { // Game over if the snake goes out of the boundaries
                    isPlaying = false
                } else {
                    newCell = headCell.from(y = newY)
                }
            }

            SnakeDirection.RIGHT -> {
                val newX = headCell.x + 1
                if (newX >= gridWidth) { // Game over if the snake goes out of the boundaries
                    isPlaying = false
                } else {
                    newCell = headCell.from(x = newX)
                }
            }

            SnakeDirection.LEFT -> {
                val newX = headCell.x - 1
                if (newX < 0) { // Game over if the snake goes out of the boundaries
                    isPlaying = false
                } else {
                    newCell = headCell.from(x = newX)
                }
            }
        }
        if (newCell != null) { // i.e. isPlaying == true
            // Check whether the new head's position intersects the body
            occupiedCells.forEachIndexed { ind, occupCell ->
                if (ind != 0 && occupCell.isAtSamePosition(newCell)) {
                    // Game over if the snake's head hits the body
                    isPlaying = false
                    return@forEachIndexed
                }
            }

            if (isPlaying) {
                // The new position of the snake's head is valid, then continue playing and update
                // the head's position.
                occupiedCells.add(newCell)
            }
        }

        // The cell that was previously holding the head is now holding the body
        if (isPlaying) occupiedCells[occupiedCells.size - 2].content = CellContent.FILLED_BODY

        // Increase the snake's size if the prize was reached
        if (isPlaying && prizeCell != null && occupiedCells.last().isAtSamePosition(prizeCell!!)) {
            increaseSnakeSize()
            prizeCell = null
        }

        // Update the position of the snake's tail
        if (isPlaying) occupiedCells.removeAt(0)

        return if (isPlaying) GameStatus.PLAYING else GameStatus.GAME_OVER
    }

    /**
     * Change the direction of movement of the snake, but only if the new direction is not a
     * forbidden one. Returns the current direction of the snake.
     */
    fun changeDirection(newDirection: SnakeDirection): SnakeDirection {
        // Perform the update only if the new direction is not forbidden
        if ((currentDirection == SnakeDirection.UP && newDirection != SnakeDirection.DOWN) || (currentDirection == SnakeDirection.DOWN && newDirection != SnakeDirection.UP) || (currentDirection == SnakeDirection.RIGHT && newDirection != SnakeDirection.LEFT) || (currentDirection == SnakeDirection.LEFT && newDirection != SnakeDirection.RIGHT)) {
            currentDirection = newDirection
        }

        return currentDirection
    }

    /**
     * Add one unit to the snake's size
     */
    private fun increaseSnakeSize() {
        occupiedCells.add(0, occupiedCells.first())
    }

    /**
     * Updates the position of the prize cell.
     */
    fun updatePrizeCell(newX: Int, newY: Int) {
        prizeCell = Cell(newX, newY, CellContent.PRIZE)
    }

    /**
     * Starts moving the snake in the grid. The provided difficulty decides the size of the timestep
     */
    suspend fun startGame(
        difficulty: GameDifficulty,
        onNewTimestep: (MutableList<Cell>, GameStatus, Cell?) -> Unit
    ) {
        // ) = flow {
        var gameStatus = GameStatus.PLAYING

        while (gameStatus != GameStatus.GAME_OVER) {
            gameStatus = increaseTimestep()
            onNewTimestep(occupiedCells, gameStatus, prizeCell)
            // emit(occupiedCells)

            delay(difficulty.speed.toLong())
        }
    }

    /**
     * It removes all the data about the current game.
     */
    fun clearData() {
        occupiedCells = mutableListOf<Cell>(
            Cell(0, 0, CellContent.FILLED_BODY), Cell(1, 0, CellContent.FILLED_HEAD),
        )
        prizeCell = null
        currentDirection = SnakeDirection.RIGHT
    }
}