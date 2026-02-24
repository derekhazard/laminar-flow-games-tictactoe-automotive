package com.laminarflowgames.tictactoe.game

private const val SCORE_WIN = 10

/**
 * Stateless minimax AI for Tic-Tac-Toe.
 *
 * Uses depth-aware scoring so the CPU prefers winning sooner and losing later:
 * - CPU win: `SCORE_WIN - depth`
 * - CPU loss: `depth - SCORE_WIN`
 * - Draw: `0`
 *
 * Backtracking is done via [GameBoard.clearCell] (mutation + undo) rather than
 * copying the board, avoiding allocations on the small 3×3 tree.
 */
object Minimax {

    /**
     * Returns the (row, col) of the best move for [cpuPlayer] on the current [board].
     *
     * Must only be called when the board has at least one empty cell and no winner
     * has been declared.
     */
    fun bestMove(board: GameBoard, cpuPlayer: Player): Pair<Int, Int> {
        require(!board.isFull()) { "bestMove requires at least one empty cell" }
        var bestScore = Int.MIN_VALUE
        var bestRow = -1
        var bestCol = -1

        for (row in 0..2) {
            for (col in 0..2) {
                if (board.cellAt(row, col) != null) continue
                board.makeMove(row, col, cpuPlayer)
                val score = evaluate(board, cpuPlayer, isMaximizing = false, depth = 1)
                board.clearCell(row, col)
                if (score > bestScore) {
                    bestScore = score
                    bestRow = row
                    bestCol = col
                }
            }
        }

        return bestRow to bestCol
    }

    private fun evaluate(board: GameBoard, cpuPlayer: Player, isMaximizing: Boolean, depth: Int): Int {
        val winner = GameRules.checkWinner(board)
        val terminal = when {
            winner == cpuPlayer -> SCORE_WIN - depth
            winner == cpuPlayer.opponent() -> depth - SCORE_WIN
            board.isFull() -> 0
            else -> null
        }
        if (terminal != null) return terminal

        val mover = if (isMaximizing) cpuPlayer else cpuPlayer.opponent()
        var best = if (isMaximizing) Int.MIN_VALUE else Int.MAX_VALUE

        for (row in 0..2) {
            for (col in 0..2) {
                if (board.cellAt(row, col) != null) continue
                board.makeMove(row, col, mover)
                val score = evaluate(board, cpuPlayer, !isMaximizing, depth + 1)
                board.clearCell(row, col)
                best = if (isMaximizing) maxOf(best, score) else minOf(best, score)
            }
        }

        return best
    }
}
