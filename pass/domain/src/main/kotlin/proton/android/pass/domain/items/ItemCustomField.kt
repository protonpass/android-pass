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

package proton.android.pass.domain.items

import androidx.compose.runtime.Stable
import proton.android.pass.domain.HiddenState

@Stable
sealed interface ItemCustomField {

    val title: String

    @Stable
    data class Plain(
        override val title: String,
        val content: String
    ) : ItemCustomField

    @Stable
    data class Hidden(
        override val title: String,
        val hiddenState: HiddenState
    ) : ItemCustomField

    @Stable
    data class Totp(
        override val title: String,
        val totp: proton.android.pass.domain.Totp?
    ) : ItemCustomField

}
