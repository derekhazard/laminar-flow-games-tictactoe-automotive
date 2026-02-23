package com.laminarflowgames.tictactoe.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GameBoardTest {

    private lateinit var board: GameBoard

    @Before
    fun setUp() {
        board = GameBoard()
    }

    @Test
    fun `initial board has all cells empty`() {
        for (row in 0..2) {
            for (col in 0..2) {
                assertNull("Cell ($row, $col) should be null initially", board.cellAt(row, col))
            }
        }
    }

    @Test
    fun `makeMove places player in empty cell`() {
        assertTrue(board.makeMove(0, 0, Player.X))
        assertEquals(Player.X, board.cellAt(0, 0))
    }

    @Test
    fun `makeMove returns false on occupied cell`() {
        board.makeMove(1, 1, Player.X)
        assertFalse(board.makeMove(1, 1, Player.O))
        assertEquals(Player.X, board.cellAt(1, 1))
    }

    @Test
    fun `makeMove does not overwrite occupied cell`() {
        board.makeMove(2, 2, Player.O)
        board.makeMove(2, 2, Player.X)
        assertEquals(Player.O, board.cellAt(2, 2))
    }

    @Test
    fun `isFull returns false on empty board`() {
        assertFalse(board.isFull())
    }

    @Test
    fun `isFull returns false when board is partially filled`() {
        board.makeMove(0, 0, Player.X)
        assertFalse(board.isFull())
    }

    @Test
    fun `isFull returns true when all nine cells are occupied`() {
        for (row in 0..2) {
            for (col in 0..2) {
                val player = if ((row * 3 + col) % 2 == 0) Player.X else Player.O
                board.makeMove(row, col, player)
            }
        }
        assertTrue(board.isFull())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `cellAt throws on out-of-bounds coordinates`() {
        board.cellAt(3, 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `makeMove throws on out-of-bounds coordinates`() {
        board.makeMove(0, 3, Player.X)
    }

    @Test
    fun `reset clears all cells`() {
        board.makeMove(0, 0, Player.X)
        board.makeMove(1, 1, Player.O)
        board.reset()
        for (row in 0..2) {
            for (col in 0..2) {
                assertNull("Cell ($row, $col) should be null after reset", board.cellAt(row, col))
            }
        }
    }

    @Test
    fun `reset allows moves to be made again`() {
        board.makeMove(0, 0, Player.X)
        board.reset()
        assertTrue(board.makeMove(0, 0, Player.O))
        assertEquals(Player.O, board.cellAt(0, 0))
    }
}
