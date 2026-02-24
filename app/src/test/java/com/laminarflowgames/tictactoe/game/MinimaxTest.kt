package com.laminarflowgames.tictactoe.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

internal class MinimaxTest {

    private lateinit var board: GameBoard

    @Before
    fun setUp() {
        board = GameBoard()
    }

    // ── Winning move ──────────────────────────────────────────────────────────

    @Test
    fun `takes winning move in row`() {
        // O O _
        // X X _
        // _ _ _
        board.makeMove(0, 0, Player.O)
        board.makeMove(0, 1, Player.O)
        board.makeMove(1, 0, Player.X)
        board.makeMove(1, 1, Player.X)
        val (row, col) = Minimax.bestMove(board, Player.O)
        assertEquals(0, row)
        assertEquals(2, col)
    }

    @Test
    fun `takes winning move in column`() {
        // O X _
        // O X _
        // _ _ _
        board.makeMove(0, 0, Player.O)
        board.makeMove(0, 1, Player.X)
        board.makeMove(1, 0, Player.O)
        board.makeMove(1, 1, Player.X)
        val (row, col) = Minimax.bestMove(board, Player.O)
        assertEquals(2, row)
        assertEquals(0, col)
    }

    @Test
    fun `takes winning move in diagonal`() {
        // O X _
        // X O _
        // _ _ _
        board.makeMove(0, 0, Player.O)
        board.makeMove(0, 1, Player.X)
        board.makeMove(1, 0, Player.X)
        board.makeMove(1, 1, Player.O)
        val (row, col) = Minimax.bestMove(board, Player.O)
        assertEquals(2, row)
        assertEquals(2, col)
    }

    // ── Blocking move ─────────────────────────────────────────────────────────

    @Test
    fun `blocks human win in row`() {
        // X X _
        // O _ _
        // _ _ _
        board.makeMove(0, 0, Player.X)
        board.makeMove(0, 1, Player.X)
        board.makeMove(1, 0, Player.O)
        val (row, col) = Minimax.bestMove(board, Player.O)
        assertEquals(0, row)
        assertEquals(2, col)
    }

    @Test
    fun `blocks human win in column`() {
        // X O _
        // X _ _
        // _ _ _
        board.makeMove(0, 0, Player.X)
        board.makeMove(0, 1, Player.O)
        board.makeMove(1, 0, Player.X)
        val (row, col) = Minimax.bestMove(board, Player.O)
        assertEquals(2, row)
        assertEquals(0, col)
    }

    @Test
    fun `blocks human win in diagonal`() {
        // X _ _
        // _ X _   — X threatens main diagonal at (2,2)
        // O _ _
        board.makeMove(0, 0, Player.X)
        board.makeMove(1, 1, Player.X)
        board.makeMove(2, 0, Player.O)
        val (row, col) = Minimax.bestMove(board, Player.O)
        assertEquals(2, row)
        assertEquals(2, col)
    }

    // ── Win vs block preference ───────────────────────────────────────────────

    @Test
    fun `prefers winning over blocking`() {
        // _ X X   — X threatens row 0 at (0,0)
        // O O _   — O wins at (1,2); winning takes priority over blocking (0,0)
        // _ _ _
        board.makeMove(0, 1, Player.X)
        board.makeMove(0, 2, Player.X)
        board.makeMove(1, 0, Player.O)
        board.makeMove(1, 1, Player.O)
        val (row, col) = Minimax.bestMove(board, Player.O)
        assertEquals(1, row)
        assertEquals(2, col)
    }

    // ── Edge cases ────────────────────────────────────────────────────────────

    @Test
    fun `returns a valid cell on empty board`() {
        val (row, col) = Minimax.bestMove(board, Player.O)
        assertTrue("row=$row out of range", row in 0..2)
        assertTrue("col=$col out of range", col in 0..2)
        assertNull("bestMove must return an empty cell", board.cellAt(row, col))
    }

    @Test
    fun `returns the only empty cell`() {
        // Fill 8 cells leaving (2,2) empty; genuinely no winner
        // X O X
        // O X O
        // O X _
        board.makeMove(0, 0, Player.X); board.makeMove(0, 1, Player.O); board.makeMove(0, 2, Player.X)
        board.makeMove(1, 0, Player.O); board.makeMove(1, 1, Player.X); board.makeMove(1, 2, Player.O)
        board.makeMove(2, 0, Player.O); board.makeMove(2, 1, Player.X)
        val (row, col) = Minimax.bestMove(board, Player.O)
        assertEquals(2, row)
        assertEquals(2, col)
    }

    // ── CPU never loses ───────────────────────────────────────────────────────

    @Test
    fun `cpu never loses when human plays top-left corner`() {
        simulateFullGame(humanFirstMove = 0 to 0)
        assertNull("minimax vs minimax must draw: top-left opening", GameRules.checkWinner(board))
    }

    @Test
    fun `cpu never loses when human plays top-right corner`() {
        simulateFullGame(humanFirstMove = 0 to 2)
        assertNull("minimax vs minimax must draw: top-right opening", GameRules.checkWinner(board))
    }

    @Test
    fun `cpu never loses when human plays center`() {
        simulateFullGame(humanFirstMove = 1 to 1)
        assertNull("minimax vs minimax must draw: center opening", GameRules.checkWinner(board))
    }

    /**
     * Simulates a game where X plays a fixed [humanFirstMove] as the opening, then
     * both sides use [Minimax.bestMove] for all remaining moves.
     * The caller verifies that the game ends in a draw (no winner).
     */
    private fun simulateFullGame(humanFirstMove: Pair<Int, Int>) {
        board = GameBoard()
        // Human (X) plays the first move
        board.makeMove(humanFirstMove.first, humanFirstMove.second, Player.X)
        var turn = Player.O

        while (GameRules.checkWinner(board) == null && !board.isFull()) {
            val (r, c) = Minimax.bestMove(board, turn)
            board.makeMove(r, c, turn)
            if (GameRules.checkWinner(board) != null || board.isFull()) break
            turn = turn.opponent()
        }
    }
}
