package me.proton.android.pass.network.impl

import android.os.Build
import me.proton.android.pass.appconfig.api.AppConfig
import me.proton.android.pass.log.PassLogger
import me.proton.core.network.domain.ApiClient
import java.util.Locale
import javax.inject.Inject

class PassApiClient @Inject constructor(appConfig: AppConfig) : ApiClient {
    override val appVersionHeader: String = "android-pass@${appConfig.versionName}"
    override val enableDebugLogging: Boolean = true
    override val shouldUseDoh: Boolean = false
    override val userAgent: String = StringBuilder()
        .append("ProtonPass/${appConfig.versionName}")
        .append("(")
        .append("Android ${Build.VERSION.RELEASE};")
        .append("${Build.MODEL};")
        .append("${Build.BRAND};")
        .append("${Build.DEVICE};")
        .append(Locale.getDefault().language)
        .append(")")
        .toString()

    override fun forceUpdate(errorMessage: String) {
        PassLogger.i(TAG, errorMessage)
    }

    companion object {
        const val TAG = "PassApiClient"
    }
}
