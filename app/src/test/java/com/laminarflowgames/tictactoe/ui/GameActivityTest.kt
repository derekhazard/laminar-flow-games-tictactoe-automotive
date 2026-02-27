package com.laminarflowgames.tictactoe.ui

import android.widget.Button
import android.widget.TextView
import android.widget.ToggleButton
import com.laminarflowgames.tictactoe.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * Integration tests for [GameActivity] covering two-player gameplay, CPU move
 * scheduling, mode toggling, and driving-restriction cell locking.
 *
 * Uses Robolectric so the Activity lifecycle, Handler callbacks, and view state
 * can be exercised on the JVM without a device or emulator.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
internal class GameActivityTest {

    private lateinit var activity: GameActivity
    private lateinit var cells: Array<Array<Button>>
    private lateinit var tvStatus: TextView

    private fun field(name: String) =
        GameActivity::class.java.getDeclaredField(name).also { it.isAccessible = true }

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(GameActivity::class.java).setup().get()

        @Suppress("UNCHECKED_CAST")
        cells = field("cells").get(activity) as Array<Array<Button>>
        tvStatus = activity.findViewById(R.id.tv_status)
    }

    // ── Two-player full game ────────────────────────────────────────────────

    @Test
    fun `two-player game shows winner status on top-row X win`() {
        // Switch to two-player mode.
        activity.findViewById<ToggleButton>(R.id.toggle_mode).isChecked = true

        // X plays top row, O plays middle row (incomplete).
        //   X | X | X
        //   O | O |
        //     |   |
        cells[0][0].performClick() // X (0,0)
        cells[1][0].performClick() // O (1,0)
        cells[0][1].performClick() // X (0,1)
        cells[1][1].performClick() // O (1,1)
        cells[0][2].performClick() // X (0,2) — wins

        assertEquals("X", cells[0][0].text.toString())
        assertEquals("X", cells[0][1].text.toString())
        assertEquals("X", cells[0][2].text.toString())

        val expected = activity.getString(R.string.status_winner, "X")
        assertEquals(expected, tvStatus.text.toString())
    }

    @Test
    fun `two-player draw shows draw status`() {
        activity.findViewById<ToggleButton>(R.id.toggle_mode).isChecked = true

        // Draw pattern (alternating X, O):
        //   X | O | X
        //   X | X | O
        //   O | X | O
        cells[0][0].performClick() // X
        cells[0][1].performClick() // O
        cells[0][2].performClick() // X
        cells[1][0].performClick() // O
        cells[1][1].performClick() // X
        cells[2][0].performClick() // O
        cells[1][2].performClick() // X
        cells[2][2].performClick() // O
        cells[2][1].performClick() // X — draw

        val expected = activity.getString(R.string.status_draw)
        assertEquals(expected, tvStatus.text.toString())
    }

    // ── CPU move scheduling ─────────────────────────────────────────────────

    @Test
    fun `CPU move fires after delay in single-player mode`() {
        // Default mode is VS_CPU; X is human, O is CPU.
        // Human plays centre cell.
        cells[1][1].performClick()

        // CPU move is posted with a 1-second delay — not yet executed.
        assertTrue(field("isCpuThinking").getBoolean(activity))

        // Advance the Robolectric scheduler past the CPU delay.
        shadowOf(android.os.Looper.getMainLooper()).idleFor(
            java.time.Duration.ofMillis(1_100),
        )

        // CPU should have placed its move (O appears on the board).
        assertFalse(field("isCpuThinking").getBoolean(activity))
        val oCount = (0..2).sumOf { r ->
            (0..2).count { c -> cells[r][c].text.toString() == "O" }
        }
        assertEquals("CPU should have placed exactly one O", 1, oCount)
    }

    // ── Mode toggle ─────────────────────────────────────────────────────────

    @Test
    fun `mode toggle clears board and score`() {
        // Play a move in single-player mode, then toggle to two-player.
        cells[0][0].performClick() // X places
        assertEquals("X", cells[0][0].text.toString())

        val tvScoreX: TextView = activity.findViewById(R.id.tv_score_x)
        // Score not yet incremented (game not over), but board has a move.

        activity.findViewById<ToggleButton>(R.id.toggle_mode).isChecked = true

        // Board should be cleared.
        assertEquals("", cells[0][0].text.toString())

        // Score labels should switch to "X" / "O" (two-player labels).
        val tvLabelX: TextView = activity.findViewById(R.id.tv_label_x)
        assertEquals(activity.getString(R.string.score_label_x), tvLabelX.text.toString())

        // Scores should be zero.
        assertEquals("0", tvScoreX.text.toString())
    }

    // ── Driving restriction ─────────────────────────────────────────────────

    @Test
    fun `driving restriction disables all cells and mode toggle`() {
        field("isDrivingRestricted").setBoolean(activity, true)

        // Trigger updateBoardEnabled via reflection (same as uxListener → runOnUiThread).
        val updateMethod = GameActivity::class.java.getDeclaredMethod("updateBoardEnabled")
        updateMethod.isAccessible = true
        updateMethod.invoke(activity)

        for (row in cells) {
            for (btn in row) {
                assertFalse("Cell must be disabled when driving", btn.isEnabled)
            }
        }
        assertFalse(
            "Mode toggle must be disabled when driving",
            activity.findViewById<ToggleButton>(R.id.toggle_mode).isEnabled,
        )
    }
}
