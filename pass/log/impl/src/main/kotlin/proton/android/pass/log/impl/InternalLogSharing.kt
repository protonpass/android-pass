package proton.android.pass.log.impl

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Process
import androidx.core.content.FileProvider
import proton.android.pass.log.api.LogSharing
import proton.android.pass.log.api.PassLogger
import java.io.File
import java.io.IOException
import javax.inject.Inject

class InternalLogSharing @Inject constructor() : LogSharing {
    override fun shareLogs(applicationId: String, context: Context) {
        try {
            val folder = File(context.filesDir, "logs")
            folder.mkdir()
            val filename = File(folder, "logs.log")
            filename.delete()
            filename.createNewFile()
            val cmd = "logcat -d --pid=${Process.myPid()} -f" + filename.absolutePath
            Runtime.getRuntime().exec(cmd)
            val contentUri: Uri =
                FileProvider.getUriForFile(
                    context,
                    "$applicationId.fileprovider",
                    filename
                )
            val intent = Intent()
            intent.data = contentUri
            intent.putExtra(Intent.EXTRA_STREAM, contentUri)
            intent.action = Intent.ACTION_SEND
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            context.startActivity(intent)
        } catch (e: IOException) {
            PassLogger.e(TAG, e)
        }
    }

    companion object {
        private const val TAG = "InternalLogSharing"
    }
}
