package me.proton.android.pass.ui

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.net.Uri
import androidx.core.content.FileProvider.getUriForFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.proton.android.pass.BuildConfig
import me.proton.android.pass.log.PassLogger
import me.proton.android.pass.notifications.api.SnackbarMessageRepository
import me.proton.android.pass.preferences.PreferenceRepository
import me.proton.pass.common.api.asResultWithoutLoading
import me.proton.pass.common.api.onError
import me.proton.pass.common.api.onSuccess
import java.io.File
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class InternalDrawerViewModel @Inject constructor(
    private val preferenceRepository: PreferenceRepository,
    private val snackbarMessageRepository: SnackbarMessageRepository
) : ViewModel() {

    fun clearPreferences() = viewModelScope.launch {
        preferenceRepository.clearPreferences()
            .asResultWithoutLoading()
            .collect { result ->
                result
                    .onSuccess {
                        snackbarMessageRepository
                            .emitSnackbarMessage(InternalDrawerSnackbarMessage.PreferencesCleared)
                    }
                    .onError {
                        val message = "Error clearing preferences"
                        PassLogger.e(TAG, it ?: Exception(message), message)
                        snackbarMessageRepository
                            .emitSnackbarMessage(InternalDrawerSnackbarMessage.PreferencesClearError)
                    }
            }
    }

    fun shareLogCatOutput(context: Context) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val folder = File(context.filesDir, "logs")
            folder.mkdir()
            val filename = File(folder, "logs.log")
            filename.delete()
            filename.createNewFile()
            val cmd = "logcat -d --pid=${android.os.Process.myPid()} -f" + filename.absolutePath
            Runtime.getRuntime().exec(cmd)
            val contentUri: Uri =
                getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", filename)
            val intent = Intent()
            intent.data = contentUri
            intent.putExtra(Intent.EXTRA_STREAM, contentUri)
            intent.action = Intent.ACTION_SEND
            intent.flags = FLAG_GRANT_READ_URI_PERMISSION
            context.startActivity(intent)
        } catch (e: IOException) {
            PassLogger.e(TAG, e)
        }
    }

    companion object {
        private const val TAG = "InternalDrawerViewModel"
    }

}
