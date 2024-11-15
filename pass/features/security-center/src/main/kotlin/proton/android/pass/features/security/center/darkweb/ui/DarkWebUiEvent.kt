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

package proton.android.pass.features.security.center.darkweb.ui

import androidx.annotation.StringRes
import proton.android.pass.domain.breach.BreachEmailId
import proton.android.pass.domain.breach.CustomEmailId

sealed interface DarkWebUiEvent {

    data object OnUpClick : DarkWebUiEvent

    data object OnNewCustomEmailClick : DarkWebUiEvent

    data object OnShowAllAliasEmailBreachClick : DarkWebUiEvent

    data object OnShowAllProtonEmailBreachClick : DarkWebUiEvent

    @JvmInline
    value class OnAddCustomEmailClick(val email: String) : DarkWebUiEvent

    data class OnUnverifiedEmailOptionsClick(
        val id: CustomEmailId,
        val email: String
    ) : DarkWebUiEvent

    data class OnCustomEmailReportClick(
        val id: CustomEmailId,
        val email: String
    ) : DarkWebUiEvent

    data class OnShowAliasEmailReportClick(
        val id: BreachEmailId.Alias,
        val email: String
    ) : DarkWebUiEvent

    data class OnShowProtonEmailReportClick(
        val id: BreachEmailId.Proton,
        val email: String
    ) : DarkWebUiEvent

    data class HelpClick(
        @StringRes val titleResId: Int,
        @StringRes val textResId: Int
    ) : DarkWebUiEvent
}
