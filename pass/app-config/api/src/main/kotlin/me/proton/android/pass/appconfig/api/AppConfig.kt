package me.proton.android.pass.appconfig.api

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

enum class BuildFlavor {
    DEV,
    ALPHA,
    PROD;

    companion object {
        fun from(string: String): BuildFlavor = when (string) {
            "dev" -> DEV
            "alpha" -> ALPHA
            "prod" -> PROD
            else -> throw UnsupportedOperationException("Unsupported flavour")
        }

        fun BuildFlavor.toValue(): String = when (this) {
            DEV -> "dev"
            ALPHA -> "alpha"
            PROD -> "prod"
        }
    }
}
