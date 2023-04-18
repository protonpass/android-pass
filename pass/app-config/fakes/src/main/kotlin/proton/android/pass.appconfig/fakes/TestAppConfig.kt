package proton.android.pass.appconfig.fakes

import proton.android.pass.appconfig.api.AppConfig
import proton.android.pass.appconfig.api.BuildEnv
import proton.android.pass.appconfig.api.BuildFlavor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestAppConfig @Inject constructor() : AppConfig {
    override val isDebug: Boolean
        get() = false
    override val applicationId: String
        get() = ""
    override val flavor: BuildFlavor
        get() = BuildFlavor.Play(BuildEnv.PROD)
    override val versionCode: Int
        get() = 0
    override val versionName: String
        get() = "0.0.0"
    override val host: String
        get() = ""
    override val humanVerificationHost: String
        get() = ""
    override val proxyToken: String
        get() = ""
    override val useDefaultPins: Boolean
        get() = true
    override val sentryDSN: String
        get() = ""
}
