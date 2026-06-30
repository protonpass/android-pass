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

package proton.android.pass.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ItemContentsBodyFieldsTest {

    // region CustomFieldContent.toDisplayString

    @Test
    fun `Text field returns label colon value`() {
        val field = CustomFieldContent.Text(label = "Note", value = "hello")
        assertThat(field.toDisplayString()).isEqualTo("Note: hello")
    }

    @Test
    fun `Text field with blank label still returns colon value`() {
        val field = CustomFieldContent.Text(label = "", value = "hello")
        assertThat(field.toDisplayString()).isEqualTo(": hello")
    }

    @Test
    fun `Text field with blank value still returns label colon`() {
        val field = CustomFieldContent.Text(label = "Note", value = "")
        assertThat(field.toDisplayString()).isEqualTo("Note: ")
    }

    @Test
    fun `Text field with both blank returns null`() {
        val field = CustomFieldContent.Text(label = "", value = "")
        assertThat(field.toDisplayString()).isNull()
    }

    @Test
    fun `Hidden field returns only label`() {
        val field = CustomFieldContent.Hidden(label = "Password", value = HiddenState.Empty(""))
        assertThat(field.toDisplayString()).isEqualTo("Password")
    }

    @Test
    fun `Hidden field with blank label returns null`() {
        val field = CustomFieldContent.Hidden(label = "", value = HiddenState.Empty(""))
        assertThat(field.toDisplayString()).isNull()
    }

    @Test
    fun `Totp field returns only label`() {
        val field = CustomFieldContent.Totp(label = "2FA", value = HiddenState.Empty(""))
        assertThat(field.toDisplayString()).isEqualTo("2FA")
    }

    @Test
    fun `Totp field with blank label returns null`() {
        val field = CustomFieldContent.Totp(label = "", value = HiddenState.Empty(""))
        assertThat(field.toDisplayString()).isNull()
    }

    @Test
    fun `Date field returns only label`() {
        val field = CustomFieldContent.Date(label = "Expiry", value = 1_700_000_000L)
        assertThat(field.toDisplayString()).isEqualTo("Expiry")
    }

    @Test
    fun `Date field with blank label returns null`() {
        val field = CustomFieldContent.Date(label = "", value = null)
        assertThat(field.toDisplayString()).isNull()
    }

    // endregion

    // region ExtraSectionContent.toHighlightableStrings

    @Test
    fun `section with title and text field returns both`() {
        val section = ExtraSectionContent(
            title = "Extra",
            customFieldList = listOf(CustomFieldContent.Text(label = "Key", value = "Val"))
        )
        assertThat(section.toHighlightableStrings()).containsExactly("Extra", "Key: Val")
    }

    @Test
    fun `section with blank title omits title`() {
        val section = ExtraSectionContent(
            title = "",
            customFieldList = listOf(CustomFieldContent.Text(label = "Key", value = "Val"))
        )
        assertThat(section.toHighlightableStrings()).containsExactly("Key: Val")
    }

    @Test
    fun `section with hidden field returns label only`() {
        val section = ExtraSectionContent(
            title = "Section",
            customFieldList = listOf(
                CustomFieldContent.Hidden(label = "Secret", value = HiddenState.Concealed("enc"))
            )
        )
        assertThat(section.toHighlightableStrings()).containsExactly("Section", "Secret")
    }

    // endregion

    // region highlightableBodyFields per ItemContents type

    @Test
    fun `Login returns text label-value and hidden-totp-date labels`() {
        val contents = loginContents(
            customFields = listOf(
                CustomFieldContent.Text(label = "Note", value = "memo"),
                CustomFieldContent.Hidden(label = "Pin", value = HiddenState.Empty("")),
                CustomFieldContent.Totp(label = "OTP", value = HiddenState.Empty("")),
                CustomFieldContent.Date(label = "Due", value = 0L)
            )
        )
        assertThat(contents.highlightableBodyFields())
            .containsExactly("Note: memo", "Pin", "OTP", "Due")
    }

    @Test
    fun `Note returns custom field strings`() {
        val contents = ItemContents.Note(
            title = "title",
            note = "body",
            customFields = listOf(
                CustomFieldContent.Text(label = "tag", value = "important"),
                CustomFieldContent.Hidden(label = "secret", value = HiddenState.Empty(""))
            )
        )
        assertThat(contents.highlightableBodyFields())
            .containsExactly("tag: important", "secret")
    }

    @Test
    fun `Alias returns custom field strings`() {
        val contents = ItemContents.Alias(
            title = "title",
            note = "",
            aliasEmail = "a@b.com",
            customFields = listOf(
                CustomFieldContent.Text(label = "nick", value = "coolalias")
            )
        )
        assertThat(contents.highlightableBodyFields()).containsExactly("nick: coolalias")
    }

    @Test
    fun `CreditCard returns custom field strings`() {
        val contents = ItemContents.CreditCard(
            title = "CC",
            note = "",
            cardHolder = "holder",
            type = CreditCardType.Other,
            number = "1234",
            cvv = HiddenState.Empty(""),
            pin = HiddenState.Empty(""),
            expirationDate = "",
            customFields = listOf(
                CustomFieldContent.Text(label = "nickname", value = "travel")
            )
        )
        assertThat(contents.highlightableBodyFields()).containsExactly("nickname: travel")
    }

    @Test
    fun `WifiNetwork returns ssid and section content`() {
        val contents = ItemContents.WifiNetwork(
            title = "Home",
            note = "",
            customFields = listOf(CustomFieldContent.Text(label = "provider", value = "ISP")),
            ssid = "MyNetwork",
            password = HiddenState.Empty(""),
            wifiSecurityType = WifiSecurityType.WPA2,
            sectionContentList = listOf(
                ExtraSectionContent(
                    title = "Extra",
                    customFieldList = listOf(CustomFieldContent.Text(label = "room", value = "office"))
                )
            )
        )
        assertThat(contents.highlightableBodyFields())
            .containsExactly("provider: ISP", "MyNetwork", "Extra", "room: office")
    }

    @Test
    fun `WifiNetwork with blank ssid omits ssid`() {
        val contents = ItemContents.WifiNetwork(
            title = "Home",
            note = "",
            customFields = emptyList(),
            ssid = "",
            password = HiddenState.Empty(""),
            wifiSecurityType = WifiSecurityType.WPA2,
            sectionContentList = emptyList()
        )
        assertThat(contents.highlightableBodyFields()).isEmpty()
    }

    @Test
    fun `SSHKey returns top-level and section custom fields`() {
        val contents = ItemContents.SSHKey(
            title = "key",
            note = "",
            customFields = listOf(CustomFieldContent.Text(label = "env", value = "prod")),
            publicKey = "pubkey",
            privateKey = HiddenState.Empty(""),
            sectionContentList = listOf(
                ExtraSectionContent(
                    title = "Config",
                    customFieldList = listOf(CustomFieldContent.Text(label = "host", value = "server"))
                )
            )
        )
        assertThat(contents.highlightableBodyFields())
            .containsExactly("env: prod", "Config", "host: server")
    }

    @Test
    fun `Custom returns top-level and section custom fields including section title`() {
        val contents = ItemContents.Custom(
            title = "My Item",
            note = "",
            customFields = listOf(CustomFieldContent.Text(label = "ref", value = "abc")),
            sectionContentList = listOf(
                ExtraSectionContent(
                    title = "Details",
                    customFieldList = listOf(
                        CustomFieldContent.Text(label = "color", value = "blue"),
                        CustomFieldContent.Hidden(label = "code", value = HiddenState.Empty(""))
                    )
                )
            )
        )
        assertThat(contents.highlightableBodyFields())
            .containsExactly("ref: abc", "Details", "color: blue", "code")
    }

    @Test
    fun `Identity returns custom fields from all four sections and extra sections`() {
        val contents = identityContents(
            personalCustomFields = listOf(CustomFieldContent.Text(label = "p", value = "pv")),
            addressCustomFields = listOf(CustomFieldContent.Text(label = "a", value = "av")),
            contactCustomFields = listOf(CustomFieldContent.Text(label = "c", value = "cv")),
            workCustomFields = listOf(CustomFieldContent.Text(label = "w", value = "wv")),
            topLevelCustomFields = listOf(CustomFieldContent.Text(label = "t", value = "tv")),
            extraSections = listOf(
                ExtraSectionContent(
                    title = "Extra",
                    customFieldList = listOf(CustomFieldContent.Text(label = "e", value = "ev"))
                )
            )
        )
        assertThat(contents.highlightableBodyFields())
            .containsExactly("t: tv", "p: pv", "a: av", "c: cv", "w: wv", "Extra", "e: ev")
    }

    @Test
    fun `Identity with hidden custom field in section returns label only`() {
        val contents = identityContents(
            personalCustomFields = listOf(
                CustomFieldContent.Hidden(label = "ssn", value = HiddenState.Concealed("enc"))
            )
        )
        assertThat(contents.highlightableBodyFields()).containsExactly("ssn")
    }

    @Test
    fun `no custom fields returns empty list`() {
        val login = loginContents(customFields = emptyList())
        assertThat(login.highlightableBodyFields()).isEmpty()

        val note = ItemContents.Note(title = "t", note = "n", customFields = emptyList())
        assertThat(note.highlightableBodyFields()).isEmpty()
    }

    // endregion

    // region helpers

    private fun loginContents(customFields: List<CustomFieldContent>) = ItemContents.Login(
        title = "title",
        note = "",
        itemEmail = "user@example.com",
        itemUsername = "user",
        password = HiddenState.Empty(""),
        urls = emptyList(),
        packageInfoSet = emptySet(),
        primaryTotp = HiddenState.Empty(""),
        customFields = customFields,
        passkeys = emptyList(),
        autofillUrls = emptyList()
    )

    private fun identityContents(
        topLevelCustomFields: List<CustomFieldContent> = emptyList(),
        personalCustomFields: List<CustomFieldContent> = emptyList(),
        addressCustomFields: List<CustomFieldContent> = emptyList(),
        contactCustomFields: List<CustomFieldContent> = emptyList(),
        workCustomFields: List<CustomFieldContent> = emptyList(),
        extraSections: List<ExtraSectionContent> = emptyList()
    ) = ItemContents.Identity(
        title = "title",
        note = "",
        customFields = topLevelCustomFields,
        personalDetailsContent = PersonalDetailsContent.EMPTY.copy(customFields = personalCustomFields),
        addressDetailsContent = AddressDetailsContent.EMPTY.copy(customFields = addressCustomFields),
        contactDetailsContent = ContactDetailsContent.default { it }.copy(customFields = contactCustomFields),
        workDetailsContent = WorkDetailsContent.EMPTY.copy(customFields = workCustomFields),
        extraSectionContentList = extraSections
    )

    // endregion
}
