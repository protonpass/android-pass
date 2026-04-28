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

package proton.android.pass.data.impl.util

import kotlinx.serialization.json.JsonPrimitive
import proton.android.pass.data.impl.requests.TelemetryGrowthEventRequest
import proton.android.pass.data.impl.requests.TelemetryGrowthFeatureUsageEventRequest
import proton.android.pass.data.impl.requests.TelemetryGrowthInstallEventRequest
import proton.android.pass.data.impl.requests.TelemetryGrowthOpenEventRequest
import proton.android.pass.data.impl.requests.TelemetryGrowthOptOutEventRequest
import proton.android.pass.data.impl.requests.TelemetryGrowthSignupEventRequest
import proton.android.pass.data.impl.requests.TelemetryGrowthSubscriptionEventRequest
import proton.android.pass.data.impl.requests.TelemetryGrowthUninstallEventRequest
import proton.android.pass.telemetry.api.TelemetryGrowthDeviceInfoProvider
import proton.android.pass.telemetry.api.TelemetryGrowthEventNames
import proton.android.pass.telemetry.api.TelemetryGrowthFeatureUsageEvent
import proton.android.pass.telemetry.api.TelemetryGrowthInstallEvent
import proton.android.pass.telemetry.api.TelemetryGrowthSignupEvent
import proton.android.pass.telemetry.api.TelemetryGrowthSubEvent

@SuppressWarnings("LongMethod", "CascadingCallWrapping", "ReturnCount")
fun buildTelemetryGrowthEventRequest(
    eventType: TelemetryGrowthEventNames,
    timestampMs: Long,
    dimensions: Map<String, JsonPrimitive>,
    asid: String,
    info: TelemetryGrowthDeviceInfoProvider
): TelemetryGrowthEventRequest? {
    // openUri always null, we dont have any deeplink for now
    return when (eventType) {
        TelemetryGrowthEventNames.Install -> TelemetryGrowthInstallEventRequest(
            eventTimestampMs = timestampMs,
            asid = asid,
            appPackageName = info.appPackageName,
            openUri = null,
            osVersion = info.osVersion,
            appVersion = info.appVersion,
            locale = info.locale,
            make = info.make,
            model = info.model,
            languageCode = info.languageCode,
            appIdentifier = info.appIdentifier,
            isReinstall = dimensions[TelemetryGrowthInstallEvent.KEY_IS_REINSTALL]
                ?.content
                ?.toBooleanStrictOrNull(),
            installRef = dimensions[TelemetryGrowthInstallEvent.KEY_INSTALL_REF]
                ?.content,
            installReceipt = dimensions[TelemetryGrowthInstallEvent.KEY_INSTALL_RECEIPT]
                ?.content
        )

        TelemetryGrowthEventNames.Signup -> TelemetryGrowthSignupEventRequest(
            eventTimestampMs = timestampMs,
            asid = asid,
            appPackageName = info.appPackageName,
            openUri = null,
            osVersion = info.osVersion,
            appVersion = info.appVersion,
            locale = info.locale,
            make = info.make,
            model = info.model,
            languageCode = info.languageCode,
            appIdentifier = info.appIdentifier,
            registrationMethod = dimensions[TelemetryGrowthSignupEvent.KEY_REGISTRATION_METHOD]
                ?.content,
            referralCode = dimensions[TelemetryGrowthSignupEvent.KEY_REFERRAL_CODE]?.content
        )

        TelemetryGrowthEventNames.Subscription -> TelemetryGrowthSubscriptionEventRequest(
            eventTimestampMs = timestampMs,
            asid = asid,
            appPackageName = info.appPackageName,
            openUri = null,
            osVersion = info.osVersion,
            appVersion = info.appVersion,
            locale = info.locale,
            make = info.make,
            model = info.model,
            languageCode = info.languageCode,
            appIdentifier = info.appIdentifier,
            contentList = dimensions[TelemetryGrowthSubEvent.KEY_CONTENT_LIST]?.content
                ?.split(",")
                ?.filter { it.isNotEmpty() },
            price = dimensions[TelemetryGrowthSubEvent.KEY_PRICE]
                ?.content
                ?.toDoubleOrNull()
                ?: return null,
            currency = dimensions[TelemetryGrowthSubEvent.KEY_CURRENCY]
                ?.content
                ?: return null,
            cycle = dimensions[TelemetryGrowthSubEvent.KEY_CYCLE]
                ?.content?.toIntOrNull()
                ?: return null,
            couponCode = dimensions[TelemetryGrowthSubEvent.KEY_COUPON_CODE]
                ?.content,
            transactionId = dimensions[TelemetryGrowthSubEvent.KEY_TRANSACTION_ID]
                ?.content,
            isFirstPurchase = dimensions[TelemetryGrowthSubEvent.KEY_IS_FIRST_PURCHASE]
                ?.content
                ?.toBooleanStrictOrNull(),
            isFreeToPaid = dimensions[TelemetryGrowthSubEvent.KEY_IS_FREE_TO_PAID]
                ?.content
                ?.toBooleanStrictOrNull()
        )

        TelemetryGrowthEventNames.FeatureUsage -> TelemetryGrowthFeatureUsageEventRequest(
            eventTimestampMs = timestampMs,
            asid = asid,
            appPackageName = info.appPackageName,
            openUri = null,
            osVersion = info.osVersion,
            appVersion = info.appVersion,
            locale = info.locale,
            make = info.make,
            model = info.model,
            languageCode = info.languageCode,
            appIdentifier = info.appIdentifier,
            action = dimensions[TelemetryGrowthFeatureUsageEvent.KEY_ACTION]
                ?.content
                ?: return null
        )

        TelemetryGrowthEventNames.Open -> TelemetryGrowthOpenEventRequest(
            eventTimestampMs = timestampMs,
            asid = asid,
            appPackageName = info.appPackageName,
            sessionStartMs = timestampMs,
            openUri = null,
            osVersion = info.osVersion,
            appVersion = info.appVersion,
            locale = info.locale,
            make = info.make,
            model = info.model,
            languageCode = info.languageCode,
            appIdentifier = info.appIdentifier
        )

        TelemetryGrowthEventNames.Uninstall -> TelemetryGrowthUninstallEventRequest(
            eventTimestampMs = timestampMs,
            asid = asid,
            appPackageName = info.appPackageName,
            openUri = null,
            osVersion = info.osVersion,
            appVersion = info.appVersion,
            locale = info.locale,
            make = info.make,
            model = info.model,
            languageCode = info.languageCode,
            appIdentifier = info.appIdentifier
        )

        TelemetryGrowthEventNames.OptOut -> TelemetryGrowthOptOutEventRequest(
            eventTimestampMs = timestampMs,
            asid = asid,
            appPackageName = info.appPackageName,
            openUri = null,
            osVersion = info.osVersion,
            appVersion = info.appVersion,
            locale = info.locale,
            make = info.make,
            model = info.model,
            languageCode = info.languageCode,
            appIdentifier = info.appIdentifier
        )
    }
}
