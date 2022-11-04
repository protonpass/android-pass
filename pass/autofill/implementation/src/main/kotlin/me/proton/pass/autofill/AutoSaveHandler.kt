package me.proton.pass.autofill

import android.app.assist.AssistStructure
import android.content.Context
import android.service.autofill.SaveCallback
import android.service.autofill.SaveRequest
import me.proton.pass.autofill.Utils.getApplicationName
import me.proton.pass.autofill.Utils.getApplicationPackageName
import me.proton.pass.autofill.Utils.getWindowNodes
import me.proton.pass.autofill.entities.SaveInformation
import me.proton.pass.autofill.service.R
import me.proton.pass.autofill.ui.autosave.AutosaveActivity

object AutoSaveHandler {
    fun handleOnSave(context: Context, request: SaveRequest, callback: SaveCallback) {
        val windowNode = getWindowNodes(request.fillContexts.last()).lastOrNull()
        if (windowNode?.rootViewNode == null) {
            callback.onFailure(context.getString(R.string.error_cant_find_matching_fields))
            return
        }

        runCatching {
            saveCredentials(context, windowNode)
        }.onSuccess {
            callback.onSuccess()
        }.onFailure {
            callback.onFailure(context.getString(R.string.error_credentials_not_saved))
        }
    }

    private fun saveCredentials(
        context: Context,
        windowNode: AssistStructure.WindowNode
    ) {
        val assistInfo = AssistNodeTraversal().traverse(windowNode.rootViewNode)

        val packageName = getApplicationPackageName(windowNode)
        val applicationName = getApplicationName(context, packageName)
        val saveInformations = SaveFieldExtractor.extract(
            assistInfo.fields,
            packageName,
            applicationName
        )

        // We should handle what happens if there are multiple credentials
        saveInformations.firstOrNull()?.let { launchSaveCredentialScreen(context, it) }
    }

    private fun launchSaveCredentialScreen(context: Context, saveInformation: SaveInformation) {
        val intent = AutosaveActivity.newIntent(context, saveInformation)
        context.startActivity(intent)
    }
}
