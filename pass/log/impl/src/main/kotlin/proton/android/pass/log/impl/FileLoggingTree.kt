package proton.android.pass.log.impl

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import proton.android.pass.log.api.PassLogger
import timber.log.Timber
import java.io.BufferedWriter
import java.io.File
import java.io.FileNotFoundException
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileLoggingTree(context: Context) : Timber.Tree() {

    private var writer: BufferedWriter? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS", Locale.getDefault())
    private val scope = CoroutineScope(SupervisorJob())

    init {
        try {
            val cacheDir = File(context.cacheDir, "logs")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            val cacheFile = File(cacheDir, "pass.log")
            if (!cacheFile.exists()) {
                cacheFile.createNewFile()
            }
            if (shouldRotate(cacheFile)) {
                rotateLog(cacheDir, cacheFile)
            }
            if (cacheFile.exists()) {
                writer = BufferedWriter(FileWriter(cacheFile, true))
            }
        } catch (e: IOException) {
            PassLogger.e(TAG, e, "Could not create log file")
        }
    }

    private fun shouldRotate(logFile: File) = logFile.length() >= MAX_FILE_SIZE

    private fun rotateLog(cacheDir: File, cacheFile: File) {
        runBlocking(Dispatchers.IO) {
            try {
                val temporaryFile = File.createTempFile("temp.log", null, cacheDir)
                cacheFile.copyTo(temporaryFile, overwrite = true)
                var lines = temporaryFile.bufferedReader().use {
                    it.lines().count()
                }
                temporaryFile.bufferedReader().use { br ->
                    cacheFile.bufferedWriter().use { bw ->
                        br.forEachLine {
                            if (lines < ROTATION_LINES) {
                                bw.appendLine(it)
                            }
                            lines--
                        }
                        bw.flush()
                    }
                }
                temporaryFile.delete()
            } catch (e: IOException) {
                PassLogger.e(TAG, e, "Could not rotate file")
            } catch (e: FileNotFoundException) {
                PassLogger.e(TAG, e, "Could not find log file")
            }
        }
    }

    private fun Int.toPriorityChar(): Char = when (this) {
        Log.VERBOSE -> 'V'
        Log.DEBUG -> 'D'
        Log.INFO -> 'I'
        Log.WARN -> 'W'
        Log.ERROR -> 'E'
        Log.ASSERT -> 'A'
        else -> '-'
    }

    @SuppressLint("LogNotTimber")
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val wr = writer ?: return
        if (priority >= Log.INFO) {
            val log = buildString {
                append(dateFormat.format(Date()))
                append(' ')
                append(priority.toPriorityChar())
                append(": ")
                append(tag ?: "EmptyTag")
                append(" - ")
                append(message)
            }

            scope.launch(Dispatchers.IO) {
                try {
                    wr.append(log)
                    wr.newLine()
                    wr.flush()
                } catch (e: IOException) {
                    Log.e(TAG, "Could not write to log file", e)
                }
            }
        }
    }

    companion object {
        private const val TAG = "FileLoggingTree"
        private const val MAX_FILE_SIZE: Long = 4 * 1024 * 1024 // 4 MB
        private const val ROTATION_LINES: Long = 500
    }
}
