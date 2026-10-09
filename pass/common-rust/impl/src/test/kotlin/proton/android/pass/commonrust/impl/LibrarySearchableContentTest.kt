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
import proton.android.pass.commonrust.impl.ItemTestData.concealed
import proton.android.pass.commonrust.impl.ItemTestData.encryptionContext
import proton.android.pass.domain.AddressDetailsContent
import proton.android.pass.domain.ContactDetailsContent
import proton.android.pass.domain.CreditCardType
import proton.android.pass.domain.CustomFieldContent
import proton.android.pass.domain.ExtraSectionContent
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.PersonalDetailsContent
import proton.android.pass.domain.WifiSecurityType
import proton.android.pass.domain.WorkDetailsContent

class LibrarySearchableContentTest {

    @Test
    fun `login indexes identifiers but never password or totp`() {
        val result = extract(
            login(
                email = "user@example.com",
                username = "johndoe",
                password = SECRET_PASSWORD,
                urls = listOf("https://example.com"),
                totpUri = "otpauth://totp/Example?secret=$SECRET_TOTP_SEED"
            )
        )

        assertThat(result).containsAtLeast("user@example.com", "johndoe", "https://example.com")
        assertThat(result.joinToString(" ")).doesNotContain(SECRET_PASSWORD)
        assertThat(result.joinToString(" ")).doesNotContain(SECRET_TOTP_SEED)
    }

    @Test
    fun `custom fields index name and TEXT value but not HIDDEN TOTP or DATE values`() {
        val result = extract(
            login(
                customFields = listOf(
                    CustomFieldContent.Text(label = "Recovery code label", value = "visible text value"),
                    CustomFieldContent.Hidden(label = "Backup PIN", value = concealed(SECRET_HIDDEN_VALUE)),
                    CustomFieldContent.Totp(
                        label = "Authenticator",
                        value = concealed("otpauth://totp/x?secret=$SECRET_TOTP_SEED")
                    ),
                    CustomFieldContent.Date(label = "Birthday", value = DATE_MILLIS)
                )
            )
        )

        assertThat(result).containsAtLeast(
            "Recovery code label",
            "visible text value",
            "Backup PIN",
            "Authenticator",
            "Birthday"
        )
        assertThat(result.joinToString(" ")).doesNotContain(SECRET_HIDDEN_VALUE)
        assertThat(result.joinToString(" ")).doesNotContain(SECRET_TOTP_SEED)
        assertThat(result.joinToString(" ")).doesNotContain((DATE_MILLIS / MILLIS_IN_SECOND).toString())
    }

    @Test
    fun `credit card indexes cardholder name but never number cvv or pin`() {
        val result = extract(
            ItemContents.CreditCard(
                title = "",
                note = "",
                customFields = emptyList(),
                cardHolder = "Jane Cardholder",
                type = CreditCardType.Other,
                number = SECRET_CARD_NUMBER,
                cvv = concealed(SECRET_CVV),
                pin = concealed(SECRET_PIN),
                expirationDate = ""
            )
        )

        assertThat(result).containsExactly("Jane Cardholder")
    }

    @Test
    fun `wifi indexes ssid but never password`() {
        val result = extract(
            ItemContents.WifiNetwork(
                title = "",
                note = "",
                customFields = emptyList(),
                ssid = "MyHomeNetwork",
                password = concealed(SECRET_WIFI_PASSWORD),
                wifiSecurityType = WifiSecurityType.Unknown,
                sectionContentList = emptyList()
            )
        )

        assertThat(result).containsExactly("MyHomeNetwork")
    }

    @Test
    fun `ssh key indexes section fields but never the private key`() {
        val result = extract(
            ItemContents.SSHKey(
                title = "",
                note = "",
                customFields = emptyList(),
                publicKey = "ssh-ed25519 AAAA-public",
                privateKey = concealed(SECRET_SSH_PRIVATE_KEY),
                sectionContentList = listOf(
                    ExtraSectionContent(
                        title = "",
                        customFieldList = listOf(CustomFieldContent.Text(label = "Host label", value = "prod-server"))
                    )
                )
            )
        )

        assertThat(result).containsExactly("Host label", "prod-server")
    }

    @Test
    fun `identity indexes top level custom fields but never the social security number`() {
        val result = extract(
            ItemContents.Identity(
                title = "",
                note = "",
                customFields = listOf(CustomFieldContent.Text(label = "Top level label", value = "top level value")),
                personalDetailsContent = PersonalDetailsContent.EMPTY.copy(fullName = "Jane Doe"),
                addressDetailsContent = AddressDetailsContent.EMPTY,
                contactDetailsContent = ContactDetailsContent.default(encryptionContext::encrypt).copy(
                    socialSecurityNumber = concealed(SECRET_SSN)
                ),
                workDetailsContent = WorkDetailsContent.EMPTY,
                extraSectionContentList = emptyList()
            )
        )

        assertThat(result).containsAtLeast("Jane Doe", "Top level label", "top level value")
        assertThat(result.joinToString(" ")).doesNotContain(SECRET_SSN)
    }

    private fun extract(contents: ItemContents): List<String> =
        contents.toLibraryItemData(itemUuid = "uuid", existing = null, encryptionContext = encryptionContext)
            .searchableContent()

    private fun login(
        email: String = "",
        username: String = "",
        password: String = "",
        urls: List<String> = emptyList(),
        totpUri: String = "",
        customFields: List<CustomFieldContent> = emptyList()
    ): ItemContents.Login = ItemContents.Login(
        title = "",
        note = "",
        customFields = customFields,
        itemEmail = email,
        itemUsername = username,
        password = concealed(password),
        urls = urls,
        packageInfoSet = emptySet(),
        primaryTotp = concealed(totpUri),
        passkeys = emptyList(),
        autofillUrls = emptyList()
    )

    private companion object {
        const val SECRET_PASSWORD = "p4ssw0rd-do-not-index"
        const val SECRET_TOTP_SEED = "TOTPSEEDDONOTINDEX"
        const val SECRET_HIDDEN_VALUE = "hidden-pin-do-not-index"
        const val SECRET_CARD_NUMBER = "4111222233334444"
        const val SECRET_CVV = "987"
        const val SECRET_PIN = "6543"
        const val SECRET_WIFI_PASSWORD = "wifi-pass-do-not-index"
        const val SECRET_SSH_PRIVATE_KEY = "-----BEGIN-PRIVATE-KEY-do-not-index-----"
        const val SECRET_SSN = "123-45-6789"
        const val DATE_MILLIS = 1_699_920_000_000L
    }
}
