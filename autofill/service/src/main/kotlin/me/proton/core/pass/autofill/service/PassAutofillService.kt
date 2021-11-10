package me.proton.core.pass.autofill.service

import android.app.PendingIntent
import android.app.assist.AssistStructure
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.service.autofill.AutofillService
import android.service.autofill.Dataset
import android.service.autofill.FillCallback
import android.service.autofill.FillContext
import android.service.autofill.FillRequest
import android.service.autofill.FillResponse
import android.service.autofill.InlinePresentation
import android.service.autofill.SaveCallback
import android.service.autofill.SaveInfo
import android.service.autofill.SaveRequest
import android.widget.RemoteViews
import android.widget.inline.InlinePresentationSpec
import androidx.annotation.RequiresApi
import androidx.autofill.inline.UiVersions
import androidx.autofill.inline.v1.InlineSuggestionUi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.proton.core.pass.autofill.service.di.AutofillSaveActivityClass
import me.proton.core.pass.autofill.service.di.AutofillSearchActivityClass
import me.proton.core.pass.autofill.service.entities.AndroidAutofillFieldId
import me.proton.core.pass.autofill.service.entities.SearchCredentialsInfo
import me.proton.core.pass.autofill.service.entities.SecretSaveInfo
import me.proton.core.pass.autofill.service.entities.asAndroid
import me.proton.core.pass.autofill.service.util.toByteArray
import me.proton.core.util.kotlin.Logger
import java.util.concurrent.TimeoutException
import javax.inject.Inject
import kotlin.coroutines.coroutineContext
import kotlin.reflect.KClass

@AndroidEntryPoint
class PassAutofillService: AutofillService() {

    companion object {
        const val TAG = "PassAutofillService"
    }

    @Inject
    @AutofillSaveActivityClass
    lateinit var saveActivityClass: KClass<*>

    @Inject
    @AutofillSearchActivityClass
    lateinit var searchActivityClass: KClass<*>

    @Inject
    lateinit var logger: Logger

    override fun onFillRequest(
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback
    ) {
        logger.d(TAG, "Fill request received")

        // TODO: check if this can ever be null (probably not)
        val windowNode = getWindowNodes(request.fillContexts.last()).lastOrNull()
        if (windowNode == null) {
            callback.onFailure(getString(R.string.error_cant_find_matching_fields))
            return
        }

        val job = CoroutineScope(Dispatchers.IO).launch {
            searchAndFill(windowNode, request, callback)
        }

        cancellationSignal.setOnCancelListener {
            job.cancel()
        }
    }

    private suspend fun searchAndFill(
        windowNode: AssistStructure.WindowNode,
        request: FillRequest,
        callback: FillCallback
    ) {
        val assistFields = AssistNodeTraversal().traverse(windowNode.rootViewNode)

        val appPackageName = getApplicationPackageName(windowNode)
        val applicationName = getApplicationName(packageName)

        val listItemId = android.R.layout.simple_list_item_1

        val authenticateView = RemoteViews(packageName, listItemId).apply {
            setTextViewText(android.R.id.text1, getString(R.string.autofill_authenticate_prompt))
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            Constants.CODE_SEARCH_CREDENTIALS,
            Intent(this, searchActivityClass.java).apply {
                val searchCredentialsInfo = SearchCredentialsInfo(
                    appPackageName,
                    applicationName,
                    assistFields,
                )
                val extras = Bundle().apply { putByteArray(Constants.ARG_SEARCH_CREDENTIALS_INFO, searchCredentialsInfo.toByteArray()) }
                putExtras(extras)
            },
            PendingIntent.FLAG_CANCEL_CURRENT
        )

        // Single Dataset to force user authentication
        val dataset = Dataset.Builder(authenticateView)
            .apply {
                setAuthentication(pendingIntent.intentSender)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val inlineSuggestionSpecs = request.inlineSuggestionsRequest
                        ?.inlinePresentationSpecs.orEmpty()
                    for (spec in inlineSuggestionSpecs) {
                        addInlineSuggestion(this, spec, pendingIntent)
                    }
                }
                for (value in assistFields) {
                    setValue(value.id.asAndroid().autofillId, null)
                }
            }
            .build()

        if (!coroutineContext.isActive) {
            logger.e(TAG, TimeoutException())
            callback.onFailure(getString(R.string.error_credentials_not_found))
            return
        }

        val autofillIds = assistFields.map {
            (it.id as AndroidAutofillFieldId).autofillId
        }.toTypedArray()
        val saveInfo = SaveInfo.Builder(SaveInfo.SAVE_DATA_TYPE_GENERIC, autofillIds).build()
        val response = FillResponse.Builder()
            .addDataset(dataset)
            .setSaveInfo(saveInfo)
            .build()

        callback.onSuccess(response)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun addInlineSuggestion(
        builder: Dataset.Builder,
        spec: InlinePresentationSpec,
        pendingIntent: PendingIntent,
    ) {
        if (!UiVersions.getVersions(spec.style).contains(UiVersions.INLINE_UI_VERSION_1)) return
        val content = InlineSuggestionUi.newContentBuilder(pendingIntent)
            .setTitle(getString(R.string.autofill_authenticate_prompt))
            .build()

        val presentation = InlinePresentation(content.slice, spec, false)
        builder.setInlinePresentation(presentation)
    }

    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {
        logger.d(TAG, "Save request received")

        val windowNode = getWindowNodes(request.fillContexts.last()).lastOrNull()
        if (windowNode == null) {
            callback.onFailure(getString(R.string.error_cant_find_matching_fields))
            return
        }

        saveCredentials(windowNode, callback)
    }

    private fun saveCredentials(
        windowNode: AssistStructure.WindowNode,
        saveCallback: SaveCallback
    ) {
        val packageName = getApplicationPackageName(windowNode)
        val applicationName = getApplicationName(packageName)

        val fieldsToSave = AssistNodeTraversal().traverse(windowNode.rootViewNode)

        logger.d(TAG, "Saving credentials for $applicationName")

        runCatching {
            SecretSaveInfoFetcher().fetch(fieldsToSave, applicationName, packageName)
        }
            .onSuccess { saveInfos ->
                // TODO: allow saving several values to a single Secret
                saveInfos.firstOrNull()?.let { launchSaveSecret(it) }
                saveCallback.onSuccess()
            }
            .onFailure {
                logger.e(TAG, it)
                saveCallback.onFailure(getString(R.string.error_credentials_not_saved))
            }
    }

    private fun getApplicationPackageName(windowNode: AssistStructure.WindowNode): String {
        val wholePackageName = windowNode.title
        val packageComponents = wholePackageName.split("/")
        return packageComponents.first()
    }

    private fun getApplicationName(packageName: String): String {
        val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        return packageManager.getApplicationLabel(appInfo).toString()
    }

    private fun getWindowNodes(fillContext: FillContext): List<AssistStructure.WindowNode> {
        val structure: AssistStructure = fillContext.structure
        return if (structure.windowNodeCount > 0)
            (0 until structure.windowNodeCount).map { structure.getWindowNodeAt(it) } else
            emptyList()
    }

    private fun launchSaveSecret(secretSaveInfo: SecretSaveInfo) {
        val intent = Intent(this, saveActivityClass.java)
        intent.putExtra(Constants.ARG_SAVE_CREDENTIALS_SECRET, secretSaveInfo)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TASK or
                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
        startActivity(intent)
    }
}
