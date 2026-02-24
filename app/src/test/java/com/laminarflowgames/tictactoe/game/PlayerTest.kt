package com.laminarflowgames.tictactoe.game

import org.junit.Assert.assertEquals
import org.junit.Test

internal class PlayerTest {

    @Test
    fun `X opponent is O`() {
        assertEquals(Player.O, Player.X.opponent())
    }

    @Test
    fun `O opponent is X`() {
        assertEquals(Player.X, Player.O.opponent())
    }
}
