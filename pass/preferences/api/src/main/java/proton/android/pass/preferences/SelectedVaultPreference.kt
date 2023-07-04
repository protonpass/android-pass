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

package proton.android.pass.preferences

private const val ALL_VAULTS_VALUE = "AllVaults"
private const val TRASH_VALUE = "Trash"

sealed interface SelectedVaultPreference {

    fun value(): String

    object AllVaults : SelectedVaultPreference {
        override fun value(): String = ALL_VAULTS_VALUE
    }
    object Trash : SelectedVaultPreference {
        override fun value(): String = TRASH_VALUE
    }
    data class Vault(val shareId: String) : SelectedVaultPreference {
        override fun value(): String = shareId
    }

    companion object
}

fun SelectedVaultPreference.Companion.fromValue(value: String): SelectedVaultPreference =
    when {
        value == ALL_VAULTS_VALUE -> SelectedVaultPreference.AllVaults
        value == TRASH_VALUE -> SelectedVaultPreference.Trash
        value.isNotBlank() -> SelectedVaultPreference.Vault(value)
        else -> SelectedVaultPreference.AllVaults
    }
