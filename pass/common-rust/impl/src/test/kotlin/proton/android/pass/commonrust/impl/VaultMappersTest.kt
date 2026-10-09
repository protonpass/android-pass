/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.commonrust.impl

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import proton.android.pass.domain.ShareColor
import proton.android.pass.domain.ShareIcon
import proton.android.pass.domain.VaultProperties
import proton.android.pass.types.VaultColor
import proton.android.pass.types.VaultData
import proton.android.pass.types.VaultDisplayPreferences
import proton.android.pass.types.VaultIcon

internal class VaultMappersTest {

    @Test
    fun `every share icon and color survives a round trip`() {
        ShareIcon.entries.forEach { assertThat(it.toLibrary().toDomain()).isEqualTo(it) }
        ShareColor.entries.forEach { assertThat(it.toLibrary().toDomain()).isEqualTo(it) }
    }

    @Test
    fun `unspecified and custom library values map to the first icon and color`() {
        assertThat(VaultIcon.UNSPECIFIED.toDomain()).isEqualTo(ShareIcon.Icon1)
        assertThat(VaultIcon.CUSTOM.toDomain()).isEqualTo(ShareIcon.Icon1)
        assertThat(VaultColor.UNSPECIFIED.toDomain()).isEqualTo(ShareColor.Color1)
        assertThat(VaultColor.CUSTOM.toDomain()).isEqualTo(ShareColor.Color1)
    }

    @Test
    fun `serializing a new vault writes the mapped icon and color`() {
        val data = properties(ShareIcon.Icon7, ShareColor.Color3).toLibraryVaultData()

        assertThat(data.displayPreferences.icon).isEqualTo(VaultIcon.ICON7)
        assertThat(data.displayPreferences.color).isEqualTo(VaultColor.COLOR3)
    }

    @Test
    fun `update keeps custom icon and color when domain value is unchanged`() {
        val original = libraryVault(VaultIcon.CUSTOM, VaultColor.CUSTOM)

        val data = properties(ShareIcon.Icon1, ShareColor.Color1, name = "renamed").toLibraryVaultData(original)

        assertThat(data.name).isEqualTo("renamed")
        assertThat(data.displayPreferences.icon).isEqualTo(VaultIcon.CUSTOM)
        assertThat(data.displayPreferences.color).isEqualTo(VaultColor.CUSTOM)
    }

    @Test
    fun `update keeps unspecified icon and color when domain value is unchanged`() {
        val original = libraryVault(VaultIcon.UNSPECIFIED, VaultColor.UNSPECIFIED)

        val data = properties(ShareIcon.Icon1, ShareColor.Color1).toLibraryVaultData(original)

        assertThat(data.displayPreferences.icon).isEqualTo(VaultIcon.UNSPECIFIED)
        assertThat(data.displayPreferences.color).isEqualTo(VaultColor.UNSPECIFIED)
    }

    @Test
    fun `update replaces custom values when the user picks another icon and color`() {
        val original = libraryVault(VaultIcon.CUSTOM, VaultColor.CUSTOM)

        val data = properties(ShareIcon.Icon5, ShareColor.Color9).toLibraryVaultData(original)

        assertThat(data.displayPreferences.icon).isEqualTo(VaultIcon.ICON5)
        assertThat(data.displayPreferences.color).isEqualTo(VaultColor.COLOR9)
    }

    @Test
    fun `update changes only the value that differs`() {
        val original = libraryVault(VaultIcon.CUSTOM, VaultColor.COLOR2)

        val data = properties(ShareIcon.Icon1, ShareColor.Color4).toLibraryVaultData(original)

        assertThat(data.displayPreferences.icon).isEqualTo(VaultIcon.CUSTOM)
        assertThat(data.displayPreferences.color).isEqualTo(VaultColor.COLOR4)
    }

    @Test
    fun `deserialized vault maps to domain properties`() {
        val result = libraryVault(VaultIcon.ICON3, VaultColor.COLOR8).toVaultProperties()

        assertThat(result).isEqualTo(properties(ShareIcon.Icon3, ShareColor.Color8, name = "vault"))
    }

    private fun properties(
        icon: ShareIcon,
        color: ShareColor,
        name: String = "name"
    ) = VaultProperties(
        name = name,
        description = "description",
        icon = icon,
        color = color
    )

    private fun libraryVault(icon: VaultIcon, color: VaultColor) = VaultData(
        name = "vault",
        description = "description",
        displayPreferences = VaultDisplayPreferences(icon = icon, color = color)
    )
}
