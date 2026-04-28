/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.data.impl.requests

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("event_type")
sealed interface TelemetryGrowthEventRequest {
    val eventTimestampMs: Long
    val asid: String
    val appPackageName: String
    val sessionStartMs: Long?
    val openUri: String?
    val osVersion: String?
    val appVersion: String?
    val locale: String?
    val platform: String?
    val make: String?
    val model: String?
    val languageCode: String?
    val appIdentifier: String?
}

@Serializable
@SerialName("install")
data class TelemetryGrowthInstallEventRequest(
    @SerialName("event_timestamp_ms")
    override val eventTimestampMs: Long,
    @SerialName("asid")
    override val asid: String,
    @SerialName("app_package_name")
    override val appPackageName: String,
    @SerialName("session_start_ms")
    override val sessionStartMs: Long? = null,
    @SerialName("openuri")
    override val openUri: String? = null,
    @SerialName("os_version")
    override val osVersion: String? = null,
    @SerialName("app_version")
    override val appVersion: String? = null,
    @SerialName("locale")
    override val locale: String? = null,
    @SerialName("platform")
    override val platform: String? = "android",
    @SerialName("make")
    override val make: String? = null,
    @SerialName("model")
    override val model: String? = null,
    @SerialName("language_code")
    override val languageCode: String? = null,
    @SerialName("app_identifier")
    override val appIdentifier: String? = null,
    @SerialName("is_reinstall")
    val isReinstall: Boolean? = null,
    @SerialName("install_ref")
    val installRef: String? = null,
    @SerialName("install_receipt")
    val installReceipt: String? = null
) : TelemetryGrowthEventRequest

@Serializable
@SerialName("signup")
data class TelemetryGrowthSignupEventRequest(
    @SerialName("event_timestamp_ms")
    override val eventTimestampMs: Long,
    @SerialName("asid")
    override val asid: String,
    @SerialName("app_package_name")
    override val appPackageName: String,
    @SerialName("session_start_ms")
    override val sessionStartMs: Long? = null,
    @SerialName("openuri")
    override val openUri: String? = null,
    @SerialName("os_version")
    override val osVersion: String? = null,
    @SerialName("app_version")
    override val appVersion: String? = null,
    @SerialName("locale")
    override val locale: String? = null,
    @SerialName("platform")
    override val platform: String? = "android",
    @SerialName("make")
    override val make: String? = null,
    @SerialName("model")
    override val model: String? = null,
    @SerialName("language_code")
    override val languageCode: String? = null,
    @SerialName("app_identifier")
    override val appIdentifier: String? = null,
    @SerialName("registration_method")
    val registrationMethod: String? = null,
    @SerialName("referral_code")
    val referralCode: String? = null
) : TelemetryGrowthEventRequest

@Serializable
@SerialName("sub")
data class TelemetryGrowthSubscriptionEventRequest(
    @SerialName("event_timestamp_ms")
    override val eventTimestampMs: Long,
    @SerialName("asid")
    override val asid: String,
    @SerialName("app_package_name")
    override val appPackageName: String,
    @SerialName("session_start_ms")
    override val sessionStartMs: Long? = null,
    @SerialName("openuri")
    override val openUri: String? = null,
    @SerialName("os_version")
    override val osVersion: String? = null,
    @SerialName("app_version")
    override val appVersion: String? = null,
    @SerialName("locale")
    override val locale: String? = null,
    @SerialName("platform")
    override val platform: String? = "android",
    @SerialName("make")
    override val make: String? = null,
    @SerialName("model")
    override val model: String? = null,
    @SerialName("language_code")
    override val languageCode: String? = null,
    @SerialName("app_identifier")
    override val appIdentifier: String? = null,
    @SerialName("content_list")
    val contentList: List<String>? = null,
    @SerialName("price")
    val price: Double,
    @SerialName("currency")
    val currency: String,
    @SerialName("cycle")
    val cycle: Int,
    @SerialName("coupon_code")
    val couponCode: String? = null,
    @SerialName("transaction_id")
    val transactionId: String? = null,
    @SerialName("is_first_purchase")
    val isFirstPurchase: Boolean? = null,
    @SerialName("is_free_to_paid")
    val isFreeToPaid: Boolean? = null
) : TelemetryGrowthEventRequest

