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

sealed interface ProfileUiEvent {

    data object OnAppLockTimeClick : ProfileUiEvent

    data object OnAppLockTypeClick : ProfileUiEvent

    data object OnAppVersionLongClick : ProfileUiEvent

    @JvmInline
    value class OnAutofillClicked(val value: Boolean) : ProfileUiEvent

    data object OnChangePinClick : ProfileUiEvent

    data object OnCopyAppVersionClick : ProfileUiEvent

    data object OnFeedbackClick : ProfileUiEvent

    data object OnImportExportClick : ProfileUiEvent

    data object OnRateAppClick : ProfileUiEvent

    data object OnSettingsClick : ProfileUiEvent

    @JvmInline
    value class OnToggleBiometricSystemLock(val value: Boolean) : ProfileUiEvent

    data object OnTutorialClick : ProfileUiEvent

    data object OnUpgradeClick : ProfileUiEvent

    data object OnSecureLinksClicked : ProfileUiEvent

    data object OnAliasesClicked : ProfileUiEvent

    data object OnLoginCountClick : ProfileUiEvent

    data object OnAliasCountClick : ProfileUiEvent

    data object OnCreditCardCountClick : ProfileUiEvent

    data object OnNoteCountClick : ProfileUiEvent

    data object OnIdentityCountClick : ProfileUiEvent

    data object OnMFACountClick : ProfileUiEvent

}
