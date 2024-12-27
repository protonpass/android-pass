/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.onboarding

import androidx.compose.runtime.Stable
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import proton.android.pass.features.onboarding.OnBoardingPageName.Autofill
import proton.android.pass.features.onboarding.OnBoardingPageName.Fingerprint
import proton.android.pass.features.onboarding.OnBoardingPageName.InvitePending
import proton.android.pass.features.onboarding.OnBoardingPageName.Last

sealed interface OnboardingEvent {
    data object Unknown : OnboardingEvent
    data object OnboardingCompleted : OnboardingEvent
}

@Stable
data class OnBoardingUiState(
    val selectedPage: Int,
    val enabledPages: ImmutableList<OnBoardingPageName>,
    val event: OnboardingEvent
) {
    companion object {
        val Initial = OnBoardingUiState(
            selectedPage = 0,
            enabledPages = persistentListOf(),
            event = OnboardingEvent.Unknown
        )
    }
}

@Stable
enum class OnBoardingPageName {
    Autofill, Fingerprint, Last, InvitePending
}

open class OnBoardingUiStatePreviewProvider : PreviewParameterProvider<OnBoardingUiState> {
    override val values: Sequence<OnBoardingUiState> = sequenceOf(
        OnBoardingUiState(
            selectedPage = 0,
            enabledPages = persistentListOf(Autofill),
            event = OnboardingEvent.Unknown
        ),
        OnBoardingUiState(
            selectedPage = 0,
            enabledPages = persistentListOf(Fingerprint),
            event = OnboardingEvent.Unknown
        ),
        OnBoardingUiState(
            selectedPage = 0,
            enabledPages = persistentListOf(Last),
            event = OnboardingEvent.Unknown
        ),
        OnBoardingUiState(
            selectedPage = 0,
            enabledPages = persistentListOf(Autofill, Fingerprint),
            event = OnboardingEvent.Unknown
        ),
        OnBoardingUiState(
            selectedPage = 0,
            enabledPages = persistentListOf(InvitePending),
            event = OnboardingEvent.Unknown
        )
    )
}
