package com.laminarflowgames.tictactoe.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

internal class GameRulesTest {

    private lateinit var board: GameBoard

    @Before
    fun setUp() {
        board = GameBoard()
    }

    // --- checkWinner ---

    @Test
    fun `checkWinner returns null on empty board`() {
        assertNull(GameRules.checkWinner(board))
    }

    @Test
    fun `checkWinner returns null with no complete line`() {
        board.makeMove(0, 0, Player.X)
        board.makeMove(0, 1, Player.O)
        assertNull(GameRules.checkWinner(board))
    }

    @Test
    fun `checkWinner detects row 0 win for X`() {
        board.makeMove(0, 0, Player.X)
        board.makeMove(0, 1, Player.X)
        board.makeMove(0, 2, Player.X)
        assertEquals(Player.X, GameRules.checkWinner(board))
    }

    @Test
    fun `checkWinner detects row 1 win for O`() {
        board.makeMove(1, 0, Player.O)
        board.makeMove(1, 1, Player.O)
        board.makeMove(1, 2, Player.O)
        assertEquals(Player.O, GameRules.checkWinner(board))
    }

    @Test
    fun `checkWinner detects row 2 win for X`() {
        board.makeMove(2, 0, Player.X)
        board.makeMove(2, 1, Player.X)
        board.makeMove(2, 2, Player.X)
        assertEquals(Player.X, GameRules.checkWinner(board))
    }

    @Test
    fun `checkWinner detects col 0 win for X`() {
        board.makeMove(0, 0, Player.X)
        board.makeMove(1, 0, Player.X)
        board.makeMove(2, 0, Player.X)
        assertEquals(Player.X, GameRules.checkWinner(board))
    }

    @Test
    fun `checkWinner detects col 1 win for O`() {
        board.makeMove(0, 1, Player.O)
        board.makeMove(1, 1, Player.O)
        board.makeMove(2, 1, Player.O)
        assertEquals(Player.O, GameRules.checkWinner(board))
    }

    @Test
    fun `checkWinner detects col 2 win for X`() {
        board.makeMove(0, 2, Player.X)
        board.makeMove(1, 2, Player.X)
        board.makeMove(2, 2, Player.X)
        assertEquals(Player.X, GameRules.checkWinner(board))
    }

    @Test
    fun `checkWinner detects main diagonal win for O`() {
        board.makeMove(0, 0, Player.O)
        board.makeMove(1, 1, Player.O)
        board.makeMove(2, 2, Player.O)
        assertEquals(Player.O, GameRules.checkWinner(board))
    }

    @Test
    fun `checkWinner detects anti-diagonal win for X`() {
        board.makeMove(0, 2, Player.X)
        board.makeMove(1, 1, Player.X)
        board.makeMove(2, 0, Player.X)
        assertEquals(Player.X, GameRules.checkWinner(board))
    }

    // --- isDraw ---

    @Test
    fun `isDraw returns false on empty board`() {
        assertFalse(GameRules.isDraw(board))
    }

    @Test
    fun `isDraw returns false when board is partially filled`() {
        board.makeMove(0, 0, Player.X)
        assertFalse(GameRules.isDraw(board))
    }

    @Test
    fun `isDraw returns true on full board with no winner`() {
        // X O X
        // X X O
        // O X O  — no three-in-a-row for either player
        board.makeMove(0, 0, Player.X)
        board.makeMove(0, 1, Player.O)
        board.makeMove(0, 2, Player.X)

        board.makeMove(1, 0, Player.X)
        board.makeMove(1, 1, Player.X)
        board.makeMove(1, 2, Player.O)

        board.makeMove(2, 0, Player.O)
        board.makeMove(2, 1, Player.X)
        board.makeMove(2, 2, Player.O)
        assertTrue(GameRules.isDraw(board))
    }

    @Test
    fun `isDraw returns false when board is not full`() {
        board.makeMove(0, 0, Player.X)
        board.makeMove(0, 1, Player.X)
        board.makeMove(0, 2, Player.X)
        assertFalse(GameRules.isDraw(board))
    }

    @Test
    fun `isDraw returns false on full board with winner`() {
        // X wins row 0; board is full — isFull() and checkWinner() both evaluated
        board.makeMove(0, 0, Player.X)
        board.makeMove(0, 1, Player.X)
        board.makeMove(0, 2, Player.X)

        board.makeMove(1, 0, Player.O)
        board.makeMove(1, 1, Player.O)
        board.makeMove(1, 2, Player.X)

        board.makeMove(2, 0, Player.O)
        board.makeMove(2, 1, Player.X)
        board.makeMove(2, 2, Player.O)
        assertFalse(GameRules.isDraw(board))
    }

