package me.proton.android.pass

import me.proton.android.pass.appconfig.api.AppConfig
import me.proton.android.pass.appconfig.api.BuildFlavor
import javax.inject.Inject

class PassAppConfig @Inject constructor() : AppConfig {
    override val isDebug: Boolean = BuildConfig.DEBUG
    override val applicationId: String = BuildConfig.APPLICATION_ID
    override val flavor: BuildFlavor = BuildFlavor.from(BuildConfig.FLAVOR)
    override val versionCode: Int = BuildConfig.VERSION_CODE
    override val versionName: String = BuildConfig.VERSION_NAME
    override val host: String = BuildConfig.HOST
    override val humanVerificationHost: String = BuildConfig.HUMAN_VERIFICATION_HOST
    override val proxyToken: String? = BuildConfig.PROXY_TOKEN
    override val useDefaultPins: Boolean = BuildConfig.USE_DEFAULT_PINS
    override val sentryDSN: String? = BuildConfig.SENTRY_DSN.takeIf { !BuildConfig.DEBUG }
}
