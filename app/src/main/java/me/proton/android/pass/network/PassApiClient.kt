package me.proton.android.pass.network

import android.os.Build
import me.proton.android.pass.BuildConfig
import me.proton.core.network.domain.ApiClient
import java.util.*
import javax.inject.Inject

class PassApiClient @Inject constructor(): ApiClient {
    // TODO: Change to AndroidPass when possible
    override val appVersionHeader: String = "AndroidDrive_4.0.0"
    override val enableDebugLogging: Boolean = true
    override val shouldUseDoh: Boolean = false
    override val userAgent: String = StringBuilder()
        .append("ProtonPass/${BuildConfig.VERSION_NAME}")
        .append("(")
        .append("Android ${ Build.VERSION.RELEASE };")
        .append("${ Build.MODEL };")
        .append("${ Build.BRAND };")
        .append("${ Build.DEVICE };")
        .append(Locale.getDefault().language)
        .append(")")
        .toString()

    override fun forceUpdate(errorMessage: String) {
        TODO("Not yet implemented")
    }
}