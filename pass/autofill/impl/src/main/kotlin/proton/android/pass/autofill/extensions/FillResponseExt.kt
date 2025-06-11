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

package proton.android.pass.autofill.extensions

import android.os.Build
import android.os.Bundle
import android.service.autofill.FillRequest
import android.service.autofill.FillResponse
import android.service.autofill.SaveInfo
import proton.android.pass.autofill.RequestFlags
import proton.android.pass.autofill.ThirdPartyModeProvider
import proton.android.pass.autofill.entities.AssistField
import proton.android.pass.autofill.entities.asAndroid
import proton.android.pass.autofill.extensions.MultiStepUtils.addPasswordToState
import proton.android.pass.autofill.extensions.MultiStepUtils.addUsernameToState
import proton.android.pass.autofill.extensions.MultiStepUtils.getUsernameFromState
import proton.android.pass.autofill.heuristics.NodeCluster
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
import proton.android.pass.domain.entity.PackageName
import proton.android.pass.log.api.PassLogger

const val TAG = "FillResponseExt"

// List of apps which we don't want to offer autosave
val FORBIDDEN_AUTOSAVE_PACKAGE_NAMES = setOf(
    "com.android.settings"
)

sealed interface SaveSessionType {
    @JvmInline
    value class Username(val field: AssistField) : SaveSessionType

    data class Password(
        val storedUsername: Option<MultiStepUtils.SaveFieldInfo>,
        val passwordField: AssistField
    ) : SaveSessionType

    data class UsernameAndPassword(
        val usernameField: AssistField,
        val passwordField: AssistField
    ) : SaveSessionType

    data object NotAutoSaveable : SaveSessionType
}

@Suppress("LongMethod")
internal fun FillResponse.Builder.addSaveInfo(
    request: FillRequest,
    cluster: NodeCluster,
    packageName: PackageName,
    thirdPartyModeProvider: ThirdPartyModeProvider
) {
    val currentClientState = request.clientState ?: Bundle()
    val autofillSessionId = request.id
    val isBrowser3PEnabled = runCatching {
        thirdPartyModeProvider.isThirdPartyModeEnabled(packageName.value)
    }.fold(
        onSuccess = { it },
        onFailure = {
            PassLogger.d(TAG, it, "Failed to check if 3P mode is enabled")
            false
        }
    )
    val saveSessionType = getSaveSessionType(cluster, currentClientState, packageName)
    val saveInfo = when (saveSessionType) {
        SaveSessionType.NotAutoSaveable -> return
        is SaveSessionType.UsernameAndPassword -> {
            PassLogger.d(TAG, "UsernameAndPassword session")
            val usernameFieldId = saveSessionType.usernameField.id.asAndroid().autofillId
            val passwordFieldId = saveSessionType.passwordField.id.asAndroid().autofillId
            currentClientState.addUsernameToState(autofillSessionId, usernameFieldId)
            currentClientState.addPasswordToState(autofillSessionId, passwordFieldId)
            setClientState(currentClientState)
            val type = SaveInfo.SAVE_DATA_TYPE_USERNAME or SaveInfo.SAVE_DATA_TYPE_PASSWORD
            val requiredIds = arrayOf(usernameFieldId, passwordFieldId)
            SaveInfo.Builder(type, requiredIds)
                .apply { if (!isBrowser3PEnabled) applyFlags(request.flags) }
        }

        is SaveSessionType.Username -> {
            PassLogger.d(TAG, "Only username session")

            val usernameFieldId = saveSessionType.field.id.asAndroid().autofillId
            currentClientState.addUsernameToState(autofillSessionId, usernameFieldId)
            setClientState(currentClientState)

            val type = SaveInfo.SAVE_DATA_TYPE_USERNAME or SaveInfo.SAVE_DATA_TYPE_EMAIL_ADDRESS
            val requiredIds = arrayOf(usernameFieldId)
            val builder = SaveInfo.Builder(type, requiredIds)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PassLogger.d(TAG, "Adding FLAG_DELAY_SAVE")
                builder.setFlags(SaveInfo.FLAG_DELAY_SAVE)
            }
            builder
        }

        is SaveSessionType.Password -> {
            PassLogger.d(TAG, "Only password session")

            val passwordFieldId = saveSessionType.passwordField.id.asAndroid().autofillId
            val requiredIds = mutableListOf(passwordFieldId)
            saveSessionType.storedUsername.map { usernameInfo ->
                requiredIds.add(usernameInfo.fieldId)
                currentClientState.addUsernameToState(usernameInfo.sessionId, usernameInfo.fieldId)
                PassLogger.d(TAG, "Also added username to state")
            }
            currentClientState.addPasswordToState(autofillSessionId, passwordFieldId)

            // Determine which flags to send to SaveInfo. These flags are used for the system
            // message that prompts the user to save their credentials.
            val saveInfoType = if (saveSessionType.storedUsername.isEmpty()) {
                SaveInfo.SAVE_DATA_TYPE_PASSWORD
            } else {
                SaveInfo.SAVE_DATA_TYPE_USERNAME or SaveInfo.SAVE_DATA_TYPE_PASSWORD
            }

            setClientState(currentClientState)
            SaveInfo.Builder(saveInfoType, requiredIds.toTypedArray())
                .apply { if (!isBrowser3PEnabled) applyFlags(request.flags) }
        }
    }

    setSaveInfo(saveInfo.build())
}

