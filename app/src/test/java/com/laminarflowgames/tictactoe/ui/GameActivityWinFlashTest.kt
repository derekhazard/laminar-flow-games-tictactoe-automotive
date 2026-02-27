package com.laminarflowgames.tictactoe.ui

import android.widget.Button
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Verifies that [GameActivity]'s win-flash animation correctly aborts and
 * re-locks the board when a CarUxRestrictions driving restriction fires
 * while the flash is in progress.
 *
 * Uses Robolectric to run the Activity on the JVM so the Handler, View
 * lifecycle, and board state can be inspected without a device or emulator.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
internal class GameActivityWinFlashTest {

    /**
     * Returns a reflected, accessible [java.lang.reflect.Field] from [GameActivity]
     * by name.
     */
    private fun field(name: String) =
        GameActivity::class.java.getDeclaredField(name).also { it.isAccessible = true }

    @Test
    fun `win flash aborts and disables all cells when driving restriction fires mid-animation`() {
        val activity = Robolectric.buildActivity(GameActivity::class.java).setup().get()

        // Simulate state immediately after a win: gameOver set, winning line recorded.
        field("gameOver").setBoolean(activity, true)
        field("lastWinLine").set(activity, listOf(0 to 0, 0 to 1, 0 to 2))

        // Driving restriction fires while the flash runnable is about to execute.
        field("isDrivingRestricted").setBoolean(activity, true)

        // Run the flash runnable directly — equivalent to the Handler delivering it.
        (field("winFlashRunnable").get(activity) as Runnable).run()

        // The guard must have aborted the flash and locked every cell.
        @Suppress("UNCHECKED_CAST")
        val cells = field("cells").get(activity) as Array<Array<Button>>
        for (row in cells) {
            for (btn in row) {
                assertFalse("Cell must be disabled when driving is restricted", btn.isEnabled)
            }
        }
    }
}
