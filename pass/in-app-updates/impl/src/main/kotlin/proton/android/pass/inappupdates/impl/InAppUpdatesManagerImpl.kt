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

package proton.android.pass.inappupdates.impl

import android.content.Context
import android.content.IntentSender
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import proton.android.pass.inappupdates.api.InAppUpdateState
import proton.android.pass.inappupdates.api.InAppUpdatesManager
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.InternalSettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InAppUpdatesManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val internalSettingsRepository: InternalSettingsRepository
) : InAppUpdatesManager {

    private val appUpdateManager = AppUpdateManagerFactory.create(context)
    private val inAppUpdateState = MutableStateFlow<InAppUpdateState>(InAppUpdateState.Idle)
    private val availableVersionCode = MutableStateFlow("")

    val listener = InstallStateUpdatedListener { state ->
        when (state.installStatus()) {
            InstallStatus.DOWNLOADING -> {
                val totalBytesToDownload = state.totalBytesToDownload()
                val bytesDownloaded = state.bytesDownloaded()
                val progress = if (totalBytesToDownload != 0L) {
                    bytesDownloaded.toFloat() / totalBytesToDownload.toFloat()
                } else {
                    0f
                }
                inAppUpdateState.tryEmit(InAppUpdateState.Downloading(progress))
            }

            InstallStatus.DOWNLOADED -> inAppUpdateState.tryEmit(InAppUpdateState.Downloaded)
            InstallStatus.CANCELED -> inAppUpdateState.tryEmit(InAppUpdateState.Cancelled)
            InstallStatus.FAILED -> inAppUpdateState.tryEmit(InAppUpdateState.Failed)
            InstallStatus.INSTALLED -> inAppUpdateState.tryEmit(InAppUpdateState.Installed)
            InstallStatus.INSTALLING -> inAppUpdateState.tryEmit(InAppUpdateState.Installing)
            InstallStatus.PENDING -> inAppUpdateState.tryEmit(InAppUpdateState.Pending)
            else -> inAppUpdateState.tryEmit(InAppUpdateState.Unknown)
        }
    }

    override fun checkForUpdates(launcher: ActivityResultLauncher<IntentSenderRequest>) {
        appUpdateManager.registerListener(listener)
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
                availableVersionCode.tryEmit(appUpdateInfo.availableVersionCode().toString())
                onSuccess(appUpdateInfo, appUpdateManager, launcher)
            }
            .addOnCanceledListener { appUpdateManager.unregisterListener(listener) }
            .addOnFailureListener {
                appUpdateManager.unregisterListener(listener)
                PassLogger.w(TAG, it, "checkForUpdates failed")
            }
    }

    override fun completeUpdate() {
        if (inAppUpdateState.value !is InAppUpdateState.Downloaded) return
        appUpdateManager.completeUpdate()
    }

    override fun checkUpdateStalled() {
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    inAppUpdateState.tryEmit(InAppUpdateState.Downloaded)
                }
            }
    }

    override fun observeInAppUpdateState(): Flow<InAppUpdateState> = inAppUpdateState

    override fun declineUpdate() {
        internalSettingsRepository.setDeclinedUpdateVersion(availableVersionCode.value)
    }

    override fun tearDown() {
        appUpdateManager.unregisterListener(listener)
    }

    private fun onSuccess(
        appUpdateInfo: AppUpdateInfo,
        appUpdateManager: AppUpdateManager,
        launcher: ActivityResultLauncher<IntentSenderRequest>
    ) {
        val declinedUpdate = runBlocking {
            internalSettingsRepository.getDeclinedUpdateVersion().firstOrNull() ?: ""
        }
        if (shouldUpdate(appUpdateInfo, declinedUpdate)) {
            try {
                val updateOptions = AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                appUpdateManager.startUpdateFlowForResult(appUpdateInfo, launcher, updateOptions)
            } catch (e: IntentSender.SendIntentException) {
                PassLogger.w(TAG, e, "startUpdateFlowForResult failed")
            }
        }
    }

    @Suppress("UnnecessaryParentheses")
    private fun shouldUpdate(appUpdateInfo: AppUpdateInfo, declinedVersionCode: String): Boolean =
        appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
            (appUpdateInfo.clientVersionStalenessDays() ?: -1) >= DAYS_FOR_FLEXIBLE_UPDATE &&
            appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) &&
            !hasUpdateBeenDeclined(
                availableVersionCode = appUpdateInfo.availableVersionCode().toString(),
                declinedVersionCode = declinedVersionCode
            )

    private fun hasUpdateBeenDeclined(
        availableVersionCode: String,
        declinedVersionCode: String
    ): Boolean = declinedVersionCode.isNotBlank() && availableVersionCode == declinedVersionCode

    companion object {
        private const val DAYS_FOR_FLEXIBLE_UPDATE = 7
        private const val TAG = "InAppUpdatesManagerImpl"
    }
}
