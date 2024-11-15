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

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import me.proton.core.compose.navigation.requireArguments
import proton.android.pass.domain.breach.BreachEmailId
import proton.android.pass.features.security.center.addressoptions.navigation.SecurityCenterAddressOptionsNavDestination
import proton.android.pass.features.security.center.addressoptions.navigation.SecurityCenterAliasAddressOptionsNavItem
import proton.android.pass.features.security.center.addressoptions.navigation.SecurityCenterCustomAddressOptionsNavItem
import proton.android.pass.features.security.center.addressoptions.navigation.SecurityCenterGlobalAddressOptionsNavItem
import proton.android.pass.features.security.center.addressoptions.navigation.SecurityCenterProtonAddressOptionsNavItem
import proton.android.pass.features.security.center.addressoptions.ui.SecurityCenterAddressOptionsBS
import proton.android.pass.features.security.center.aliaslist.navigation.SecurityCenterAliasListNavDestination
import proton.android.pass.features.security.center.aliaslist.navigation.SecurityCenterAliasListNavItem
import proton.android.pass.features.security.center.aliaslist.ui.SecurityCenterAliasListScreen
import proton.android.pass.features.security.center.breachdetail.navigation.SecurityCenterAliasEmailBreachDetailNavItem
import proton.android.pass.features.security.center.breachdetail.navigation.SecurityCenterCustomEmailBreachDetailNavItem
import proton.android.pass.features.security.center.breachdetail.navigation.SecurityCenterProtonEmailBreachDetailNavItem
import proton.android.pass.features.security.center.breachdetail.ui.SecurityCenterBreachDetailBottomSheet
import proton.android.pass.features.security.center.customemail.navigation.SecurityCenterCustomEmailNavDestination
import proton.android.pass.features.security.center.customemail.navigation.SecurityCenterCustomEmailNavItem
import proton.android.pass.features.security.center.customemail.ui.SecurityCenterCustomEmailScreen
import proton.android.pass.features.security.center.darkweb.navigation.CustomEmailOptionsNavDestination
import proton.android.pass.features.security.center.darkweb.navigation.CustomEmailOptionsNavItem
import proton.android.pass.features.security.center.darkweb.navigation.DarkWebCannotAddCustomEmailNavItem
import proton.android.pass.features.security.center.darkweb.navigation.DarkWebMonitorNavDestination
import proton.android.pass.features.security.center.darkweb.navigation.DarkWebMonitorNavItem
import proton.android.pass.features.security.center.darkweb.navigation.help.DarkWebHelpNavItem
import proton.android.pass.features.security.center.darkweb.navigation.help.DarkWebHelpTextNavArgId
import proton.android.pass.features.security.center.darkweb.navigation.help.DarkWebHelpTitleNavArgId
import proton.android.pass.features.security.center.darkweb.ui.DarkWebScreen
import proton.android.pass.features.security.center.darkweb.ui.customemails.dialog.CannotAddCustomEmailsDialog
import proton.android.pass.features.security.center.darkweb.ui.customemails.options.UnverifiedCustomEmailOptionsBottomSheet
import proton.android.pass.features.security.center.darkweb.ui.help.DarkWebHelpDialog
import proton.android.pass.features.security.center.excludeditems.navigation.SecurityCenterExcludeItemsDestination
import proton.android.pass.features.security.center.excludeditems.navigation.SecurityCenterExcludedItemsNavItem
import proton.android.pass.features.security.center.excludeditems.ui.SecurityCenterExcludedItemsScreen
import proton.android.pass.features.security.center.home.navigation.SecurityCenterHomeNavDestination
import proton.android.pass.features.security.center.home.navigation.SecurityCenterHomeNavItem
import proton.android.pass.features.security.center.home.ui.SecurityCenterHomeScreen
import proton.android.pass.features.security.center.missingtfa.navigation.SecurityCenterMissingTFADestination
import proton.android.pass.features.security.center.missingtfa.navigation.SecurityCenterMissingTFANavItem
import proton.android.pass.features.security.center.missingtfa.ui.SecurityCenterMissingTfaScreen
import proton.android.pass.features.security.center.protonlist.navigation.SecurityCenterProtonListNavDestination
import proton.android.pass.features.security.center.protonlist.navigation.SecurityCenterProtonListNavItem
import proton.android.pass.features.security.center.protonlist.ui.SecurityCenterProtonListScreen
import proton.android.pass.features.security.center.report.navigation.SecurityCenterAliasEmailReportNavItem
import proton.android.pass.features.security.center.report.navigation.SecurityCenterCustomEmailReportNavItem
import proton.android.pass.features.security.center.report.navigation.SecurityCenterProtonEmailReportNavItem
import proton.android.pass.features.security.center.report.navigation.SecurityCenterReportDestination
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
import proton.android.pass.navigation.api.dialog

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
                    SecurityCenterHomeNavDestination.ExcludedItems -> SecurityCenterNavDestination.ExcludedItems
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

                    is SecurityCenterSentinelDestination.OnUpsell ->
                        SecurityCenterNavDestination.Upsell(destination.paidFeature)
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
                        itemId = destination.itemId,
                        origin = SecurityCenterNavDestination.ItemDetails.Origin.WeakPasswords
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
                        itemId = destination.itemId,
                        origin = SecurityCenterNavDestination.ItemDetails.Origin.ReusedPassword
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
                        itemId = destination.itemId,
                        origin = SecurityCenterNavDestination.ItemDetails.Origin.Missing2fa
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
                    is DarkWebMonitorNavDestination.AddEmail -> onNavigated(
                        SecurityCenterNavDestination.AddCustomEmail(
                            email = destination.email
                        )
                    )

                    is DarkWebMonitorNavDestination.CustomEmailReport -> onNavigated(
                        SecurityCenterNavDestination.CustomEmailReport(
                            id = destination.id,
                            email = destination.email
                        )
                    )

                    DarkWebMonitorNavDestination.Back -> onNavigated(SecurityCenterNavDestination.Back())
                    is DarkWebMonitorNavDestination.VerifyEmail -> onNavigated(
                        SecurityCenterNavDestination.VerifyEmail(
                            id = destination.id,
                            email = destination.email
                        )
                    )

                    is DarkWebMonitorNavDestination.UnverifiedEmailOptions -> onNavigated(
                        SecurityCenterNavDestination.UnverifiedEmailOptions(
                            id = destination.id,
                            email = destination.email
                        )
                    )

                    is DarkWebMonitorNavDestination.AliasEmailReport -> onNavigated(
                        SecurityCenterNavDestination.AliasEmailReport(
                            id = destination.id,
                            email = destination.email
                        )
                    )

                    is DarkWebMonitorNavDestination.ProtonEmailReport -> onNavigated(
                        SecurityCenterNavDestination.ProtonEmailReport(
                            id = destination.id,
                            email = destination.email
                        )
                    )

                    DarkWebMonitorNavDestination.AllProtonEmails ->
                        onNavigated(SecurityCenterNavDestination.AllProtonEmails)

                    DarkWebMonitorNavDestination.AllAliasEmails ->
                        onNavigated(SecurityCenterNavDestination.AllAliasEmails)

                    is DarkWebMonitorNavDestination.Help -> onNavigated(
                        SecurityCenterNavDestination.DarkWebHelp(
                            titleResId = destination.titleResId,
                            textResId = destination.textResId
                        )
                    )

                    DarkWebMonitorNavDestination.CannotAddCustomEmails ->
                        onNavigated(SecurityCenterNavDestination.CannotAddCustomEmails)
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
                    SecurityCenterVerifyEmailDestination.Back -> SecurityCenterNavDestination.BackToDarkWebMonitoring
                    SecurityCenterVerifyEmailDestination.EmailVerified -> SecurityCenterNavDestination.EmailVerified
                }
                onNavigated(event)
            }
        )
    }

    composable(SecurityCenterCustomEmailReportNavItem) {
        SecurityCenterReportScreen(onNavigated)
    }

    composable(SecurityCenterAliasEmailReportNavItem) {
        SecurityCenterReportScreen(onNavigated)
    }

    composable(SecurityCenterProtonEmailReportNavItem) {
        SecurityCenterReportScreen(onNavigated)
    }

    bottomSheet(CustomEmailOptionsNavItem) {
        UnverifiedCustomEmailOptionsBottomSheet(
            onNavigate = { destination ->
                val event = when (destination) {
                    CustomEmailOptionsNavDestination.Close -> SecurityCenterNavDestination.Back(
                        comesFromBottomSheet = true
                    )

                    is CustomEmailOptionsNavDestination.Verify -> SecurityCenterNavDestination.VerifyEmail(
                        id = destination.breachCustomEmailId,
                        email = destination.customEmail
                    )
                }
                onNavigated(event)
            }
        )
    }

    bottomSheet(navItem = SecurityCenterCustomEmailBreachDetailNavItem) {
        SecurityCenterBreachDetailBottomSheet(
            onDismiss = {
                onNavigated(SecurityCenterNavDestination.Back(comesFromBottomSheet = true))
            }
        )
    }

    bottomSheet(navItem = SecurityCenterProtonEmailBreachDetailNavItem) {
        SecurityCenterBreachDetailBottomSheet(
            onDismiss = {
                onNavigated(SecurityCenterNavDestination.Back(comesFromBottomSheet = true))
            }
        )
    }

    bottomSheet(navItem = SecurityCenterAliasEmailBreachDetailNavItem) {
        SecurityCenterBreachDetailBottomSheet(
            onDismiss = {
                onNavigated(SecurityCenterNavDestination.Back(comesFromBottomSheet = true))
            }
        )
    }

    composable(SecurityCenterExcludedItemsNavItem) {
        SecurityCenterExcludedItemsScreen(
            onNavigated = { destination ->
                when (destination) {
                    is SecurityCenterExcludeItemsDestination.Back -> SecurityCenterNavDestination.Back(
                        force = destination.force
                    )

                    is SecurityCenterExcludeItemsDestination.ItemDetails -> SecurityCenterNavDestination.ItemDetails(
                        shareId = destination.shareId,
                        itemId = destination.itemId,
                        origin = SecurityCenterNavDestination.ItemDetails.Origin.Excluded
                    )
                }.also(onNavigated)
            }
        )
    }

    composable(SecurityCenterProtonListNavItem) {
        SecurityCenterProtonListScreen(
            onNavigated = { destination ->
                when (destination) {
                    SecurityCenterProtonListNavDestination.Back -> SecurityCenterNavDestination.Back()
                    is SecurityCenterProtonListNavDestination.OnEmailClick ->
                        SecurityCenterNavDestination.ProtonEmailReport(
                            id = destination.id,
                            email = destination.email
                        )

                    is SecurityCenterProtonListNavDestination.OnOptionsClick ->
                        SecurityCenterNavDestination.GlobalMonitorAddressOptions(
                            globalMonitorAddressType = destination.globalMonitorAddressType,
                            addressOptionsType = destination.addressOptionsType
                        )
                }.also(onNavigated)
            }
        )
    }


    composable(SecurityCenterAliasListNavItem) {
        SecurityCenterAliasListScreen(
            onNavigated = { destination ->
                when (destination) {
                    SecurityCenterAliasListNavDestination.Back -> SecurityCenterNavDestination.Back()
                    is SecurityCenterAliasListNavDestination.OnEmailClick ->
                        SecurityCenterNavDestination.AliasEmailReport(
                            id = destination.id,
                            email = destination.email
                        )

                    is SecurityCenterAliasListNavDestination.OnOptionsClick ->
                        SecurityCenterNavDestination.GlobalMonitorAddressOptions(
                            globalMonitorAddressType = destination.globalMonitorAddressType,
                            addressOptionsType = destination.addressOptionsType
                        )
                }.also(onNavigated)
            }
        )
    }

    bottomSheet(SecurityCenterGlobalAddressOptionsNavItem) {
        SecurityCenterAddressOptionsBottomsheet(onNavigated)
    }
    bottomSheet(SecurityCenterCustomAddressOptionsNavItem) {
        SecurityCenterAddressOptionsBottomsheet(onNavigated)
    }
    bottomSheet(SecurityCenterAliasAddressOptionsNavItem) {
        SecurityCenterAddressOptionsBottomsheet(onNavigated)
    }
    bottomSheet(SecurityCenterProtonAddressOptionsNavItem) {
        SecurityCenterAddressOptionsBottomsheet(onNavigated)
    }

    dialog(DarkWebHelpNavItem) { navBackStackEntry ->
        DarkWebHelpDialog(
            titleResId = navBackStackEntry.requireArguments().getInt(DarkWebHelpTitleNavArgId.key),
            textResId = navBackStackEntry.requireArguments().getInt(DarkWebHelpTextNavArgId.key),
            onDismiss = {
                onNavigated(SecurityCenterNavDestination.Back())
            }
        )
    }

    dialog(DarkWebCannotAddCustomEmailNavItem) {
        CannotAddCustomEmailsDialog(
            onDismiss = { onNavigated(SecurityCenterNavDestination.Back()) }
        )
    }
}