@Serializable
@SerialName("feature_usage")
data class TelemetryGrowthFeatureUsageEventRequest(
    @SerialName("event_timestamp_ms")
    override val eventTimestampMs: Long,
    @SerialName("asid")
    override val asid: String,
    @SerialName("app_package_name")
    override val appPackageName: String,
    @SerialName("session_start_ms")
    override val sessionStartMs: Long? = null,
    @SerialName("openuri")
    override val openUri: String? = null,
    @SerialName("os_version")
    override val osVersion: String? = null,
    @SerialName("app_version")
    override val appVersion: String? = null,
    @SerialName("locale")
    override val locale: String? = null,
    @SerialName("platform")
    override val platform: String? = "android",
    @SerialName("make")
    override val make: String? = null,
    @SerialName("model")
    override val model: String? = null,
    @SerialName("language_code")
    override val languageCode: String? = null,
    @SerialName("app_identifier")
    override val appIdentifier: String? = null,
    @SerialName("action")
    val action: String
) : TelemetryGrowthEventRequest

@Serializable
@SerialName("open")
data class TelemetryGrowthOpenEventRequest(
    @SerialName("event_timestamp_ms")
    override val eventTimestampMs: Long,
    @SerialName("asid")
    override val asid: String,
    @SerialName("app_package_name")
    override val appPackageName: String,
    @SerialName("session_start_ms")
    override val sessionStartMs: Long? = null,
    @SerialName("openuri")
    override val openUri: String? = null,
    @SerialName("os_version")
    override val osVersion: String? = null,
    @SerialName("app_version")
    override val appVersion: String? = null,
    @SerialName("locale")
    override val locale: String? = null,
    @SerialName("platform")
    override val platform: String? = "android",
    @SerialName("make")
    override val make: String? = null,
    @SerialName("model")
    override val model: String? = null,
    @SerialName("language_code")
    override val languageCode: String? = null,
    @SerialName("app_identifier")
    override val appIdentifier: String? = null
) : TelemetryGrowthEventRequest

@Serializable
@SerialName("uninstall")
data class TelemetryGrowthUninstallEventRequest(
    @SerialName("event_timestamp_ms")
    override val eventTimestampMs: Long,
    @SerialName("asid")
    override val asid: String,
    @SerialName("app_package_name")
    override val appPackageName: String,
    @SerialName("session_start_ms")
    override val sessionStartMs: Long? = null,
    @SerialName("openuri")
    override val openUri: String? = null,
    @SerialName("os_version")
    override val osVersion: String? = null,
    @SerialName("app_version")
    override val appVersion: String? = null,
    @SerialName("locale")
    override val locale: String? = null,
    @SerialName("platform")
    override val platform: String? = "android",
    @SerialName("make")
    override val make: String? = null,
    @SerialName("model")
    override val model: String? = null,
    @SerialName("language_code")
    override val languageCode: String? = null,
    @SerialName("app_identifier")
    override val appIdentifier: String? = null
) : TelemetryGrowthEventRequest

@Serializable
@SerialName("opt_out")
data class TelemetryGrowthOptOutEventRequest(
    @SerialName("event_timestamp_ms")
    override val eventTimestampMs: Long,
    @SerialName("asid")
    override val asid: String,
    @SerialName("app_package_name")
    override val appPackageName: String,
    @SerialName("session_start_ms")
    override val sessionStartMs: Long? = null,
    @SerialName("openuri")
    override val openUri: String? = null,
    @SerialName("os_version")
    override val osVersion: String? = null,
    @SerialName("app_version")
    override val appVersion: String? = null,
    @SerialName("locale")
    override val locale: String? = null,
    @SerialName("platform")
    override val platform: String? = "android",
    @SerialName("make")
    override val make: String? = null,
    @SerialName("model")
    override val model: String? = null,
    @SerialName("language_code")
    override val languageCode: String? = null,
    @SerialName("app_identifier")
    override val appIdentifier: String? = null
) : TelemetryGrowthEventRequest

@Serializable
data class TelemetryGrowthBatchRequest(
    @SerialName("Events")
    val events: List<TelemetryGrowthEventRequest>
)
