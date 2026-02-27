package com.laminarflowgames.tictactoe.game

/**
 * Stateless game-rule evaluations for Tic-Tac-Toe.
 *
 * All functions are pure: they read the board but never mutate it.
 */
object GameRules {

    private val WIN_LINES = listOf(
        listOf(0 to 0, 0 to 1, 0 to 2), // row 0
        listOf(1 to 0, 1 to 1, 1 to 2), // row 1
        listOf(2 to 0, 2 to 1, 2 to 2), // row 2
        listOf(0 to 0, 1 to 0, 2 to 0), // col 0
        listOf(0 to 1, 1 to 1, 2 to 1), // col 1
        listOf(0 to 2, 1 to 2, 2 to 2), // col 2
        listOf(0 to 0, 1 to 1, 2 to 2), // main diagonal
        listOf(0 to 2, 1 to 1, 2 to 0), // anti-diagonal
    )

    /**
     * Returns the [Player] who has completed a line, or null if no winner exists yet.
     *
     * Checks all eight winning lines (three rows, three columns, two diagonals).
     */
    fun checkWinner(board: GameBoard): Player? {
        for (line in WIN_LINES) {
            val (r0, c0) = line[0]
            val (r1, c1) = line[1]
            val (r2, c2) = line[2]
            val a = board.cellAt(r0, c0)
            val b = board.cellAt(r1, c1)
            val c = board.cellAt(r2, c2)
            if (a != null && a == b && b == c) return a
        }
        return null
    }

    /**
     * Returns the three (row, col) pairs that form the winning line, or null if
     * no winner exists yet.
     *
     * Mirrors the traversal of [checkWinner] so the two functions always agree.
     */
    fun winningLine(board: GameBoard): List<Pair<Int, Int>>? {
        for (line in WIN_LINES) {
            val (r0, c0) = line[0]
            val (r1, c1) = line[1]
            val (r2, c2) = line[2]
            val a = board.cellAt(r0, c0)
            if (a != null && a == board.cellAt(r1, c1) && a == board.cellAt(r2, c2)) return line
        }
        return null
    }

    /**
     * Returns true when the board is full and no player has won.
     *
     * Internally calls both [GameBoard.isFull] and [checkWinner]; callers do not
     * need to pre-check for a winner before calling this function.
     */
    fun isDraw(board: GameBoard): Boolean = board.isFull() && checkWinner(board) == null

    /**
     * Returns true if placing a piece at [row]/[col] is a legal move.
     *
     * A move is legal when the target cell is currently empty. This does not
     * check whether the game is already over; callers are responsible for
     * gating moves on game state.
     */
    fun isValidMove(board: GameBoard, row: Int, col: Int): Boolean =
        board.cellAt(row, col) == null
}
