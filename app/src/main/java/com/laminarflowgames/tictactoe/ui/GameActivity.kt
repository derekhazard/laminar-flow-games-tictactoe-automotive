package com.laminarflowgames.tictactoe.ui

import android.car.Car
import android.car.drivingstate.CarUxRestrictions
import android.car.drivingstate.CarUxRestrictionsManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.laminarflowgames.tictactoe.R
import com.laminarflowgames.tictactoe.game.GameBoard
import com.laminarflowgames.tictactoe.game.GameRules
import com.laminarflowgames.tictactoe.game.Minimax
import com.laminarflowgames.tictactoe.game.Player
import com.laminarflowgames.tictactoe.game.opponent

/**
 * Entry point [Activity][androidx.appcompat.app.AppCompatActivity] for the Tic-Tac-Toe game.
 *
 * Hosts the 3×3 game board and the info panel (turn status, mode selector, New Game button).
 * Targets the AAOS `automotive_1024p_landscape` display (1024×600 dp).
 *
 * Board interaction is gated on three conditions:
 * - [gameOver] is false (a win or draw has not yet been recorded),
 * - [isDrivingRestricted] is false (the vehicle is parked), and
 * - [isCpuThinking] is false (the CPU move has not yet been applied).
 *
 * Park-only enforcement is performed via [CarUxRestrictionsManager]. When
 * [CarUxRestrictions.isRequiresDistractionOptimization] returns true the board
 * cells and mode radio buttons are disabled; the New Game button remains enabled
 * because it is a single, low-distraction action.
 *
 * In [GameMode.VS_CPU] mode the human plays as X (always first mover) and the
 * CPU plays as O. After each human move the CPU move is deferred one frame via
 * [mainHandler] so the "CPU's turn…" status renders before minimax runs. The
 * pending [cpuMoveRunnable] is cancelled in [startNewGame] and [onDestroy] to
 * prevent stale moves against a reset or destroyed Activity.
 */
class GameActivity : AppCompatActivity() {

    // ── Game state ────────────────────────────────────────────────────────────

    private val board = GameBoard()
    private var currentPlayer = Player.X
    private var gameOver = false
    private var isCpuThinking = false
    private var gameMode = GameMode.TWO_PLAYER
    private val cpuPlayer = Player.O

    @Volatile
    private var isDrivingRestricted = false

    // ── CPU scheduling ────────────────────────────────────────────────────────

    private val mainHandler = Handler(Looper.getMainLooper())
    private val cpuMoveRunnable = Runnable {
        isCpuThinking = false
        if (gameOver || isDrivingRestricted) return@Runnable
        val (row, col) = Minimax.bestMove(board, cpuPlayer)
        onCellClicked(row, col)
    }

    // ── Views ─────────────────────────────────────────────────────────────────

    private lateinit var tvStatus: TextView
    private lateinit var cells: Array<Array<Button>>
    private lateinit var rgMode: RadioGroup

    // ── Car API ───────────────────────────────────────────────────────────────

