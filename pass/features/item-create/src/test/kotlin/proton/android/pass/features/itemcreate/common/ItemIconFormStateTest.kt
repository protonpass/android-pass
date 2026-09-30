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

package proton.android.pass.features.itemcreate.common

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import proton.android.pass.commonrust.fakes.FakeEmailValidator
import proton.android.pass.crypto.fakes.context.FakeEncryptionContext
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.WifiSecurityType
import proton.android.pass.features.itemcreate.alias.AliasItemFormState
import proton.android.pass.features.itemcreate.creditcard.CreditCardItemFormState
import proton.android.pass.features.itemcreate.custom.createupdate.presentation.ItemFormState
import proton.android.pass.features.itemcreate.identity.presentation.IdentityItemFormState
import proton.android.pass.features.itemcreate.login.LoginItemFormState
import proton.android.pass.features.itemcreate.note.NoteItemFormState

/**
 * The update screens build the new contents from their form state, so the form state
 * must carry the icon of the edited item, otherwise saving would remove it.
 */
class ItemIconFormStateTest {

    private val empty = HiddenState.Empty(FakeEncryptionContext.encrypt(""))

    @Test
    fun `note form state keeps the icon`() {
        val contents = ItemContents.Note(title = "t", note = "n", customFields = emptyList(), icon = ICON)
        assertThat(NoteItemFormState(contents).toItemContents().icon).isEqualTo(ICON)
    }

    @Test
    fun `credit card form state keeps the icon`() {
        val contents = ItemContents.CreditCard.default(cvv = empty, pin = empty).copy(icon = ICON)
        assertThat(CreditCardItemFormState(contents).toItemContents().icon).isEqualTo(ICON)
    }

    @Test
    fun `custom item form states keep the icon`() {
        val custom = ItemContents.Custom(
            title = "t",
            note = "",
            customFields = emptyList(),
            sectionContentList = emptyList(),
            icon = ICON
        )
        val wifi = ItemContents.WifiNetwork(
            title = "t",
            note = "",
            customFields = emptyList(),
            ssid = "ssid",
            password = empty,
            wifiSecurityType = WifiSecurityType.WPA2,
            sectionContentList = emptyList(),
            icon = ICON
        )
        val sshKey = ItemContents.SSHKey(
            title = "t",
            note = "",
            customFields = emptyList(),
            publicKey = "public",
            privateKey = empty,
            sectionContentList = emptyList(),
            icon = ICON
        )

        assertThat(ItemFormState(custom).toItemContents().icon).isEqualTo(ICON)
        assertThat(ItemFormState(wifi).toItemContents().icon).isEqualTo(ICON)
        assertThat(ItemFormState(sshKey).toItemContents().icon).isEqualTo(ICON)
    }

    @Test
    fun `alias form state keeps the icon`() {
        val formState = AliasItemFormState(customFields = emptyList(), icon = ICON)
        assertThat(formState.toItemContents().icon).isEqualTo(ICON)
    }

    @Test
    fun `identity form state maps the icon`() {
        val formState = IdentityItemFormState.default(FakeEncryptionContext).copy(icon = ICON)
        assertThat(formState.toItemContents().icon).isEqualTo(ICON)
    }

    @Test
    fun `login form state maps the icon and detects icon changes`() {
        val default = LoginItemFormState.default(FakeEncryptionContext)
        val withIcon = default.copy(icon = ICON)

        assertThat(withIcon.toItemContents(FakeEmailValidator()).icon).isEqualTo(ICON)
        assertThat(default.toItemContents(FakeEmailValidator()).icon).isNull()
        assertThat(withIcon.compare(default, FakeEncryptionContext)).isFalse()
        assertThat(withIcon.compare(withIcon.copy(), FakeEncryptionContext)).isTrue()
    }

    private companion object {
        const val ICON = "data:image/png;base64,QUJD"
    }
}
