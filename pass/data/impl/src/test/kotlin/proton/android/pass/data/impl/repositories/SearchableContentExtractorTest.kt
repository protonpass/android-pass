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
import proton.android.pass.crypto.fakes.context.FakeEncryptionContext
import proton.android.pass.data.impl.db.entities.ItemEntity
import proton_pass_item_v1.ItemV1

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
            content = ItemV1.Content.newBuilder().setLogin(
                ItemV1.ItemLogin.newBuilder()
                    .setItemEmail("user@example.com")
                    .setItemUsername("johndoe")
                    .addUrls("https://example.com")
                    .setPassword(SECRET_PASSWORD)
                    .setTotpUri("otpauth://totp/Example?secret=$SECRET_TOTP_SEED")
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
    fun `custom fields index name and TEXT value but not HIDDEN or TOTP values`() {
        val item = item(
            content = ItemV1.Content.newBuilder().setLogin(ItemV1.ItemLogin.newBuilder()),
            extraFields = listOf(
                ItemV1.ExtraField.newBuilder()
                    .setFieldName("Recovery code label")
                    .setText(ItemV1.ExtraTextField.newBuilder().setContent("visible text value"))
                    .build(),
                ItemV1.ExtraField.newBuilder()
                    .setFieldName("Backup PIN")
                    .setHidden(ItemV1.ExtraHiddenField.newBuilder().setContent(SECRET_HIDDEN_VALUE))
                    .build(),
                ItemV1.ExtraField.newBuilder()
                    .setFieldName("Authenticator")
                    .setTotp(ItemV1.ExtraTotp.newBuilder().setTotpUri("otpauth://totp/x?secret=$SECRET_TOTP_SEED"))
                    .build()
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
            content = ItemV1.Content.newBuilder().setCreditCard(
                ItemV1.ItemCreditCard.newBuilder()
                    .setCardholderName("Jane Cardholder")
                    .setNumber(SECRET_CARD_NUMBER)
                    .setVerificationNumber(SECRET_CVV)
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
            content = ItemV1.Content.newBuilder().setWifi(
                ItemV1.ItemWifi.newBuilder()
                    .setSsid("MyHomeNetwork")
                    .setPassword(SECRET_WIFI_PASSWORD)
            )
        )

        val result = extract(item)

        assertThat(result).contains("MyHomeNetwork")
        assertThat(result).doesNotContain(SECRET_WIFI_PASSWORD)
    }

    @Test
    fun `ssh key indexes custom section fields but never the private key`() {
        val item = item(
            content = ItemV1.Content.newBuilder().setSshKey(
                ItemV1.ItemSSHKey.newBuilder()
                    .setPrivateKey(SECRET_SSH_PRIVATE_KEY)
                    .setPublicKey("ssh-ed25519 AAAA-public")
                    .addSections(
                        ItemV1.CustomSection.newBuilder()
                            .addSectionFields(
                                ItemV1.ExtraField.newBuilder()
                                    .setFieldName("Host label")
                                    .setText(ItemV1.ExtraTextField.newBuilder().setContent("prod-server"))
                            )
                    )
            )
        )

        val result = extract(item)

        assertThat(result).contains("Host label")
        assertThat(result).contains("prod-server")
        assertThat(result).doesNotContain(SECRET_SSH_PRIVATE_KEY)
    }

    private fun extract(item: ItemV1.Item, aliasEmail: String? = null): String =
        SearchableContentExtractor.extract(itemEntity(item, aliasEmail), FakeEncryptionContext)

    private fun item(content: ItemV1.Content.Builder, extraFields: List<ItemV1.ExtraField> = emptyList()): ItemV1.Item =
        ItemV1.Item.newBuilder()
            .setContent(content)
            .addAllExtraFields(extraFields)
            .build()

    private fun itemEntity(item: ItemV1.Item, aliasEmail: String?): ItemEntity = ItemEntity(
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
        encryptedContent = FakeEncryptionContext.encrypt(item.toByteArray()),
        encryptedKey = null
    )

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
