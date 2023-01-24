package proton.android.pass.appconfig.api

interface AppConfig {
    val isDebug: Boolean
    val applicationId: String
    val flavor: BuildFlavor
    val versionCode: Int
    val versionName: String
    val host: String
    val humanVerificationHost: String
    val proxyToken: String?
    val useDefaultPins: Boolean
    val sentryDSN: String?
}

enum class BuildEnv {
    BLACK,
    PROD
}

sealed class BuildFlavor(val env: BuildEnv) {
    class Dev(env: BuildEnv) : BuildFlavor(env)
    class Alpha(env: BuildEnv) : BuildFlavor(env)
    class Play(env: BuildEnv) : BuildFlavor(env)

    companion object {
        fun from(string: String): BuildFlavor = when (string) {
            "devBlack" -> Dev(BuildEnv.BLACK)
            "devProd" -> Dev(BuildEnv.PROD)
            "alphaBlack" -> Alpha(BuildEnv.BLACK)
            "alphaProd" -> Alpha(BuildEnv.PROD)
            "playBlack" -> Play(BuildEnv.BLACK)
            "playProd" -> Play(BuildEnv.PROD)
            else -> throw UnsupportedOperationException("Unsupported flavour")
        }

        fun BuildFlavor.toValue(): String = when (this) {
            is Dev -> when (this.env) {
                BuildEnv.BLACK -> "devBlack"
                BuildEnv.PROD -> "devProd"
            }
            is Alpha -> when (this.env) {
                BuildEnv.BLACK -> "alphaBlack"
                BuildEnv.PROD -> "alphaProd"
            }
            is Play -> when (this.env) {
                BuildEnv.BLACK -> "playBlack"
                BuildEnv.PROD -> "playProd"
            }
        }
    }
}
