/*
 * Copyright (c) 2026 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass

import android.app.Activity
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class ForegroundActivityTrackerTest {

    private lateinit var tracker: ForegroundActivityTracker

    @Before
    fun setUp() {
        tracker = ForegroundActivityTracker(isSingleInstance = { (it as TestActivity).singleInstance })
    }

    @Test
    fun `returns null when no activity has been resumed`() {
        assertThat(tracker.launchSourceActivity).isNull()
    }

    @Test
    fun `returns the resumed activity`() {
        val activity = TestActivity()

        tracker.onActivityResumed(activity)

        assertThat(tracker.launchSourceActivity).isSameInstanceAs(activity)
    }

    @Test
    fun `returns the most recently resumed activity`() {
        val first = TestActivity()
        val second = TestActivity()
        tracker.onActivityResumed(first)
        tracker.onActivityResumed(second)

        tracker.onActivityResumed(first)

        assertThat(tracker.launchSourceActivity).isSameInstanceAs(first)
    }

    @Test
    fun `skips a single instance activity and returns the one below it`() {
        val below = TestActivity()
        val singleInstance = TestActivity(singleInstance = true)
        tracker.onActivityResumed(below)

        tracker.onActivityResumed(singleInstance)

        assertThat(tracker.launchSourceActivity).isSameInstanceAs(below)
    }

    @Test
    fun `returns null when only single instance activities are alive`() {
        tracker.onActivityResumed(TestActivity(singleInstance = true))

        assertThat(tracker.launchSourceActivity).isNull()
    }

    @Test
    fun `skips a finishing activity`() {
        val below = TestActivity()
        val finishing = TestActivity()
        tracker.onActivityResumed(below)
        tracker.onActivityResumed(finishing)

        finishing.finishing = true

        assertThat(tracker.launchSourceActivity).isSameInstanceAs(below)
    }

    @Test
    fun `forgets a destroyed activity`() {
        val below = TestActivity()
        val destroyed = TestActivity()
        tracker.onActivityResumed(below)
        tracker.onActivityResumed(destroyed)

        tracker.onActivityDestroyed(destroyed)

        assertThat(tracker.launchSourceActivity).isSameInstanceAs(below)
    }

    @Test
    fun `keeps the top activity when another activity is destroyed`() {
        val below = TestActivity()
        val top = TestActivity()
        tracker.onActivityResumed(below)
        tracker.onActivityResumed(top)

        tracker.onActivityDestroyed(below)

        assertThat(tracker.launchSourceActivity).isSameInstanceAs(top)
    }

    private class TestActivity(val singleInstance: Boolean = false) : Activity() {
        var finishing: Boolean = false

        override fun isFinishing(): Boolean = finishing
        override fun isDestroyed(): Boolean = false
    }
}
