package proton.android.pass.log.impl

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import proton.android.pass.log.api.PassLogger
import timber.log.Timber
import java.io.BufferedWriter
import java.io.File
import java.io.FileNotFoundException
import java.io.FileWriter
import java.io.IOException
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

class FileLoggingTree(private val context: Context) : Timber.Tree() {
    private val mutex = Mutex()
    private val dateTimeFormatter = DateTimeFormatter
        .ofPattern("yyyy-MM-dd hh:mm:ss.SSS", Locale.getDefault())
        .withZone(ZoneId.from(ZoneOffset.UTC))
    private val scope = CoroutineScope(SupervisorJob())

    init {
        try {
            val cacheDir = File(context.cacheDir, LOGS_DIR)
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            val cacheFile = File(cacheDir, LOGS_FILE)
            if (!cacheFile.exists()) {
                cacheFile.createNewFile()
            }
            if (shouldRotate(cacheFile)) {
                rotateLog(cacheDir, cacheFile)
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

    @SuppressLint("LogNotTimber")
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority < Log.INFO) return
        scope.launch(Dispatchers.IO) {
            try {
                val cacheFile = File(context.cacheDir, "$LOGS_DIR/$LOGS_FILE")
                if (cacheFile.exists()) {
                    mutex.withLock {
                        BufferedWriter(FileWriter(cacheFile, true))
                            .use { writer ->
                                writer.append(buildLog(priority, tag, message))
                                writer.newLine()
                                writer.flush()
                            }
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Could not write to log file", e)
            }
        }
    }

    private fun buildLog(priority: Int, tag: String?, message: String): String = buildString {
        append(dateTimeFormatter.format(Clock.System.now().toJavaInstant()))
        append(' ')
        append(priority.toPriorityChar())
        append(": ")
        append(tag ?: "EmptyTag")
        append(" - ")
        append(message.sanitise())
    }

    private fun String.sanitise(): String = split('/')
        .joinToString(separator = "/") {
            if (it.length == ID_LENGTH) {
                it.substring(0, ID_OFFSET) + "..." + it.substring(ID_LENGTH - ID_OFFSET)
            } else {
                it
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

    companion object {
        private const val TAG = "FileLoggingTree"
        private const val MAX_FILE_SIZE: Long = 4 * 1024 * 1024 // 4 MB
        private const val ROTATION_LINES: Long = 500
        private const val LOGS_DIR = "logs"
        private const val LOGS_FILE = "pass.log"
        private const val ID_LENGTH = 88
        private const val ID_OFFSET = 4
    }
}
