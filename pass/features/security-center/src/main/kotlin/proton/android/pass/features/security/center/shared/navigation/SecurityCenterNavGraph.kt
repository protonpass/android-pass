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
import proton.android.pass.features.security.center.customemail.navigation.SecurityCenterCustomEmailNavDestination
import proton.android.pass.features.security.center.customemail.navigation.SecurityCenterCustomEmailNavItem
import proton.android.pass.features.security.center.customemail.ui.SecurityCenterCustomEmailScreen
import proton.android.pass.features.security.center.darkweb.navigation.DarkWebMonitorNavDestination
import proton.android.pass.features.security.center.darkweb.navigation.DarkWebMonitorNavItem
import proton.android.pass.features.security.center.darkweb.ui.DarkWebScreen
import proton.android.pass.features.security.center.home.navigation.SecurityCenterHomeNavDestination
import proton.android.pass.features.security.center.home.navigation.SecurityCenterHomeNavItem
import proton.android.pass.features.security.center.home.ui.SecurityCenterHomeScreen
import proton.android.pass.features.security.center.missingtfa.navigation.SecurityCenterMissingTFADestination
import proton.android.pass.features.security.center.missingtfa.navigation.SecurityCenterMissingTFANavItem
import proton.android.pass.features.security.center.missingtfa.ui.SecurityCenterMissingTfaScreen
import proton.android.pass.features.security.center.report.navigation.EmailType
import proton.android.pass.features.security.center.report.navigation.SecurityCenterReportDestination
import proton.android.pass.features.security.center.report.navigation.SecurityCenterReportNavItem
import proton.android.pass.features.security.center.report.ui.SecurityCenterReportScreen
import proton.android.pass.features.security.center.reusepass.navigation.SecurityCenterReusedPassDestination
import proton.android.pass.features.security.center.reusepass.navigation.SecurityCenterReusedPassNavItem
import proton.android.pass.features.security.center.reusepass.ui.SecurityCenterReusedPassScreen
import proton.android.pass.features.security.center.sentinel.navigation.SecurityCenterSentinelDestination
import proton.android.pass.features.security.center.sentinel.navigation.SecurityCenterSentinelNavItem
import proton.android.pass.features.security.center.sentinel.ui.SecurityCenterSentinelBottomSheet
import proton.android.pass.features.security.center.verifyemail.navigation.SecurityCenterVerifyEmailDestination
import proton.android.pass.features.security.center.verifyemail.navigation.SecurityCenterVerifyEmailNavItem
import proton.android.pass.features.security.center.verifyemail.ui.SecurityCenterVerifyEmailScreen
import proton.android.pass.features.security.center.weakpass.navigation.SecurityCenterWeakPassDestination
import proton.android.pass.features.security.center.weakpass.navigation.SecurityCenterWeakPassNavItem
import proton.android.pass.features.security.center.weakpass.ui.SecurityCenterWeakPassScreen
import proton.android.pass.navigation.api.bottomSheet
import proton.android.pass.navigation.api.composable

@Suppress("ComplexMethod", "CyclomaticComplexity", "LongMethod")
fun NavGraphBuilder.securityCenterNavGraph(onNavigated: (SecurityCenterNavDestination) -> Unit) {

    composable(navItem = SecurityCenterHomeNavItem) {
        SecurityCenterHomeScreen(
            onNavigated = { destination ->
                when (destination) {
                    SecurityCenterHomeNavDestination.Home -> SecurityCenterNavDestination.MainHome
                    SecurityCenterHomeNavDestination.NewItem -> SecurityCenterNavDestination.MainNewItem
                    SecurityCenterHomeNavDestination.Profile -> SecurityCenterNavDestination.MainProfile
                    SecurityCenterHomeNavDestination.DarkWebMonitoring -> SecurityCenterNavDestination.DarkWebMonitoring
                    SecurityCenterHomeNavDestination.ReusedPasswords -> SecurityCenterNavDestination.ReusedPasswords
                    SecurityCenterHomeNavDestination.WeakPasswords -> SecurityCenterNavDestination.WeakPasswords
                    SecurityCenterHomeNavDestination.MissingTFA -> SecurityCenterNavDestination.MissingTFA
                    SecurityCenterHomeNavDestination.Sentinel -> SecurityCenterNavDestination.Sentinel
                    is SecurityCenterHomeNavDestination.Upsell -> SecurityCenterNavDestination.Upsell(
                        paidFeature = destination.paidFeature
                    )
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

    composable(DarkWebMonitorNavItem) {
        DarkWebScreen(
            onNavigate = { destination ->
                when (destination) {
                    DarkWebMonitorNavDestination.AddEmail ->
                        onNavigated(SecurityCenterNavDestination.AddCustomEmail)

                    is DarkWebMonitorNavDestination.CustomEmailReport -> {
                        onNavigated(
                            SecurityCenterNavDestination.Report(
                                emailType = EmailType.Custom,
                                id = destination.id,
                                email = destination.email,
                                breachCount = destination.breachCount
                            )
                        )
                    }

                    DarkWebMonitorNavDestination.Back -> onNavigated(SecurityCenterNavDestination.Back())
                    is DarkWebMonitorNavDestination.VerifyEmail -> onNavigated(
                        SecurityCenterNavDestination.VerifyEmail(
                            id = destination.id,
                            email = destination.email
                        )
                    )
                }
            }
        )
    }

    composable(SecurityCenterCustomEmailNavItem) {
        SecurityCenterCustomEmailScreen(
            onNavigated = { destination ->
                val event = when (destination) {
                    SecurityCenterCustomEmailNavDestination.Back -> SecurityCenterNavDestination.Back()
                    is SecurityCenterCustomEmailNavDestination.VerifyEmail -> SecurityCenterNavDestination.VerifyEmail(
                        id = destination.id,
                        email = destination.email
                    )
                }
                onNavigated(event)
            }
        )
    }

    composable(SecurityCenterVerifyEmailNavItem) {
        SecurityCenterVerifyEmailScreen(
            onNavigated = { destination ->
                val event = when (destination) {
                    SecurityCenterVerifyEmailDestination.Back -> SecurityCenterNavDestination.Back()
                    SecurityCenterVerifyEmailDestination.EmailVerified -> SecurityCenterNavDestination.EmailVerified
                }
                onNavigated(event)
            }
        )
    }

    composable(SecurityCenterReportNavItem) {
        SecurityCenterReportScreen(
            onNavigated = { destination ->
                val event = when (destination) {
                    SecurityCenterReportDestination.Back -> SecurityCenterNavDestination.Back()
                }
                onNavigated(event)
            }
        )
    }
}
