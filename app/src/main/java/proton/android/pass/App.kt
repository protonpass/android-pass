/*
 * Copyright (c) 2023 Proton AG
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
import android.app.Application
import android.os.Bundle
import coil.ImageLoader
import coil.ImageLoaderFactory
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import proton.android.pass.inappreview.api.InAppReviewTriggerMetrics
import proton.android.pass.initializer.MainInitializer
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.HasAuthenticated
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject
import javax.inject.Provider

@HiltAndroidApp
class App : Application(), ImageLoaderFactory {
    @Inject
    lateinit var imageLoader: Provider<ImageLoader>

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    @Inject
    lateinit var inAppReviewTriggerMetrics: InAppReviewTriggerMetrics

    override fun newImageLoader(): ImageLoader = imageLoader.get()

    override fun onCreate() {
        super.onCreate()
        MainInitializer.init(this)
        userPreferencesRepository.setHasAuthenticated(HasAuthenticated.NotAuthenticated)

        val currentHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(PassExceptionHandler(currentHandler))

        registerActivityLifecycleCallbacks(
            activityLifecycleCallbacks(
                onActivityCreated = { activity, _ ->
                    CoroutineScope(Dispatchers.IO).launch {
                        inAppReviewTriggerMetrics.incrementAppLaunchStreakCount()
                    }
                    PassLogger.i(TAG, "Created activity ${activity::class.java.simpleName}")
                },
                onActivityResumed = { activity ->
                    PassLogger.i(TAG, "Resumed activity ${activity::class.java.simpleName}")
                },
                onActivityPaused = { activity ->
                    PassLogger.i(TAG, "Paused activity ${activity::class.java.simpleName}")
                }
            )
        )
    }

    private fun activityLifecycleCallbacks(
        onActivityCreated: (activity: Activity, savedInstanceState: Bundle?) -> Unit = { _, _ -> },
        onActivityStarted: (activity: Activity) -> Unit = {},
        onActivityResumed: (activity: Activity) -> Unit = {},
        onActivityPaused: (activity: Activity) -> Unit = {},
        onActivityStopped: (activity: Activity) -> Unit = {},
        onActivitySaveInstanceState: (activity: Activity, outState: Bundle) -> Unit = { _, _ -> },
        onActivityDestroyed: (activity: Activity) -> Unit = {}
    ) = object : ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            onActivityCreated(activity, savedInstanceState)
        }

        override fun onActivityStarted(activity: Activity) {
            onActivityStarted(activity)
        }

        override fun onActivityResumed(activity: Activity) {
            onActivityResumed(activity)
        }

        override fun onActivityPaused(activity: Activity) {
            onActivityPaused(activity)
        }

        override fun onActivityStopped(activity: Activity) {
            onActivityStopped(activity)
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            onActivitySaveInstanceState(activity, outState)
        }

        override fun onActivityDestroyed(activity: Activity) {
            onActivityDestroyed(activity)
        }
    }

    companion object {
        private const val TAG = "App"
    }
}
