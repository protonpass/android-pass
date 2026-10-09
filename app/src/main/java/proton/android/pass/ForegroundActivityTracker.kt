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
import android.content.ComponentName
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import proton.android.pass.log.api.PassLogger
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ForegroundActivityTracker internal constructor(
    private val isSingleInstance: (Activity) -> Boolean
) {

    @Volatile
    private var activities: List<WeakReference<Activity>> = emptyList()

    val launchSourceActivity: Activity?
        get() = activities.asReversed().firstNotNullOfOrNull { reference ->
            reference.get()?.takeUnless { it.isFinishing || it.isDestroyed || isSingleInstance(it) }
        }

    @Inject
    constructor() : this(::isSingleInstanceActivity)

    fun onActivityResumed(activity: Activity) {
        activities = activities.without(activity) + WeakReference(activity)
    }

    fun onActivityDestroyed(activity: Activity) {
        activities = activities.without(activity)
    }

    private fun List<WeakReference<Activity>>.without(activity: Activity) = filterNot { reference ->
        reference.get().let { it == null || it === activity }
    }
}

private fun isSingleInstanceActivity(activity: Activity): Boolean =
    activity.packageManager.isSingleInstance(activity.componentName)

internal fun PackageManager.launchModeOf(component: ComponentName): Result<Int> = runCatching {
    getActivityInfo(component, PackageManager.MATCH_DISABLED_COMPONENTS).launchMode
}

internal fun PackageManager.isSingleInstance(component: ComponentName): Boolean = launchModeOf(component)
    .onFailure {
        PassLogger.e(TAG, "Unable to read launch mode of ${component.flattenToShortString()}")
        PassLogger.e(TAG, it)
    }
    .map { it == ActivityInfo.LAUNCH_SINGLE_INSTANCE }
    .getOrDefault(false)

private const val TAG = "ForegroundActivityTracker"
