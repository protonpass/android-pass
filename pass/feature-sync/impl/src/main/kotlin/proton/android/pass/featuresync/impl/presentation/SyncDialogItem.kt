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

package proton.android.pass.featuresync.impl.presentation

import proton.android.pass.domain.ShareColor
import proton.android.pass.domain.ShareIcon
import proton.android.pass.domain.Vault

internal data class SyncDialogItem(
    private val vault: Vault,
    private val current: Int?,
    private val total: Int?
) {

    internal val vaultName: String = vault.name

    internal val vaultColor: ShareColor = vault.color

    internal val vaultIcon: ShareIcon = vault.icon

    internal val currentItemsCount: Int = current ?: ITEM_COUNT_DEFAULT_CURRENT

    internal val totalItemsCount: Int = total ?: ITEM_COUNT_DEFAULT_TOTAL

    private companion object {

        private const val ITEM_COUNT_DEFAULT_CURRENT = -1

        private const val ITEM_COUNT_DEFAULT_TOTAL = -1

    }

}
