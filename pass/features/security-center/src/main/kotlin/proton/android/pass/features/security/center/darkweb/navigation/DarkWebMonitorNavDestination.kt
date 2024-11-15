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

package proton.android.pass.features.security.center.darkweb.navigation

import androidx.annotation.StringRes
import proton.android.pass.common.api.Option
import proton.android.pass.domain.breach.BreachEmailId
import proton.android.pass.domain.breach.CustomEmailId

sealed interface DarkWebMonitorNavDestination {

    data object Back : DarkWebMonitorNavDestination

    @JvmInline
    value class AddEmail(val email: Option<String>) : DarkWebMonitorNavDestination

    data class VerifyEmail(val id: CustomEmailId, val email: String) : DarkWebMonitorNavDestination

    data class CustomEmailReport(
        val id: CustomEmailId,
        val email: String
    ) : DarkWebMonitorNavDestination

    data class AliasEmailReport(
        val id: BreachEmailId.Alias,
        val email: String
    ) : DarkWebMonitorNavDestination

    data class ProtonEmailReport(
        val id: BreachEmailId.Proton,
        val email: String
    ) : DarkWebMonitorNavDestination

    data class UnverifiedEmailOptions(
        val id: CustomEmailId,
        val email: String
    ) : DarkWebMonitorNavDestination

    data object AllProtonEmails : DarkWebMonitorNavDestination

    data object AllAliasEmails : DarkWebMonitorNavDestination

    data class Help(
        @StringRes val titleResId: Int,
        @StringRes val textResId: Int
    ) : DarkWebMonitorNavDestination

    data object CannotAddCustomEmails : DarkWebMonitorNavDestination

}
