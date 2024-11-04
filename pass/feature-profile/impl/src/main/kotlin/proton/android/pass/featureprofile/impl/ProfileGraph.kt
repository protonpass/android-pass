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

package proton.android.pass.featureprofile.impl

import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import me.proton.core.domain.entity.UserId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.features.PaidFeature
import proton.android.pass.featureprofile.impl.applocktime.AppLockTimeBottomsheet
import proton.android.pass.featureprofile.impl.applocktype.AppLockTypeBottomsheet
import proton.android.pass.featureprofile.impl.pinconfig.PinConfigScreen
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavItemType
import proton.android.pass.navigation.api.bottomSheet
import proton.android.pass.navigation.api.composable

const val ENTER_PIN_PARAMETER_KEY = "enterPin"
private const val PROFILE_GRAPH = "profile_graph"

object ProfileNavItem : NavItem(baseRoute = "profile", isTopLevel = true)
object FeedbackBottomsheet : NavItem(
    baseRoute = "feedback/bottomsheet",
    navItemType = NavItemType.Bottomsheet
)

object AppLockTimeBottomsheet : NavItem(
    baseRoute = "applock/time/bottomsheet",
    navItemType = NavItemType.Bottomsheet
)

object AppLockTypeBottomsheet : NavItem(
    baseRoute = "applock/type/bottomsheet",
    navItemType = NavItemType.Bottomsheet,
    noHistory = true
)

object PinConfig : NavItem(baseRoute = "pin/config")

sealed interface ProfileNavigation {

    data object Account : ProfileNavigation

    data object AppLockTime : ProfileNavigation

    data object AppLockType : ProfileNavigation

    data object Back : ProfileNavigation

    data object CloseBottomSheet : ProfileNavigation

    data object ConfigurePin : ProfileNavigation

    data object EnterPin : ProfileNavigation

    data object FeatureFlags : ProfileNavigation

    data object Feedback : ProfileNavigation

    data object Finish : ProfileNavigation

    data object Home : ProfileNavigation

    data object Report : ProfileNavigation

    data object Settings : ProfileNavigation

    data object Upgrade : ProfileNavigation

    data object SecureLinks : ProfileNavigation

    @JvmInline
    value class UpsellSecureLinks(val paidFeature: PaidFeature) : ProfileNavigation

    data object OnAddAccount : ProfileNavigation

    @JvmInline
    value class OnSignIn(val userId: UserId) : ProfileNavigation

    @JvmInline
    value class OnSignOut(val userId: UserId) : ProfileNavigation

    @JvmInline
    value class OnRemoveAccount(val userId: UserId) : ProfileNavigation

    @JvmInline
    value class OnSwitchAccount(val userId: UserId) : ProfileNavigation

    data object SyncDialog : ProfileNavigation

    data object AliasesSyncDetails : ProfileNavigation

    data object AliasesSyncManagement : ProfileNavigation

    @JvmInline
    value class AliasesSyncSettings(val shareId: ShareId?) : ProfileNavigation

}

fun NavGraphBuilder.profileGraph(onNavigateEvent: (ProfileNavigation) -> Unit) {
    navigation(
        route = PROFILE_GRAPH,
        startDestination = ProfileNavItem.route
    ) {
        composable(ProfileNavItem) {
            val enterPinSuccess by it.savedStateHandle.getStateFlow(ENTER_PIN_PARAMETER_KEY, false)
                .collectAsStateWithLifecycle()
            ProfileScreen(
                modifier = Modifier.testTag(ProfileScreenTestTag.SCREEN),
                enterPinSuccess = enterPinSuccess,
                onNavigateEvent = onNavigateEvent,
                onClearPinSuccess = { it.savedStateHandle.remove<Boolean>(ENTER_PIN_PARAMETER_KEY) }
            )
        }
        bottomSheet(FeedbackBottomsheet) {
            FeedbackBottomsheet(onNavigateEvent = onNavigateEvent)
        }
        bottomSheet(AppLockTimeBottomsheet) {
            AppLockTimeBottomsheet(onClose = { onNavigateEvent(ProfileNavigation.CloseBottomSheet) })
        }
        bottomSheet(AppLockTypeBottomsheet) {
            val enterPin = it.savedStateHandle.get<Boolean>(ENTER_PIN_PARAMETER_KEY) ?: false
            AppLockTypeBottomsheet(enterPinSuccess = enterPin, onNavigateEvent = onNavigateEvent)
        }
        composable(PinConfig) {
            PinConfigScreen(onNavigateEvent = onNavigateEvent)
        }
    }
}
