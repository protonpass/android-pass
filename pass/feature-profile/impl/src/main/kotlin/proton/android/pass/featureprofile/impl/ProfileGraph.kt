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

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import proton.android.pass.featureprofile.impl.applocktime.AppLockTimeBottomsheet
import proton.android.pass.featureprofile.impl.applocktype.AppLockTypeBottomsheet
import proton.android.pass.featureprofile.impl.pinconfig.PinConfigScreen
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavItemType
import proton.android.pass.navigation.api.bottomSheet
import proton.android.pass.navigation.api.composable

const val ENTER_PIN_PARAMETER_KEY = "enterPin"

object Profile : NavItem(baseRoute = "profile", isTopLevel = true)
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
    object Back : ProfileNavigation
    object Account : ProfileNavigation
    object List : ProfileNavigation
    object CreateItem : ProfileNavigation
    object Settings : ProfileNavigation
    object Feedback : ProfileNavigation
    object Report : ProfileNavigation
    object FeatureFlags : ProfileNavigation
    object Upgrade : ProfileNavigation
    object Finish : ProfileNavigation
    object CloseBottomSheet : ProfileNavigation
    object AppLockType : ProfileNavigation
    object AppLockTime : ProfileNavigation
    object ConfigurePin : ProfileNavigation
    object EnterPin : ProfileNavigation
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.profileGraph(
    onNavigateEvent: (ProfileNavigation) -> Unit
) {
    composable(Profile) {
        val enterPinSuccess by it.savedStateHandle.getStateFlow(ENTER_PIN_PARAMETER_KEY, false)
            .collectAsStateWithLifecycle()
        BackHandler { onNavigateEvent(ProfileNavigation.Finish) }
        ProfileScreen(
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