    // --- isValidMove ---

    @Test
    fun `isValidMove returns true for empty cell`() {
        assertTrue(GameRules.isValidMove(board, 0, 0))
    }

    @Test
    fun `isValidMove returns false for occupied cell`() {
        board.makeMove(0, 0, Player.X)
        assertFalse(GameRules.isValidMove(board, 0, 0))
    }

    @Test
    fun `isValidMove returns true for empty cell on partially filled board`() {
        board.makeMove(0, 0, Player.X)
        assertTrue(GameRules.isValidMove(board, 1, 1))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `isValidMove throws on out-of-bounds coordinates`() {
        GameRules.isValidMove(board, 3, 0)
    }

    // --- winningLine ---

    @Test
    fun `winningLine returns null on empty board`() {
        assertNull(GameRules.winningLine(board))
    }

    @Test
    fun `winningLine returns null with no complete line`() {
        board.makeMove(0, 0, Player.X)
        board.makeMove(0, 1, Player.O)
        assertNull(GameRules.winningLine(board))
    }

    @Test
    fun `winningLine returns null on draw board`() {
        board.makeMove(0, 0, Player.X); board.makeMove(0, 1, Player.O); board.makeMove(0, 2, Player.X)
        board.makeMove(1, 0, Player.X); board.makeMove(1, 1, Player.X); board.makeMove(1, 2, Player.O)
        board.makeMove(2, 0, Player.O); board.makeMove(2, 1, Player.X); board.makeMove(2, 2, Player.O)
        assertNull(GameRules.winningLine(board))
    }

    @Test
    fun `winningLine returns row 0 cells`() {
        board.makeMove(0, 0, Player.X); board.makeMove(0, 1, Player.X); board.makeMove(0, 2, Player.X)
        assertEquals(listOf(0 to 0, 0 to 1, 0 to 2), GameRules.winningLine(board))
    }

    @Test
    fun `winningLine returns row 1 cells`() {
        board.makeMove(1, 0, Player.O); board.makeMove(1, 1, Player.O); board.makeMove(1, 2, Player.O)
        assertEquals(listOf(1 to 0, 1 to 1, 1 to 2), GameRules.winningLine(board))
    }

    @Test
    fun `winningLine returns row 2 cells`() {
        board.makeMove(2, 0, Player.X); board.makeMove(2, 1, Player.X); board.makeMove(2, 2, Player.X)
        assertEquals(listOf(2 to 0, 2 to 1, 2 to 2), GameRules.winningLine(board))
    }

    @Test
    fun `winningLine returns col 0 cells`() {
        board.makeMove(0, 0, Player.X); board.makeMove(1, 0, Player.X); board.makeMove(2, 0, Player.X)
        assertEquals(listOf(0 to 0, 1 to 0, 2 to 0), GameRules.winningLine(board))
    }

    @Test
    fun `winningLine returns col 1 cells`() {
        board.makeMove(0, 1, Player.O); board.makeMove(1, 1, Player.O); board.makeMove(2, 1, Player.O)
        assertEquals(listOf(0 to 1, 1 to 1, 2 to 1), GameRules.winningLine(board))
    }

    @Test
    fun `winningLine returns col 2 cells`() {
        board.makeMove(0, 2, Player.X); board.makeMove(1, 2, Player.X); board.makeMove(2, 2, Player.X)
        assertEquals(listOf(0 to 2, 1 to 2, 2 to 2), GameRules.winningLine(board))
    }

    @Test
    fun `winningLine returns main diagonal cells`() {
        board.makeMove(0, 0, Player.O); board.makeMove(1, 1, Player.O); board.makeMove(2, 2, Player.O)
        assertEquals(listOf(0 to 0, 1 to 1, 2 to 2), GameRules.winningLine(board))
    }

    @Test
    fun `winningLine returns anti-diagonal cells`() {
        board.makeMove(0, 2, Player.X); board.makeMove(1, 1, Player.X); board.makeMove(2, 0, Player.X)
        assertEquals(listOf(0 to 2, 1 to 1, 2 to 0), GameRules.winningLine(board))
    }

    @Test
    fun `winningLine contains exactly 3 cells for any win`() {
        board.makeMove(0, 0, Player.X); board.makeMove(0, 1, Player.X); board.makeMove(0, 2, Player.X)
        assertNotNull(GameRules.winningLine(board))
        assertEquals(3, GameRules.winningLine(board)!!.size)
    }
}
