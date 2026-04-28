/*
 * Copyright (c) 2023-2026 Proton AG
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

import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.items.ItemCategory

enum class EventItemType(val itemTypeName: String) {
    Login("login"),
    Note("note"),
    Alias("alias"),
    Password("password"),
    CreditCard("credit_card"),
    Identity("identity"),
    Custom("custom"),
    WifiNetwork("wifi_network"),
    SSHKey("ssh_key"),
    Unknown("unknown");

    companion object {
        fun from(itemType: ItemType): EventItemType = when (itemType) {
            ItemType.Unknown -> Unknown
            ItemType.Password -> Password
            is ItemType.Alias -> Alias
            is ItemType.Note -> Note
            is ItemType.Login -> Login
            is ItemType.CreditCard -> CreditCard
            is ItemType.Identity -> Identity
            is ItemType.Custom -> Custom
            is ItemType.SSHKey -> WifiNetwork
            is ItemType.WifiNetwork -> SSHKey
        }

        fun from(itemCategory: ItemCategory): EventItemType = when (itemCategory) {
            ItemCategory.Login -> Login
            ItemCategory.Alias -> Alias
            ItemCategory.Note -> Note
            ItemCategory.Password -> Password
            ItemCategory.CreditCard -> CreditCard
            ItemCategory.Identity -> Identity
            ItemCategory.Custom -> Custom
            ItemCategory.WifiNetwork -> WifiNetwork
            ItemCategory.SSHKey -> SSHKey
            ItemCategory.Unknown -> Unknown
        }

        fun from(itemContents: ItemContents): EventItemType = when (itemContents) {
            is ItemContents.Alias -> Alias
            is ItemContents.Login -> Login
            is ItemContents.Note -> Note
            is ItemContents.CreditCard -> CreditCard
            is ItemContents.Identity -> Identity
            is ItemContents.Custom -> Custom
            is ItemContents.WifiNetwork -> WifiNetwork
            is ItemContents.SSHKey -> SSHKey
            is ItemContents.Unknown -> Unknown
        }
    }
}

enum class TelemetryGrowthEventNames(val eventName: String) {

    Install("install"),
    Signup("signup"),
    Subscription("sub"),
    FeatureUsage("feature_usage"),
    Uninstall("uninstall"),
    Open("open"),
    OptOut("opt_out")
}

enum class TelemetryGrowthFeatureUsageAction(
    val actionName: String,
    val sendOncePerInstall: Boolean = false
) {
    AccountCreated("account_created"),
    SkippedOnboarding("skipped_onboarding"),
    CompletedOnboarding("completed_onboarding"),
    OfferServed("offer_served"),
    OfferClicked("offer_clicked"),
    InAppSubscriptionPaywall("in_app_subscription_paywall"),
    InAppSubscriptionOnboarding("in_app_subscription_onboarding"),
    InAppSubscriptionManual("in_app_subscription_manual"),
    ItemCreatedLogin("item_created_login", sendOncePerInstall = true),
    ItemCreatedAlias("item_created_alias", sendOncePerInstall = true),
    ItemCreatedPassword("item_created_password", sendOncePerInstall = true),
    ItemCreatedCreditCard("item_created_cc", sendOncePerInstall = true),
    VaultShared("vault_shared", sendOncePerInstall = true),
    ItemShared("item_shared", sendOncePerInstall = true),
    VaultCreated("vault_created", sendOncePerInstall = true)
}

sealed class TelemetryEvent(val eventName: String) {
    open fun dimensions(): Map<String, String> = emptyMap()

    @Suppress("UnnecessaryAbstractClass")
    abstract class DeferredTelemetryEvent(eventName: String) : TelemetryEvent(eventName)

    /**
     * DO NOT USE for new MMP/growth events. Prefer [LiveTelemetryGrowthEvent] which sends
     * immediately via the unauthenticated session — required for accurate Singular install
     * attribution (deferred 6h batch is too slow to fit Singular's attribution window).
     *
     * Kept only as an extension point if a future event genuinely needs the deferred 6h
     * pipeline. The plumbing (TelemetryGrowthEntity, TelemetryRepositoryImpl growth path,
     * 6h worker) remains in place for that case.
     */
    @Suppress("UnnecessaryAbstractClass")
    abstract class DeferredTelemetryGrowthEvent(eventName: String) : DeferredTelemetryEvent(eventName)

    @Suppress("UnnecessaryAbstractClass")
    abstract class LiveTelemetryEvent(eventName: String) : TelemetryEvent(eventName)

    @Suppress("UnnecessaryAbstractClass")
    abstract class LiveTelemetryGrowthEvent(eventName: String) : LiveTelemetryEvent(eventName)

}
