package com.laminarflowgames.tictactoe.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class MinimaxTest {

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

    // ── Win vs block preference ───────────────────────────────────────────────

    @Test
    fun `prefers winning over blocking`() {
        // O O _
        // X X _
        // _ _ _   — O can win at (0,2); X threatens at (1,2) but winning takes priority
        board.makeMove(0, 0, Player.O)
        board.makeMove(0, 1, Player.O)
        board.makeMove(1, 0, Player.X)
        board.makeMove(1, 1, Player.X)
        val (row, col) = Minimax.bestMove(board, Player.O)
        assertEquals(0, row)
        assertEquals(2, col)
    }

    // ── Edge cases ────────────────────────────────────────────────────────────

    @Test
    fun `returns a valid cell on empty board`() {
        val (row, col) = Minimax.bestMove(board, Player.O)
        assert(row in 0..2) { "row=$row out of range" }
        assert(col in 0..2) { "col=$col out of range" }
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
        assertNotEquals(Player.X, GameRules.checkWinner(board))
    }

    @Test
    fun `cpu never loses when human plays top-right corner`() {
        simulateFullGame(humanFirstMove = 0 to 2)
        assertNotEquals(Player.X, GameRules.checkWinner(board))
    }

    @Test
    fun `cpu never loses when human plays center`() {
        simulateFullGame(humanFirstMove = 1 to 1)
        assertNotEquals(Player.X, GameRules.checkWinner(board))
    }

    /**
     * Simulates a full game where X plays [humanFirstMove] then minimax plays for X,
     * and minimax plays for O.  Both sides use minimax so the game must draw.
     * We then verify X did not win.
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
