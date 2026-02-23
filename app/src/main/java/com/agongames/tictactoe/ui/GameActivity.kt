package com.agongames.tictactoe.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.agongames.tictactoe.R

/**
 * Entry point [Activity][androidx.appcompat.app.AppCompatActivity] for the Tic-Tac-Toe game.
 *
 * Hosts the 3×3 game board and the info panel (turn status, New Game button).
 * Targets the AAOS `automotive_1024p_landscape` display (1024×600 dp).
 */
class GameActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
    }
}