    private var car: Car? = null
    private var uxRestrictionsManager: CarUxRestrictionsManager? = null
    private val uxListener = CarUxRestrictionsManager.OnUxRestrictionsChangedListener { restrictions ->
        isDrivingRestricted = restrictions.isRequiresDistractionOptimization
        runOnUiThread { updateBoardEnabled() }
    }
    private val carLifecycleListener = Car.CarServiceLifecycleListener { connectedCar, ready ->
        if (ready) {
            uxRestrictionsManager =
                connectedCar.getCarManager(Car.CAR_UX_RESTRICTION_SERVICE) as? CarUxRestrictionsManager
            uxRestrictionsManager?.registerListener(uxListener)
            uxRestrictionsManager?.currentCarUxRestrictions?.let { restrictions ->
                isDrivingRestricted = restrictions.isRequiresDistractionOptimization
                updateBoardEnabled()
            }
        } else {
            uxRestrictionsManager?.unregisterListener()
            uxRestrictionsManager = null
        }
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    /**
     * Inflates the layout, wires board cells, the mode selector, and the New
     * Game button, starts [CarUxRestrictionsManager] monitoring, and sets the
     * initial turn status.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        tvStatus = findViewById(R.id.tv_status)
        rgMode = findViewById(R.id.rg_mode)
        wireBoard()
        rgMode.setOnCheckedChangeListener { _, checkedId ->
            gameMode = if (checkedId == R.id.rb_vs_cpu) GameMode.VS_CPU else GameMode.TWO_PLAYER
            startNewGame()
        }
        findViewById<Button>(R.id.btn_new_game).setOnClickListener { startNewGame() }
        initCarUxRestrictions()
        updateStatus()
    }

    /**
     * Cancels any pending CPU move callback, then releases Car API resources
     * before the activity is destroyed.
     */
    override fun onDestroy() {
        mainHandler.removeCallbacks(cpuMoveRunnable)
        uxRestrictionsManager?.unregisterListener()
        car?.disconnect()
        super.onDestroy()
    }

    // ── Board wiring ──────────────────────────────────────────────────────────

    private fun wireBoard() {
        val ids = arrayOf(
            arrayOf(R.id.btn_cell_00, R.id.btn_cell_01, R.id.btn_cell_02),
            arrayOf(R.id.btn_cell_10, R.id.btn_cell_11, R.id.btn_cell_12),
            arrayOf(R.id.btn_cell_20, R.id.btn_cell_21, R.id.btn_cell_22),
        )
        cells = Array(3) { row ->
            Array(3) { col ->
                findViewById<Button>(ids[row][col]).also { btn ->
                    btn.setOnClickListener { onCellClicked(row, col) }
                }
            }
        }
    }

    private fun onCellClicked(row: Int, col: Int) {
        if (gameOver || isDrivingRestricted || isCpuThinking) return
        if (!GameRules.isValidMove(board, row, col)) return
        board.makeMove(row, col, currentPlayer)
        updateCell(row, col)
        updateBoardEnabled()
        val winner = GameRules.checkWinner(board)
        when {
            winner != null -> {
                gameOver = true
                tvStatus.text = getString(R.string.status_winner, winner.name)
                updateBoardEnabled()
            }
            GameRules.isDraw(board) -> {
                gameOver = true
                tvStatus.text = getString(R.string.status_draw)
                updateBoardEnabled()
            }
            else -> {
                currentPlayer = currentPlayer.opponent()
                if (gameMode == GameMode.VS_CPU && currentPlayer == cpuPlayer) {
                    scheduleCpuMove()
                } else {
                    updateStatus()
                }
            }
        }
    }

    private fun scheduleCpuMove() {
        isCpuThinking = true
        updateBoardEnabled()
        tvStatus.text = getString(R.string.status_cpu_turn)
        mainHandler.post(cpuMoveRunnable)
    }

    private fun updateCell(row: Int, col: Int) {
        val btn = cells[row][col]
        val player = board.cellAt(row, col)
        btn.text = player?.name ?: ""
        btn.contentDescription = if (player == null) {
            getString(R.string.cell_desc_empty, row + 1, col + 1)
        } else {
            getString(R.string.cell_desc_occupied, row + 1, col + 1, player.name)
        }
    }

    private fun updateAllCells() {
        for (row in 0..2) {
            for (col in 0..2) {
                updateCell(row, col)
            }
        }
    }

    private fun updateBoardEnabled() {
        for (row in 0..2) {
            for (col in 0..2) {
                cells[row][col].isEnabled =
                    !gameOver && !isDrivingRestricted && !isCpuThinking && board.cellAt(row, col) == null
            }
        }
        rgMode.isEnabled = !isDrivingRestricted
        for (i in 0 until rgMode.childCount) {
            rgMode.getChildAt(i).isEnabled = !isDrivingRestricted
        }
    }

    private fun updateStatus() {
        tvStatus.text = getString(R.string.status_turn, currentPlayer.name)
    }

    private fun startNewGame() {
        mainHandler.removeCallbacks(cpuMoveRunnable)
        isCpuThinking = false
        board.reset()
        currentPlayer = Player.X
        gameOver = false
        updateAllCells()
        updateBoardEnabled()
        updateStatus()
    }

    // ── CarUxRestrictions ─────────────────────────────────────────────────────

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    private fun initCarUxRestrictions() {
        try {
            car = Car.createCar(
                this,
                mainHandler,
                Car.CAR_WAIT_TIMEOUT_DO_NOT_WAIT,
                carLifecycleListener,
            )
        } catch (e: Exception) {
            // Non-automotive environments (e.g. standard Android or JVM tests) do not
            // provide the Car service. Fail gracefully: leave isDrivingRestricted = false
            // so the board remains interactive.
        }
    }
}
