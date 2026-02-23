package com.laminarflowgames.tictactoe.game

/** Represents one of the two players in a Tic-Tac-Toe game. */
enum class Player { X, O }

/** Returns the opposing [Player]. */
fun Player.opponent(): Player = when (this) {
    Player.X -> Player.O
    Player.O -> Player.X
}
