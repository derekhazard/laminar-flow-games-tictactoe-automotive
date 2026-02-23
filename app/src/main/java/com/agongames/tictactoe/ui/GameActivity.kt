package com.agongames.tictactoe.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.agongames.tictactoe.R

/** Main activity for the Tic-Tac-Toe game screen. */
class GameActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
    }
}
