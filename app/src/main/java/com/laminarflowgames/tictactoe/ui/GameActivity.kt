package com.laminarflowgames.tictactoe.ui

import android.car.Car
import android.car.drivingstate.CarUxRestrictions
import android.car.drivingstate.CarUxRestrictionsManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.ToggleButton
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
 * Hosts the 3×3 game board and the info panel (turn status, mode selector, Clear Board button).
 * Targets the AAOS `automotive_1024p_landscape` display (1024×600 dp).
 *
 * Board interaction is gated on three conditions:
 * - [gameOver] is false (a win or draw has not yet been recorded),
 * - [isDrivingRestricted] is false (the vehicle is parked), and
 * - [isCpuThinking] is false (the CPU move has not yet been applied).
 *
 * Park-only enforcement is performed via [CarUxRestrictionsManager]. When
 * [CarUxRestrictions.isRequiresDistractionOptimization] returns true the board
 * cells and the mode toggle are disabled; the Clear Board and Clear Score buttons
 * remain enabled because they are single, low-distraction actions.
 *
 * In [GameMode.VS_CPU] mode the human plays as X (always first mover) and the
 * CPU plays as O. After each human move the CPU move is deferred by
 * [CPU_MOVE_DELAY_MS] (1 second) via [mainHandler] so the "CPU's turn…" status
 * is visible before minimax runs. If a driving restriction fires while the move
 * is pending, [isCpuThinking] stays true (board remains locked) and the move is
 * rescheduled when restrictions lift. The pending [cpuMoveRunnable] is cancelled
 * in [clearBoard] and [onDestroy] to prevent stale moves against a reset or
 * destroyed Activity.
 */
class GameActivity : AppCompatActivity() {

    // ── Game state ────────────────────────────────────────────────────────────

    private val board = GameBoard()
    private var currentPlayer = Player.X
    private var gameOver = false
    @Volatile
    private var isCpuThinking = false
    private var gameMode = GameMode.VS_CPU
    private val cpuPlayer = Player.O

    // ── Score state ───────────────────────────────────────────────────────────

    private var winsX = 0
    private var winsO = 0
    private var draws = 0

    @Volatile
    private var isDrivingRestricted = false
    private var lastDrivingRestricted = false

    // ── CPU scheduling ────────────────────────────────────────────────────────

    private val mainHandler = Handler(Looper.getMainLooper())
    private val cpuMoveRunnable = Runnable {
        if (gameOver || isDrivingRestricted) return@Runnable
        isCpuThinking = false
        val (row, col) = Minimax.bestMove(board, cpuPlayer)
        onCellClicked(row, col)
    }
    private val autoClearBoardRunnable = Runnable { clearBoard() }

    // ── Win flash ─────────────────────────────────────────────────────────────

    private var lastWinLine: List<Pair<Int, Int>>? = null
    private var winFlashStep = 0
    private val winFlashRunnable = object : Runnable {
        override fun run() {
            val line = lastWinLine ?: return
            // If a driving restriction fires mid-animation, abort the flash and
            // restore the correct hard-locked state so no cell appears interactive.
            if (isDrivingRestricted) {
                highlightCells(line, false)
                updateBoardEnabled()
                return
            }
            // Even steps = highlight ON; odd steps = highlight OFF.
            highlightCells(line, winFlashStep % 2 == 0)
            winFlashStep++
            if (winFlashStep < WIN_FLASH_COUNT * 2) {
                mainHandler.postDelayed(this, WIN_FLASH_PERIOD_MS)
            }
        }
    }

    // ── Views ─────────────────────────────────────────────────────────────────

    private lateinit var tvScoreHeader: TextView
    private lateinit var tvScoreHeaderWins: TextView
    private lateinit var tvLabelX: TextView
    private lateinit var tvLabelO: TextView
    private lateinit var tvLabelDraws: TextView
    private lateinit var tvScoreX: TextView
    private lateinit var tvScoreO: TextView
    private lateinit var tvScoreDraws: TextView
    private lateinit var tvStatus: TextView
    private lateinit var cells: Array<Array<Button>>
    private lateinit var toggleMode: ToggleButton

    // ── Car API ───────────────────────────────────────────────────────────────

