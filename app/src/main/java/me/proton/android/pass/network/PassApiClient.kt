package me.proton.android.pass.network

import android.os.Build
import java.util.Locale
import javax.inject.Inject
import me.proton.android.pass.BuildConfig
import me.proton.core.network.domain.ApiClient

class PassApiClient @Inject constructor() : ApiClient {
    override val appVersionHeader: String = "android-pass@${BuildConfig.VERSION_NAME}"
    override val enableDebugLogging: Boolean = true
    override val shouldUseDoh: Boolean = false
    override val userAgent: String = StringBuilder()
        .append("ProtonPass/${BuildConfig.VERSION_NAME}")
        .append("(")
        .append("Android ${Build.VERSION.RELEASE};")
        .append("${Build.MODEL};")
        .append("${Build.BRAND};")
        .append("${Build.DEVICE};")
        .append(Locale.getDefault().language)
        .append(")")
        .toString()

    override fun forceUpdate(errorMessage: String) {
        TODO("Not yet implemented")
    }
}
