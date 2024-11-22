package com.snakehandgestures

private enum class CellContent {
    // EMPTY,  // Represents an empty cell of the grid
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

// A class representing a cell of the grid
private data class Cell(val x: Int, val y: Int, var content: CellContent) {
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

class SnakeLogic {
    // The list of the grid cells occupied by the snake. The order is relevant: the first element
    // is the tail and the last is the head.
    private var occupiedCells = mutableListOf<Cell>(
        Cell(0, 0, CellContent.FILLED_BODY), Cell(1, 0, CellContent.FILLED_HEAD),
    )

    // The cell containing the prize
    private var prizeCell: Cell? = null;

    // The current direction of the snake
    private var currentDirection: SnakeDirection = SnakeDirection.RIGHT

    private var gridHeight: Int = 5;
    private var gridWidth: Int = 5;

    fun increaseTimestep(): GameStatus {
        var isPlaying: Boolean = true;

        // Update the position of the snake's head
        var headCell = occupiedCells.last()
        when (currentDirection) {
            SnakeDirection.UP -> {
                val newY = headCell.y - 1
                if (newY < 0) {
                    isPlaying = false
                } else {
                    occupiedCells.add(headCell.from(y = newY))
                }

            }

            SnakeDirection.DOWN -> {
                val newY = headCell.y + 1
                if (newY >= gridHeight) {
                    isPlaying = false
                } else {
                    occupiedCells.add(headCell.from(y = newY))
                }
            }

            SnakeDirection.RIGHT -> {
                val newX = headCell.x + 1
                if (newX >= gridWidth) {
                    isPlaying = false
                } else {
                    occupiedCells.add(headCell.from(x = newX))
                }
            }

            SnakeDirection.LEFT -> {
                val newX = headCell.x - 1
                if (newX < 0) {
                    isPlaying = false
                } else {
                    occupiedCells.add(headCell.from(x = newX))
                }
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
        occupiedCells.removeAt(0)

        return if (isPlaying) GameStatus.PLAYING else GameStatus.GAME_OVER
    }

    /**
     * Change the direction of movement of the snake
     */
    fun changeDirection(newDirection: SnakeDirection) {
        currentDirection = newDirection
        // TODO: handle moves in forbidden directions
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
}