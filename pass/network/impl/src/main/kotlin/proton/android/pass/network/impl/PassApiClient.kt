package proton.android.pass.network.impl

import android.os.Build
import me.proton.core.network.domain.ApiClient
import proton.android.pass.appconfig.api.AppConfig
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class PassApiClient @Inject constructor(appConfig: AppConfig) : ApiClient {
    override val appVersionHeader: String = "android-pass@${appConfig.versionName}"
    override val enableDebugLogging: Boolean = appConfig.isDebug
    override val shouldUseDoh: Boolean = false
    override val userAgent: String = StringBuilder()
        .append("ProtonPass/${appConfig.versionName}")
        .append(" ")
        .append("(")
        .append("Android ${Build.VERSION.RELEASE};")
        .append(" ")
        .append("${Build.MODEL} ${Build.BRAND}")
        .append(")")
        .toString()

    override fun forceUpdate(errorMessage: String) {
        PassLogger.i(TAG, errorMessage)
    }

    companion object {
        const val TAG = "PassApiClient"
    }
}
