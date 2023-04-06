package proton.android.pass.clipboard.impl

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipDescription.MIMETYPE_TEXT_PLAIN
import android.content.Context
import android.os.Build
import android.os.PersistableBundle
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.clipboard.api.CouldNotAccessClipboard
import proton.android.pass.clipboard.api.CouldNotGetClipboardContent
import proton.android.pass.clipboard.api.EmptyClipboardContent
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.ClearClipboardPreference
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject
import android.content.ClipboardManager as AndroidClipboardManager

class ClipboardManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    preferencesRepository: UserPreferencesRepository,
    private val scheduler: ClearClipboardScheduler
) : ClipboardManager {

    private val clearClipboardPreferenceFlow = preferencesRepository.getClearClipboardPreference()

    @Suppress("MagicNumber")
    override fun copyToClipboard(text: String, isSecure: Boolean) {
        val androidClipboard = context.getSystemService(AndroidClipboardManager::class.java)
        if (androidClipboard == null) {
            PassLogger.i(TAG, "Could not get ClipboardManager")
            return
        }

        val clipData = ClipData.newPlainText("pass-contents", text)
        if (isSecure) {
            applySecureFlag(clipData)
        }
        runBlocking(Dispatchers.IO) {
            androidClipboard.setPrimaryClip(clipData)
        }
        when (runBlocking { clearClipboardPreferenceFlow.first() }) {
            ClearClipboardPreference.Never -> {}
            ClearClipboardPreference.S69 -> scheduler.schedule(69, text)
            ClearClipboardPreference.S180 -> scheduler.schedule(180, text)
        }
    }

    override fun getClipboardContent(): Result<String> {
        val androidClipboard = context.getSystemService(AndroidClipboardManager::class.java)
        if (androidClipboard == null) {
            PassLogger.i(TAG, "Could not get ClipboardManager")
            return Result.failure(CouldNotAccessClipboard())
        }
        if (!androidClipboard.hasPrimaryClip() ||
            androidClipboard.primaryClipDescription?.hasMimeType(MIMETYPE_TEXT_PLAIN) != true
        ) {
            PassLogger.i(TAG, "Could not get clipboard content")
            return Result.failure(CouldNotGetClipboardContent())
        }

        return androidClipboard.primaryClip?.getItemAt(0)?.text?.toString()
            ?.let { Result.success(it) }
            ?: Result.failure(EmptyClipboardContent())
    }

    private fun applySecureFlag(clipData: ClipData) {
        val key = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ClipDescription.EXTRA_IS_SENSITIVE
        } else {
            "android.content.extra.IS_SENSITIVE"
        }

        clipData.description.extras = PersistableBundle().apply {
            putBoolean(key, true)
        }
    }

    companion object {
        private const val TAG = "ClipboardManagerImpl"
    }
}
