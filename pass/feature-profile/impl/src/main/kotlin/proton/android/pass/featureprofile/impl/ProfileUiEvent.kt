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
    object OnListClick : ProfileUiEvent
    object OnCreateItemClick : ProfileUiEvent
    object OnAccountClick : ProfileUiEvent
    object OnSettingsClick : ProfileUiEvent
    object OnFeedbackClick : ProfileUiEvent
    object OnImportExportClick : ProfileUiEvent
    object OnRateAppClick : ProfileUiEvent
    object OnCopyAppVersionClick : ProfileUiEvent
    object OnAppVersionLongClick : ProfileUiEvent
    object OnUpgradeClick : ProfileUiEvent
    object OnAppLockTypeClick : ProfileUiEvent
    object OnAppLockTimeClick : ProfileUiEvent
    object OnChangePinClick : ProfileUiEvent

    @JvmInline
    value class OnAutofillClicked(val value: Boolean) : ProfileUiEvent

    @JvmInline
    value class OnToggleBiometricSystemLock(val value: Boolean) : ProfileUiEvent
}
