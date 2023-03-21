package proton.android.pass.featuresettings.impl

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.appconfig.api.AppConfig
import proton.android.pass.log.api.PassLogger
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class LogViewViewModel @Inject constructor(
    private val appConfig: AppConfig
) : ViewModel() {

    private val _state: MutableStateFlow<String> = MutableStateFlow("")
    val state: StateFlow<String> = _state

    fun loadLogFile(context: Context) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val cacheDir = File(context.cacheDir, "logs")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            val cacheFile = File(cacheDir, "pass.log")
            if (cacheFile.exists()) {
                cacheFile.bufferedReader().use { br ->
                    _state.update {
                        br.readLines()
                            .takeLast(TAIL_LOG_SIZE)
                            .joinToString("\n")
                    }
                }
            }
        } catch (e: IOException) {
            PassLogger.e(TAG, e, "Could not read log file")
        } catch (e: FileNotFoundException) {
            PassLogger.e(TAG, e, "Could not find log file")
        }
    }

    fun startShareIntent(context: Context) = viewModelScope.launch(Dispatchers.IO) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "*/*"
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("pass@proton.me"))
        intent.putExtra(Intent.EXTRA_SUBJECT, "Proton Pass: Share Logs")
        val cacheFile = File(context.cacheDir, "logs/pass.log")
        val contentUri: Uri = FileProvider.getUriForFile(
            context,
            "${appConfig.applicationId}.fileprovider",
            cacheFile
        )
        intent.putExtra(Intent.EXTRA_STREAM, contentUri)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        context.startActivity(Intent.createChooser(intent, "Share log"))
    }

    companion object {
        private const val TAIL_LOG_SIZE = 100
        private const val TAG = "LogViewViewModel"
    }
}
