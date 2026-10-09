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

package proton.android.pass.uitest.robot

import android.app.Activity
import android.os.SystemClock
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import org.junit.Assert.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

object ActivityStackRobot {

    private val pollInterval = 100.milliseconds

    fun awaitResumed(activityClass: Class<out Activity>, timeout: Duration = 30.seconds): ActivityStackRobot = apply {
        val deadline = SystemClock.uptimeMillis() + timeout.inWholeMilliseconds
        while (resumedActivities().none(activityClass::isInstance)) {
            check(SystemClock.uptimeMillis() < deadline) { "${activityClass.simpleName} was not resumed in $timeout" }
            SystemClock.sleep(pollInterval.inWholeMilliseconds)
        }
    }

    fun assertSameTask(
        expected: Class<out Activity>,
        actual: Class<out Activity>
    ): ActivityStackRobot = apply {
        val taskIds = liveActivityTaskIds()
        val expectedTaskId = requireNotNull(taskIds[expected]) { "${expected.simpleName} is not alive" }
        val actualTaskId = requireNotNull(taskIds[actual]) { "${actual.simpleName} is not alive" }
        assertEquals(
            "${actual.simpleName} opened in a separate task from ${expected.simpleName}",
            expectedTaskId,
            actualTaskId
        )
    }

    private fun resumedActivities(): Collection<Activity> = onMainThread {
        ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)
    }

    private fun liveActivityTaskIds(): Map<Class<out Activity>, Int> = onMainThread {
        val monitor = ActivityLifecycleMonitorRegistry.getInstance()
        Stage.entries
            .filterNot { it == Stage.DESTROYED }
            .flatMap(monitor::getActivitiesInStage)
            .associate { it::class.java to it.taskId }
    }

    private fun <T> onMainThread(block: () -> T): T {
        var result: Result<T>? = null
        InstrumentationRegistry.getInstrumentation().runOnMainSync { result = runCatching(block) }
        return requireNotNull(result).getOrThrow()
    }
}