private fun getSaveSessionType(
    cluster: NodeCluster,
    currentClientState: Bundle,
    packageName: PackageName
): SaveSessionType {
    if (FORBIDDEN_AUTOSAVE_PACKAGE_NAMES.contains(packageName.value)) {
        return SaveSessionType.NotAutoSaveable
    }

    val isBrowser = packageName.isBrowser()
    return when (cluster) {
        NodeCluster.Empty -> SaveSessionType.NotAutoSaveable
        is NodeCluster.CreditCard -> SaveSessionType.NotAutoSaveable
        is NodeCluster.SignUp -> {
            SaveSessionType.UsernameAndPassword(
                usernameField = cluster.username,
                passwordField = cluster.password
            )
        }

        is NodeCluster.Login -> when (cluster) {
            is NodeCluster.Login.OnlyPassword -> if (isBrowser) {
                PassLogger.d(TAG, "Not adding saveInfo because is only password and is browser")
                SaveSessionType.NotAutoSaveable
            } else {
                val usernameField = currentClientState.getUsernameFromState().toOption()
                SaveSessionType.Password(
                    storedUsername = usernameField,
                    passwordField = cluster.password
                )
            }

            is NodeCluster.Login.OnlyUsername -> if (isBrowser) {
                PassLogger.d(TAG, "Not adding saveInfo because is only username and is browser")
                SaveSessionType.NotAutoSaveable
            } else {
                SaveSessionType.Username(cluster.username)
            }

            is NodeCluster.Login.UsernameAndPassword -> {
                SaveSessionType.UsernameAndPassword(
                    usernameField = cluster.username,
                    passwordField = cluster.password
                )
            }
        }

        is NodeCluster.Identity -> SaveSessionType.NotAutoSaveable
    }
}

private fun SaveInfo.Builder.applyFlags(flags: Int): SaveInfo.Builder = apply {
    // From official docs we should set FLAG_SAVE_ON_ALL_VIEWS_INVISIBLE if save was triggered in compatibility mode.
    // https://developer.android.com/reference/android/service/autofill/AutofillService#CompatibilityMode
    if (RequestFlags.FLAG_COMPATIBILITY_MODE_REQUEST in RequestFlags.fromValue(flags)) {
        setFlags(SaveInfo.FLAG_SAVE_ON_ALL_VIEWS_INVISIBLE)
    }
}
