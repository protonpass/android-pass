/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

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
    val accountSentryDSN: String?
    val androidVersion: Int
    val allowScreenshotsDefaultValue: Boolean
}

enum class BuildEnv {
    BLACK,
    PROD
}

sealed class BuildFlavor(val env: BuildEnv) {
    class Dev(env: BuildEnv) : BuildFlavor(env)
    class Alpha(env: BuildEnv) : BuildFlavor(env)
    class Play(env: BuildEnv) : BuildFlavor(env)
    class Fdroid(env: BuildEnv) : BuildFlavor(env)
    class Quest(env: BuildEnv) : BuildFlavor(env)

    companion object {
        fun from(string: String): BuildFlavor = when (string) {
            "devBlack" -> Dev(BuildEnv.BLACK)
            "devProd" -> Dev(BuildEnv.PROD)
            "alphaBlack" -> Alpha(BuildEnv.BLACK)
            "alphaProd" -> Alpha(BuildEnv.PROD)
            "playBlack" -> Play(BuildEnv.BLACK)
            "playProd" -> Play(BuildEnv.PROD)
            "fdroidBlack" -> Fdroid(BuildEnv.BLACK)
            "fdroidProd" -> Fdroid(BuildEnv.PROD)
            "questBlack" -> Quest(BuildEnv.BLACK)
            "questProd" -> Quest(BuildEnv.PROD)
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

            is Fdroid -> when (this.env) {
                BuildEnv.BLACK -> "fdroidBlack"
                BuildEnv.PROD -> "fdroidProd"
            }

            is Quest -> when (this.env) {
                BuildEnv.BLACK -> "questBlack"
                BuildEnv.PROD -> "questProd"
            }
        }

        fun BuildFlavor.supportPayment() = this !is Fdroid && this !is Quest

        fun BuildFlavor.isQuest() = this is Quest
    }
}
