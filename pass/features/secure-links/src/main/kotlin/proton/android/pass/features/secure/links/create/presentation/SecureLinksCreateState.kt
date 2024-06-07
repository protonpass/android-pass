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

package proton.android.pass.features.secure.links.create.presentation

import androidx.compose.runtime.Stable
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some

@Stable
internal data class SecureLinksCreateState(
    internal val expiration: SecureLinkExpiration,
    private val maxViewsAllowedOption: Option<Int>
) {

    internal enum class SecureLinkExpiration {
        OneHour,
        OneDay,
        SevenDays,
        FourteenDays,
        ThirtyDays
    }

    internal val maxViewsAllowed: Int = maxViewsAllowedOption.value() ?: MIN_MAX_VIEWS_ALLOWED

    internal val isMaxViewsEnabled: Boolean = maxViewsAllowedOption is Some

    internal val isMaxViewsDecreaseEnabled: Boolean = maxViewsAllowed > MIN_MAX_VIEWS_ALLOWED

    internal companion object {

        internal val Initial = SecureLinksCreateState(
            expiration = SecureLinkExpiration.SevenDays,
            maxViewsAllowedOption = None
        )

        private const val MIN_MAX_VIEWS_ALLOWED = 1

    }

}
