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

package proton.android.pass.autofill

import android.app.assist.AssistStructure
import android.content.Context
import android.service.autofill.FillContext
import android.service.autofill.SaveCallback
import android.service.autofill.SaveRequest
import me.proton.core.util.kotlin.orEmpty
import proton.android.pass.autofill.Utils.getApplicationPackageName
import proton.android.pass.autofill.Utils.getWindowNodes
import proton.android.pass.autofill.entities.SaveInformation
import proton.android.pass.autofill.entities.SaveItemType
import proton.android.pass.autofill.extensions.MultiStepUtils
import proton.android.pass.autofill.extensions.MultiStepUtils.getPasswordFromState
import proton.android.pass.autofill.extensions.MultiStepUtils.getUsernameFromState
import proton.android.pass.autofill.extensions.isBrowser
import proton.android.pass.autofill.heuristics.NodeExtractor
import proton.android.pass.autofill.heuristics.findChildById
import proton.android.pass.autofill.service.R
import proton.android.pass.autofill.ui.autosave.AutoSaveActivity
import proton.android.pass.autofill.ui.autosave.LinkedAppInfo
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.SpecialCharacters.DOT_SEPARATOR
import proton.android.pass.commonui.api.AndroidUtils.getApplicationName
import proton.android.pass.data.api.url.UrlSanitizer
import proton.android.pass.domain.entity.PackageName
import proton.android.pass.log.api.PassLogger

object AutoSaveHandler {

    private const val TAG = "AutoSaveHandler"

    fun handleOnSave(
        context: Context,
        request: SaveRequest,
        callback: SaveCallback
    ) {
        val usernameFromState = request.clientState?.getUsernameFromState()
        val passwordFromState = request.clientState?.getPasswordFromState()

        val usernameField = getField(usernameFromState, request)
        val passwordField = getField(passwordFromState, request)

        logAutosavePackageNames(request.fillContexts)

        val windowNode = getWindowNodes(request.fillContexts).lastOrNull()
        if (windowNode?.rootViewNode == null) {
            PassLogger.w(TAG, "WindowNode.rootViewNode is null")
            callback.onFailure(context.getString(R.string.error_cant_find_matching_fields))
            return
        }

        if (usernameField == null && passwordField == null) {
            PassLogger.w(TAG, "Username and password fields were null")
            callback.onFailure(context.getString(R.string.error_cant_find_matching_fields))
            return
        }

        runCatching {
            saveCredentials(
                context = context,
                windowNode = windowNode,
                usernameField = usernameField,
                passwordField = passwordField
            )
        }.onSuccess {
            callback.onSuccess()
        }.onFailure {
            callback.onFailure(context.getString(R.string.error_credentials_not_saved))
        }
    }

    @Suppress("ReturnCount")
    private fun getField(field: MultiStepUtils.SaveFieldInfo?, request: SaveRequest): AssistStructure.ViewNode? {
        if (field == null) return null
        val fillContext = request.fillContexts.firstOrNull { it.requestId == field.sessionId }
        if (fillContext == null) {
            PassLogger.w(TAG, "No fill context found for session id ${field.sessionId}")
            return null
        }

        val structure = fillContext.structure
        val windowNodes = if (structure.windowNodeCount > 0) {
            (0 until structure.windowNodeCount).map { structure.getWindowNodeAt(it) }
        } else {
            return null
        }

        windowNodes.forEach { windowNode ->
            val child = windowNode.rootViewNode.findChildById(field.fieldId)
            if (child != null) {
                return child
            }
        }

        PassLogger.w(TAG, "Could not find child with id ${field.fieldId}")
        return null
    }


    private fun saveCredentials(
        context: Context,
        windowNode: AssistStructure.WindowNode,
        usernameField: AssistStructure.ViewNode?,
        passwordField: AssistStructure.ViewNode?
    ) {
        val assistInfo = NodeExtractor().extract(windowNode.rootViewNode)
        val infoUrl = assistInfo.mainUrl()

        val packageName = getApplicationPackageName(windowNode)

        val itemTitle = getItemTitle(context, packageName, infoUrl)

        val usernameValue: String = usernameField?.autofillValue?.textValue.orEmpty()
        val passwordValue: String = passwordField?.autofillValue?.textValue.orEmpty().let {
            // Sometimes masked values can be the dot character (•) and the last one might not be masked.
            // This can happen if the input field uses a masking character for all but the last character,
            // which might be left unmasked to indicate the end of the input or for other UI/UX reasons.
            if (it.dropLast(1).all { char -> char == DOT_SEPARATOR }) "" else it
        }

        val saveInfo = when {
            usernameValue.isNotBlank() && passwordValue.isNotBlank() -> {
                SaveInformation(
                    itemType = SaveItemType.Login(
                        identity = usernameValue,
                        password = passwordValue
                    )
                )
            }
            usernameValue.isNotBlank() && passwordValue.isBlank() -> {
                SaveInformation(SaveItemType.Username(usernameValue))
            }
            usernameValue.isBlank() && passwordValue.isNotBlank() -> {
                SaveInformation(SaveItemType.Password(passwordValue))
            }
            else -> return
        }

        val linkedAppInfo = if (PackageName(packageName).isBrowser()) {
            null
        } else {
            val appName = getApplicationName(context, packageName).value() ?: ""
            LinkedAppInfo(packageName, appName)
        }

        launchSaveCredentialScreen(
            context = context,
            saveInformation = saveInfo,
            title = itemTitle,
            website = infoUrl,
            linkedAppInfo = linkedAppInfo
        )
    }

    private fun getItemTitle(
        context: Context,
        packageName: String,
        url: Option<String>
    ): String = if (BROWSERS.contains(packageName)) {
        when (url) {
            None -> ""
            is Some -> UrlSanitizer.getDomain(url.value).fold(
                onSuccess = { it },
                onFailure = { "" }
            )
        }
    } else {
        getApplicationName(context, packageName).value() ?: ""
    }

    private fun launchSaveCredentialScreen(
        context: Context,
        saveInformation: SaveInformation,
        linkedAppInfo: LinkedAppInfo?,
        title: String,
        website: Option<String>
    ) {
        val intent = AutoSaveActivity.newIntent(
            context = context,
            saveInformation = saveInformation,
            title = title,
            website = website.value(),
            linkedAppInfo = linkedAppInfo
        )
        context.startActivity(intent)
    }

    private fun logAutosavePackageNames(fillContexts: List<FillContext>) {
        val windowNodes = getWindowNodes(fillContexts)
        val packageNames = windowNodes.joinToString(", ") { getApplicationPackageName(it) }
        PassLogger.i(TAG, "Received autosave request for packageNames [$packageNames]")
    }
}
