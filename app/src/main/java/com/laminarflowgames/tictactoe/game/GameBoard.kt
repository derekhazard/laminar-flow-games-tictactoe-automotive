package com.laminarflowgames.tictactoe.game

/**
 * Represents the mutable state of a 3×3 Tic-Tac-Toe board.
 *
 * Cells are addressed by zero-based (row, col) coordinates where (0,0) is the
 * top-left corner. This class is intentionally free of Android dependencies so
 * it can be unit-tested on the JVM without an emulator.
 */
class GameBoard {

    private val cells = arrayOfNulls<Player>(9)

    private fun requireValidCoords(row: Int, col: Int) {
        require(row in 0..2 && col in 0..2) { "row and col must be in 0..2, got ($row, $col)" }
    }

    /**
     * Returns the [Player] occupying [row]/[col], or null if the cell is empty.
     *
     * @throws IllegalArgumentException if [row] or [col] is outside 0..2.
     */
    fun cellAt(row: Int, col: Int): Player? {
        requireValidCoords(row, col)
        return cells[row * 3 + col]
    }

    /**
     * Places [player] at [row]/[col].
     *
     * @return true if the move was accepted; false if the cell is already occupied.
     * @throws IllegalArgumentException if [row] or [col] is outside 0..2.
     */
    fun makeMove(row: Int, col: Int, player: Player): Boolean {
        requireValidCoords(row, col)
        if (cells[row * 3 + col] != null) return false
        cells[row * 3 + col] = player
        return true
    }

    /** Returns true when every cell on the board is occupied. */
    fun isFull(): Boolean = cells.none { it == null }

    /** Clears all cells, returning the board to its initial empty state. */
    fun reset() = cells.fill(null)
}
