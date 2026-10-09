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

package proton.android.pass.data.impl.repositories

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import proton.android.pass.commonrust.fakes.FakeItemContentSerializer
import proton.android.pass.crypto.fakes.context.FakeEncryptionContext
import proton.android.pass.data.impl.db.entities.ItemEntity
import proton.android.pass.domain.CreditCardType
import proton.android.pass.domain.CustomFieldContent
import proton.android.pass.domain.ExtraSectionContent
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.WifiSecurityType

/**
 * Security regression guard for what plaintext lands in the search index.
 *
 * The search DB is SQLCipher-encrypted, but its contents are decrypted item fields. This test
 * locks down the contract that secret-bearing fields are never indexed, while searchable
 * identifiers are. If someone later "improves search" by indexing a password, or the proto
 * evolves so a secret slips into an indexed path, one of these assertions fails.
 */
class SearchableContentExtractorTest {

    @Test
    fun `login indexes identifiers but never password or totp`() {
        val item = item(
            contents = login(
                email = "user@example.com",
                username = "johndoe",
                password = SECRET_PASSWORD,
                urls = listOf("https://example.com"),
                totpUri = "otpauth://totp/Example?secret=$SECRET_TOTP_SEED"
            )
        )

        val result = extract(item)

        assertThat(result).contains("user@example.com")
        assertThat(result).contains("johndoe")
        assertThat(result).contains("https://example.com")
        assertThat(result).doesNotContain(SECRET_PASSWORD)
        assertThat(result).doesNotContain(SECRET_TOTP_SEED)
    }

    @Test
    fun `alias indexes email and SimpleLogin note`() {
        val item = item(
            contents = ItemContents.Alias(
                title = "",
                note = "",
                customFields = emptyList(),
                aliasEmail = "alias@passmail.com"
            )
        )

        val result = extract(
            item = item,
            aliasEmail = "alias@passmail.com",
            slNote = "shopping newsletters"
        )

        assertThat(result).contains("alias@passmail.com")
        assertThat(result).contains("shopping newsletters")
    }

    @Test
    fun `custom fields index name and TEXT value but not HIDDEN or TOTP values`() {
        val item = item(
            contents = login(
                customFields = listOf(
                    CustomFieldContent.Text(label = "Recovery code label", value = "visible text value"),
                    CustomFieldContent.Hidden(label = "Backup PIN", value = concealed(SECRET_HIDDEN_VALUE)),
                    CustomFieldContent.Totp(
                        label = "Authenticator",
                        value = concealed("otpauth://totp/x?secret=$SECRET_TOTP_SEED")
                    )
                )
            )
        )

        val result = extract(item)

        // Field names and TEXT values are searchable.
        assertThat(result).contains("Recovery code label")
        assertThat(result).contains("visible text value")
        assertThat(result).contains("Backup PIN")
        assertThat(result).contains("Authenticator")
        // The secret values behind HIDDEN / TOTP fields are not.
        assertThat(result).doesNotContain(SECRET_HIDDEN_VALUE)
        assertThat(result).doesNotContain(SECRET_TOTP_SEED)
    }

    @Test
    fun `credit card indexes cardholder name but never number or cvv`() {
        val item = item(
            contents = ItemContents.CreditCard(
                title = "",
                note = "",
                customFields = emptyList(),
                cardHolder = "Jane Cardholder",
                type = CreditCardType.Other,
                number = SECRET_CARD_NUMBER,
                cvv = concealed(SECRET_CVV),
                pin = HiddenState.Empty(FakeEncryptionContext.encrypt("")),
                expirationDate = ""
            )
        )

        val result = extract(item)

        assertThat(result).contains("Jane Cardholder")
        assertThat(result).doesNotContain(SECRET_CARD_NUMBER)
        assertThat(result).doesNotContain(SECRET_CVV)
    }

    @Test
    fun `wifi indexes ssid but never password`() {
        val item = item(
            contents = ItemContents.WifiNetwork(
                title = "",
                note = "",
                customFields = emptyList(),
                ssid = "MyHomeNetwork",
                password = concealed(SECRET_WIFI_PASSWORD),
                wifiSecurityType = WifiSecurityType.Unknown,
                sectionContentList = emptyList()
            )
        )

        val result = extract(item)

        assertThat(result).contains("MyHomeNetwork")
        assertThat(result).doesNotContain(SECRET_WIFI_PASSWORD)
    }

    @Test
    fun `ssh key indexes custom section fields but never the private key`() {
        val item = item(
            contents = ItemContents.SSHKey(
                title = "",
                note = "",
                customFields = emptyList(),
                publicKey = "ssh-ed25519 AAAA-public",
                privateKey = concealed(SECRET_SSH_PRIVATE_KEY),
                sectionContentList = listOf(
                    ExtraSectionContent(
                        title = "",
                        customFieldList = listOf(
                            CustomFieldContent.Text(label = "Host label", value = "prod-server")
                        )
                    )
                )
            )
        )

        val result = extract(item)

        assertThat(result).contains("Host label")
        assertThat(result).contains("prod-server")
        assertThat(result).doesNotContain(SECRET_SSH_PRIVATE_KEY)
    }

    private fun extract(
        item: ItemContents,
        aliasEmail: String? = null,
        slNote: String? = null
    ): String {
        val serializer = FakeItemContentSerializer()
        return SearchableContentExtractor.extract(
            itemEntity(item, serializer, aliasEmail, slNote),
            FakeEncryptionContext,
            serializer
        )
    }

    private fun item(contents: ItemContents): ItemContents = contents

    private fun concealed(value: String): HiddenState = HiddenState.Concealed(FakeEncryptionContext.encrypt(value))

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

    private fun itemEntity(
        item: ItemContents,
        serializer: FakeItemContentSerializer,
        aliasEmail: String?,
        slNote: String? = null
    ): ItemEntity {
        val itemBytes = serializer.serializeItem(item, FakeEncryptionContext, "test-uuid")
        return ItemEntity(
            id = "item-id",
            userId = "user-id",
            addressId = "address-id",
            shareId = "share-id",
            folderId = null,
            revision = 1L,
            contentFormatVersion = 1,
            keyRotation = 1L,
            content = "",
            key = null,
            state = 0,
            itemType = 0,
            aliasEmail = aliasEmail,
            slNote = slNote?.let(FakeEncryptionContext::encrypt),
            createTime = 0L,
            modifyTime = 0L,
            lastUsedTime = null,
            hasTotp = null,
            isPinned = false,
            pinTime = null,
            hasPasskeys = null,
            flags = 0,
            shareCount = 0,
            encryptedTitle = FakeEncryptionContext.encrypt(""),
            encryptedNote = FakeEncryptionContext.encrypt(""),
            encryptedContent = FakeEncryptionContext.encrypt(itemBytes),
            encryptedKey = null
        )
    }

    private companion object {
        const val SECRET_PASSWORD = "p4ssw0rd-do-not-index"
        const val SECRET_TOTP_SEED = "TOTPSEEDDONOTINDEX"
        const val SECRET_HIDDEN_VALUE = "hidden-pin-do-not-index"
        const val SECRET_CARD_NUMBER = "4111222233334444"
        const val SECRET_CVV = "987"
        const val SECRET_WIFI_PASSWORD = "wifi-pass-do-not-index"
        const val SECRET_SSH_PRIVATE_KEY = "-----BEGIN-PRIVATE-KEY-do-not-index-----"
    }
}
