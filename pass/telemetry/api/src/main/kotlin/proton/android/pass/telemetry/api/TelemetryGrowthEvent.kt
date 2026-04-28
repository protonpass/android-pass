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

package proton.android.pass.telemetry.api

import proton.android.pass.telemetry.api.TelemetryEvent.LiveTelemetryGrowthEvent

data class TelemetryGrowthInstallEvent(
    val isReinstall: Boolean = false,
    val installRef: String? = null,
    val installReceipt: String? = null
) : LiveTelemetryGrowthEvent(TelemetryGrowthEventNames.Install.eventName) {
    override fun dimensions(): Map<String, String> = buildMap {
        put(KEY_IS_REINSTALL, isReinstall.toString())
        installRef?.let { put(KEY_INSTALL_REF, it) }
        installReceipt?.let { put(KEY_INSTALL_RECEIPT, it) }
    }

    companion object {
        const val KEY_IS_REINSTALL = "is_reinstall"
        const val KEY_INSTALL_REF = "install_ref"
        const val KEY_INSTALL_RECEIPT = "install_receipt"
    }
}

data class TelemetryGrowthSignupEvent(
    val registrationMethod: String? = null,
    val referralCode: String? = null
) : LiveTelemetryGrowthEvent(TelemetryGrowthEventNames.Signup.eventName) {
    override fun dimensions(): Map<String, String> = buildMap {
        registrationMethod?.let { put(KEY_REGISTRATION_METHOD, it) }
        referralCode?.let { put(KEY_REFERRAL_CODE, it) }
    }

    companion object {
        const val KEY_REGISTRATION_METHOD = "registration_method"
        const val KEY_REFERRAL_CODE = "referral_code"
    }
}

data class TelemetryGrowthSubEvent(
    val contentList: List<String>? = null,
    val price: Double,
    val currency: String,
    val cycle: Int,
    val couponCode: String? = null,
    val transactionId: String? = null,
    val isFirstPurchase: Boolean? = null,
    val isFreeToPaid: Boolean? = null
) : LiveTelemetryGrowthEvent(TelemetryGrowthEventNames.Subscription.eventName) {
    override fun dimensions(): Map<String, String> = buildMap {
        contentList?.let { put(KEY_CONTENT_LIST, it.joinToString(",")) }
        put(KEY_PRICE, price.toString())
        put(KEY_CURRENCY, currency)
        put(KEY_CYCLE, cycle.toString())
        couponCode?.let { put(KEY_COUPON_CODE, it) }
        transactionId?.let { put(KEY_TRANSACTION_ID, it) }
        isFirstPurchase?.let { put(KEY_IS_FIRST_PURCHASE, it.toString()) }
        isFreeToPaid?.let { put(KEY_IS_FREE_TO_PAID, it.toString()) }
    }

    companion object {
        const val KEY_CONTENT_LIST = "content_list"
        const val KEY_PRICE = "price"
        const val KEY_CURRENCY = "currency"
        const val KEY_CYCLE = "cycle"
        const val KEY_COUPON_CODE = "coupon_code"
        const val KEY_TRANSACTION_ID = "transaction_id"
        const val KEY_IS_FIRST_PURCHASE = "is_first_purchase"
        const val KEY_IS_FREE_TO_PAID = "is_free_to_paid"
    }
}

data class TelemetryGrowthFeatureUsageEvent(
    val action: TelemetryGrowthFeatureUsageAction
) : LiveTelemetryGrowthEvent(TelemetryGrowthEventNames.FeatureUsage.eventName) {
    override fun dimensions(): Map<String, String> = mapOf(KEY_ACTION to action.actionName)

    companion object {
        const val KEY_ACTION = "action"
    }
}

data object TelemetryGrowthOpenEvent : LiveTelemetryGrowthEvent(TelemetryGrowthEventNames.Open.eventName)

data object TelemetryGrowthOptOutEvent : LiveTelemetryGrowthEvent(TelemetryGrowthEventNames.OptOut.eventName)
