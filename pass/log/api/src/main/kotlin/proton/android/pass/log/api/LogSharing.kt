package proton.android.pass.log.api

import android.content.Context

interface LogSharing {
    fun shareLogs(applicationId: String, context: Context)
}
