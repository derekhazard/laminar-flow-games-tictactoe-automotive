package com.laminarflowgames.tictactoe.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GameRulesTest {

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
}