@Composable
private fun SecurityCenterAddressOptionsBottomsheet(onNavigated: (SecurityCenterNavDestination) -> Unit) {
    SecurityCenterAddressOptionsBS(
        onNavigated = { destination ->
            when (destination) {
                SecurityCenterAddressOptionsNavDestination.Back ->
                    SecurityCenterNavDestination.Back(comesFromBottomSheet = true)

                SecurityCenterAddressOptionsNavDestination.OnCustomEmailRemoved ->
                    SecurityCenterNavDestination.BackToDarkWebMonitoring
            }.also(onNavigated)
        }
    )
}

@Composable
private fun SecurityCenterReportScreen(onNavigated: (SecurityCenterNavDestination) -> Unit) {
    SecurityCenterReportScreen(
        onNavigated = { destination ->
            val event = when (destination) {
                SecurityCenterReportDestination.Back -> SecurityCenterNavDestination.Back()
                is SecurityCenterReportDestination.EmailBreachDetail -> when (destination.id) {

                    is BreachEmailId.Alias ->
                        SecurityCenterNavDestination.AliasEmailBreachDetail(destination.id)

                    is BreachEmailId.Custom ->
                        SecurityCenterNavDestination.CustomEmailBreachDetail(destination.id)

                    is BreachEmailId.Proton ->
                        SecurityCenterNavDestination.ProtonEmailBreachDetail(destination.id)
                }

                is SecurityCenterReportDestination.ItemDetail ->
                    SecurityCenterNavDestination.ItemDetails(
                        shareId = destination.shareId,
                        itemId = destination.itemId,
                        origin = SecurityCenterNavDestination.ItemDetails.Origin.Report
                    )

                is SecurityCenterReportDestination.OnMenuClick -> when (destination.id) {
                    is BreachEmailId.Alias -> SecurityCenterNavDestination.ReportAliasAddressOptions(
                        breachEmailId = destination.id,
                        addressOptionsType = destination.addressOptionsType
                    )

                    is BreachEmailId.Custom -> SecurityCenterNavDestination.ReportCustomAddressOptions(
                        breachEmailId = destination.id,
                        addressOptionsType = destination.addressOptionsType
                    )

                    is BreachEmailId.Proton -> SecurityCenterNavDestination.ReportProtonAddressOptions(
                        breachEmailId = destination.id,
                        addressOptionsType = destination.addressOptionsType
                    )
                }
            }
            onNavigated(event)
        }
    )
}
