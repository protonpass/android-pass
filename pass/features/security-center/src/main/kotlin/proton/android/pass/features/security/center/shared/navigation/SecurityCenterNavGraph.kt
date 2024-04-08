/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.features.security.center.shared.navigation

import androidx.navigation.NavGraphBuilder
import proton.android.pass.domain.features.PaidFeature
import proton.android.pass.features.security.center.home.navigation.SecurityCenterHomeNavDestination
import proton.android.pass.features.security.center.home.navigation.SecurityCenterHomeNavItem
import proton.android.pass.features.security.center.home.ui.SecurityCenterHomeScreen
import proton.android.pass.features.security.center.missingtfa.navigation.SecurityCenterMissingTFADestination
import proton.android.pass.features.security.center.missingtfa.navigation.SecurityCenterMissingTFANavItem
import proton.android.pass.features.security.center.missingtfa.ui.SecurityCenterMissingTfaScreen
import proton.android.pass.features.security.center.reusepass.navigation.SecurityCenterReusedPassDestination
import proton.android.pass.features.security.center.reusepass.navigation.SecurityCenterReusedPassNavItem
import proton.android.pass.features.security.center.reusepass.ui.SecurityCenterReusedPassScreen
import proton.android.pass.features.security.center.sentinel.navigation.SecurityCenterSentinelDestination
import proton.android.pass.features.security.center.sentinel.navigation.SecurityCenterSentinelNavItem
import proton.android.pass.features.security.center.sentinel.ui.SecurityCenterSentinelBottomSheet
import proton.android.pass.features.security.center.weakpass.navigation.SecurityCenterWeakPassDestination
import proton.android.pass.features.security.center.weakpass.navigation.SecurityCenterWeakPassNavItem
import proton.android.pass.features.security.center.weakpass.ui.SecurityCenterWeakPassScreen
import proton.android.pass.navigation.api.bottomSheet
import proton.android.pass.navigation.api.composable

@Suppress("LongMethod")
fun NavGraphBuilder.securityCenterNavGraph(onNavigated: (SecurityCenterNavDestination) -> Unit) {

    composable(navItem = SecurityCenterHomeNavItem) {
        SecurityCenterHomeScreen(
            onNavigated = { destination ->
                when (destination) {
                    SecurityCenterHomeNavDestination.Home -> SecurityCenterNavDestination.MainHome
                    SecurityCenterHomeNavDestination.NewItem -> SecurityCenterNavDestination.MainNewItem
                    SecurityCenterHomeNavDestination.Profile -> SecurityCenterNavDestination.MainProfile
                    SecurityCenterHomeNavDestination.ReusedPasswords -> SecurityCenterNavDestination.ReusedPasswords
                    SecurityCenterHomeNavDestination.WeakPasswords -> SecurityCenterNavDestination.WeakPasswords
                    SecurityCenterHomeNavDestination.MissingTFA -> SecurityCenterNavDestination.MissingTFA
                    SecurityCenterHomeNavDestination.Sentinel -> SecurityCenterNavDestination.Sentinel
                }.also(onNavigated)
            }
        )
    }

    bottomSheet(navItem = SecurityCenterSentinelNavItem) {
        SecurityCenterSentinelBottomSheet(
            onNavigated = { destination ->
                when (destination) {
                    SecurityCenterSentinelDestination.Dismiss -> SecurityCenterNavDestination.Back(
                        comesFromBottomSheet = true
                    )

                    SecurityCenterSentinelDestination.Upsell -> SecurityCenterNavDestination.Upsell(
                        paidFeature = PaidFeature.Sentinel
                    )
                }.also(onNavigated)
            }
        )
    }

    composable(navItem = SecurityCenterWeakPassNavItem) {
        SecurityCenterWeakPassScreen(
            onNavigated = { destination ->
                when (destination) {
                    SecurityCenterWeakPassDestination.Back -> SecurityCenterNavDestination.Back(
                        comesFromBottomSheet = false
                    )

                    is SecurityCenterWeakPassDestination.ItemDetails -> SecurityCenterNavDestination.ItemDetails(
                        shareId = destination.shareId,
                        itemId = destination.itemId
                    )

                    SecurityCenterWeakPassDestination.Empty -> SecurityCenterNavDestination.Empty
                }.also(onNavigated)
            }
        )
    }

    composable(navItem = SecurityCenterReusedPassNavItem) {
        SecurityCenterReusedPassScreen(
            onNavigated = { destination ->
                when (destination) {
                    SecurityCenterReusedPassDestination.Back -> SecurityCenterNavDestination.Back(
                        comesFromBottomSheet = false
                    )

                    is SecurityCenterReusedPassDestination.ItemDetails -> SecurityCenterNavDestination.ItemDetails(
                        shareId = destination.shareId,
                        itemId = destination.itemId
                    )

                    SecurityCenterReusedPassDestination.Empty -> SecurityCenterNavDestination.Empty
                }.also(onNavigated)
            }
        )
    }

    composable(SecurityCenterMissingTFANavItem) {
        SecurityCenterMissingTfaScreen(
            onNavigated = { destination ->
                val event = when (destination) {
                    SecurityCenterMissingTFADestination.Back -> SecurityCenterNavDestination.Back(
                        comesFromBottomSheet = false
                    )

                    is SecurityCenterMissingTFADestination.ItemDetails -> SecurityCenterNavDestination.ItemDetails(
                        shareId = destination.shareId,
                        itemId = destination.itemId
                    )

                    SecurityCenterMissingTFADestination.Empty -> SecurityCenterNavDestination.Empty
                }
                onNavigated(event)
            }
        )
    }

}