    private var car: Car? = null
    private var uxRestrictionsManager: CarUxRestrictionsManager? = null
    private val uxListener = CarUxRestrictionsManager.OnUxRestrictionsChangedListener { restrictions ->
        isDrivingRestricted = restrictions.isRequiresDistractionOptimization
        val restrictionLifted = lastDrivingRestricted && !isDrivingRestricted
        val cpuTurnPending = gameMode == GameMode.VS_CPU &&
            currentPlayer == cpuPlayer &&
            isCpuThinking &&
            !gameOver
        if (restrictionLifted && cpuTurnPending) {
            mainHandler.removeCallbacks(cpuMoveRunnable)
            mainHandler.postDelayed(cpuMoveRunnable, CPU_MOVE_DELAY_MS)
        }
        lastDrivingRestricted = isDrivingRestricted
        runOnUiThread { updateBoardEnabled() }
    }
    private val carLifecycleListener = Car.CarServiceLifecycleListener { connectedCar, ready ->
        if (ready) {
            uxRestrictionsManager =
                connectedCar.getCarManager(Car.CAR_UX_RESTRICTION_SERVICE) as? CarUxRestrictionsManager
            uxRestrictionsManager?.registerListener(uxListener)
            uxRestrictionsManager?.currentCarUxRestrictions?.let { restrictions ->
                isDrivingRestricted = restrictions.isRequiresDistractionOptimization
                lastDrivingRestricted = isDrivingRestricted
                updateBoardEnabled()
            }
        } else {
            uxRestrictionsManager?.unregisterListener()
            uxRestrictionsManager = null
        }
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    /**
     * Inflates the layout, wires board cells, the mode selector, and the Clear
     * Board button, starts [CarUxRestrictionsManager] monitoring, and sets the
     * initial turn status.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        tvScoreHeader = findViewById(R.id.tv_score_header)
        tvScoreHeaderWins = findViewById(R.id.tv_score_header_wins)
        tvLabelX = findViewById(R.id.tv_label_x)
        tvLabelO = findViewById(R.id.tv_label_o)
        tvLabelDraws = findViewById(R.id.tv_label_draws)
        tvScoreX = findViewById(R.id.tv_score_x)
        tvScoreO = findViewById(R.id.tv_score_o)
        tvScoreDraws = findViewById(R.id.tv_score_draws)
        tvStatus = findViewById(R.id.tv_status)
        toggleMode = findViewById(R.id.toggle_mode)
        wireBoard()
        toggleMode.setOnCheckedChangeListener { _, isChecked ->
            gameMode = if (isChecked) GameMode.TWO_PLAYER else GameMode.VS_CPU
            clearScore()
        }
        findViewById<Button>(R.id.btn_new_game).setOnClickListener { clearBoard() }
        findViewById<Button>(R.id.btn_new_round).setOnClickListener { clearScore() }
        initCarUxRestrictions()
        updateScore()
        updateStatus()
    }

    /**
     * Cancels any pending CPU move callback, then releases Car API resources
     * before the activity is destroyed.
     */
    override fun onDestroy() {
        mainHandler.removeCallbacks(cpuMoveRunnable)
        mainHandler.removeCallbacks(autoClearBoardRunnable)
        mainHandler.removeCallbacks(winFlashRunnable)
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

    @Suppress("CyclomaticComplexMethod")
    private fun onCellClicked(row: Int, col: Int) {
        if (gameOver || isDrivingRestricted || isCpuThinking) return
        if (!GameRules.isValidMove(board, row, col)) return
        board.makeMove(row, col, currentPlayer)
        updateCell(row, col)
        updateBoardEnabled()
        val winner = GameRules.checkWinner(board)
        when {
            winner != null -> {
                if (winner == Player.X) winsX++ else winsO++
                GameRules.winningLine(board)?.let { line ->
                    lastWinLine = line
                    winFlashStep = 0
                    mainHandler.removeCallbacks(winFlashRunnable)
                    mainHandler.post(winFlashRunnable)
                }
                val isX = winner == Player.X
                val winColor = getColor(if (isX) R.color.player_x else R.color.player_o)
                val winText = if (gameMode == GameMode.VS_CPU) {
                    getString(if (isX) R.string.status_you_win else R.string.status_android_wins)
                } else {
                    getString(R.string.status_winner, winner.name)
                }
                finishRound(winText, winColor)
            }
            GameRules.isDraw(board) -> {
                draws++
                finishRound(getString(R.string.status_draw), getColor(R.color.accent))
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

    private fun finishRound(statusText: String, statusColor: Int) {
        gameOver = true
        updateScore()
        tvStatus.text = statusText
        tvStatus.setTextColor(statusColor)
        updateBoardEnabled()
        mainHandler.removeCallbacks(autoClearBoardRunnable)
        mainHandler.postDelayed(autoClearBoardRunnable, AUTO_CLEAR_BOARD_DELAY_MS)
    }

    private fun scheduleCpuMove() {
        isCpuThinking = true
        updateBoardEnabled()
        tvStatus.text = getString(R.string.status_cpu_turn)
        tvStatus.setTextColor(getColor(R.color.player_o))
        mainHandler.postDelayed(cpuMoveRunnable, CPU_MOVE_DELAY_MS)
    }

    private fun updateCell(row: Int, col: Int) {
        val btn = cells[row][col]
        val player = board.cellAt(row, col)
        btn.text = player?.name ?: ""
        btn.setTextColor(
            getColor(
                when (player) {
                    Player.X -> R.color.player_x
                    Player.O -> R.color.player_o
                    null -> R.color.text_primary
                },
            ),
        )
        btn.contentDescription = if (player == null) {
            getString(R.string.cell_desc_empty, row + 1, col + 1)
        } else {
            getString(R.string.cell_desc_occupied, row + 1, col + 1, player.name)
        }
    }

    private fun updateBoardEnabled() {
        for (row in 0..2) {
            for (col in 0..2) {
                val btn = cells[row][col]
                val isEmpty = board.cellAt(row, col) == null
                // Hard-lock (disabled visual): game over, driving restriction, or occupied cell.
                val hardLocked = gameOver || isDrivingRestricted || !isEmpty
                btn.isEnabled = !hardLocked
                // Soft-lock during CPU thinking: block interaction without the gray disabled look.
                // The isCpuThinking guard in onCellClicked also prevents moves.
                btn.isClickable = !hardLocked && !isCpuThinking
            }
        }
        toggleMode.isEnabled = !isDrivingRestricted
    }

    private fun updateStatus() {
        if (gameMode == GameMode.VS_CPU) {
            tvStatus.text = getString(R.string.status_your_turn)
            tvStatus.setTextColor(getColor(R.color.text_primary))
        } else {
            tvStatus.text = getString(R.string.status_turn, currentPlayer.name)
            val colorRes = if (currentPlayer == Player.O) R.color.player_o else R.color.text_primary
            tvStatus.setTextColor(getColor(colorRes))
        }
    }

    private fun updateScore() {
        if (gameMode == GameMode.VS_CPU) {
            tvLabelX.text = getString(R.string.score_label_you)
            tvLabelO.text = getString(R.string.score_label_android)
        } else {
            tvLabelX.text = getString(R.string.score_label_x)
            tvLabelO.text = getString(R.string.score_label_o)
        }
        tvScoreX.text = winsX.toString()
        tvScoreO.text = winsO.toString()
        tvScoreDraws.text = draws.toString()
    }

    private fun clearBoard() {
        mainHandler.removeCallbacks(cpuMoveRunnable)
        mainHandler.removeCallbacks(autoClearBoardRunnable)
        mainHandler.removeCallbacks(winFlashRunnable)
        lastWinLine = null
        winFlashStep = 0
        isCpuThinking = false
        board.reset()
        currentPlayer = Player.X
        gameOver = false
        for (row in 0..2) for (col in 0..2) {
            updateCell(row, col)
            cells[row][col].setBackgroundResource(R.drawable.bg_cell)
        }
        updateBoardEnabled()
        updateStatus()
    }

    private fun highlightCells(line: List<Pair<Int, Int>>, highlight: Boolean) {
        val drawableRes = if (highlight) R.drawable.bg_cell_win else R.drawable.bg_cell
        line.forEach { (row, col) ->
            val btn = cells[row][col]
            // MaterialButton only renders setBackgroundResource() when enabled;
            // toggling isEnabled here is intentional to make the flash visible.
            // Clicks during the flash are harmless — onCellClicked guards on gameOver.
            btn.isEnabled = highlight
            btn.setBackgroundResource(drawableRes)
        }
    }

    private fun clearScore() {
        winsX = 0
        winsO = 0
        draws = 0
        updateScore()
        clearBoard()
    }

    // ── Constants ─────────────────────────────────────────────────────────────

    /** Activity-scoped constants. */
    companion object {
        private const val CPU_MOVE_DELAY_MS = 1_000L
        private const val AUTO_CLEAR_BOARD_DELAY_MS = 3_000L
        private const val WIN_FLASH_COUNT = 3
        private const val WIN_FLASH_PERIOD_MS = 350L
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
