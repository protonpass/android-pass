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

import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.data.impl.db.entities.ItemEntity
import proton.android.pass.log.api.PassLogger
import proton_pass_item_v1.ItemV1

/**
 * Decides which decrypted item fields become plaintext in the (SQLCipher-encrypted) search
 * index. This is the data-at-rest exposure surface: only searchable identifiers and metadata
 * are returned here. Secret-bearing fields are deliberately excluded:
 *  - login password and TOTP, credit-card number/CVV/expiry, WiFi password, SSH private key
 *    are never read;
 *  - custom fields contribute their *name* but only TEXT values — HIDDEN / TOTP / TIMESTAMP
 *    values are skipped (see [addExtraFields]).
 *
 * Adding a secret field here leaks plaintext into the index, so the contract is locked down by
 * SearchableContentExtractorTest.
 */
internal object SearchableContentExtractor {

    private const val TAG = "SearchableContentExtractor"

    @Suppress("TooGenericExceptionCaught", "LongMethod")
    fun extract(item: ItemEntity, context: EncryptionContext): String {
        var decrypted: ByteArray? = null
        return try {
            val bytes = context.decrypt(item.encryptedContent)
            decrypted = bytes
            val parsed = ItemV1.Item.parseFrom(bytes)
            val parts = mutableListOf<String>()

            // Top-level custom fields (all item types)
            addExtraFields(parts, parsed.extraFieldsList)

            when {
                parsed.hasContent() && parsed.content.hasLogin() -> {
                    val login = parsed.content.login
                    parts.addIfNotBlank(login.itemEmail)
                    parts.addIfNotBlank(login.itemUsername)
                    login.urlsList.forEach { parts.addIfNotBlank(it) }
                }
                parsed.hasContent() && parsed.content.hasAlias() -> {
                    item.aliasEmail?.let { parts.addIfNotBlank(it) }
                    item.slNote?.let { parts.addIfNotBlank(context.decrypt(it)) }
                }
                parsed.hasContent() && parsed.content.hasCreditCard() -> {
                    parts.addIfNotBlank(parsed.content.creditCard.cardholderName)
                }
                parsed.hasContent() && parsed.content.hasIdentity() -> {
                    val identity = parsed.content.identity
                    // Personal details
                    parts.addIfNotBlank(identity.fullName)
                    parts.addIfNotBlank(identity.firstName)
                    parts.addIfNotBlank(identity.middleName)
                    parts.addIfNotBlank(identity.lastName)
                    parts.addIfNotBlank(identity.birthdate)
                    parts.addIfNotBlank(identity.gender)
                    parts.addIfNotBlank(identity.email)
                    parts.addIfNotBlank(identity.phoneNumber)
                    addExtraFields(parts, identity.extraPersonalDetailsList)
                    // Address details
                    parts.addIfNotBlank(identity.organization)
                    parts.addIfNotBlank(identity.streetAddress)
                    parts.addIfNotBlank(identity.zipOrPostalCode)
                    parts.addIfNotBlank(identity.city)
                    parts.addIfNotBlank(identity.stateOrProvince)
                    parts.addIfNotBlank(identity.countryOrRegion)
                    parts.addIfNotBlank(identity.floor)
                    parts.addIfNotBlank(identity.county)
                    addExtraFields(parts, identity.extraAddressDetailsList)
                    // Contact details
                    parts.addIfNotBlank(identity.passportNumber)
                    parts.addIfNotBlank(identity.licenseNumber)
                    parts.addIfNotBlank(identity.website)
                    parts.addIfNotBlank(identity.xHandle)
                    parts.addIfNotBlank(identity.secondPhoneNumber)
                    parts.addIfNotBlank(identity.linkedin)
                    parts.addIfNotBlank(identity.reddit)
                    parts.addIfNotBlank(identity.facebook)
                    parts.addIfNotBlank(identity.yahoo)
                    parts.addIfNotBlank(identity.instagram)
                    addExtraFields(parts, identity.extraContactDetailsList)
                    // Work details
                    parts.addIfNotBlank(identity.company)
                    parts.addIfNotBlank(identity.jobTitle)
                    parts.addIfNotBlank(identity.personalWebsite)
                    parts.addIfNotBlank(identity.workPhoneNumber)
                    parts.addIfNotBlank(identity.workEmail)
                    addExtraFields(parts, identity.extraWorkDetailsList)
                    // Extra sections
                    identity.extraSectionsList.forEach { section ->
                        addExtraFields(parts, section.sectionFieldsList)
                    }
                }
                parsed.hasContent() && parsed.content.hasWifi() -> {
                    val wifi = parsed.content.wifi
                    parts.addIfNotBlank(wifi.ssid)
                    wifi.sectionsList.forEach { section ->
                        addExtraFields(parts, section.sectionFieldsList)
                    }
                }
                parsed.hasContent() && parsed.content.hasSshKey() -> {
                    val sshKey = parsed.content.sshKey
                    sshKey.sectionsList.forEach { section ->
                        addExtraFields(parts, section.sectionFieldsList)
                    }
                }
                parsed.hasContent() && parsed.content.hasCustom() -> {
                    val custom = parsed.content.custom
                    custom.sectionsList.forEach { section ->
                        addExtraFields(parts, section.sectionFieldsList)
                    }
                }
            }

            parts.joinToString(" ")
        } catch (e: Exception) {
            PassLogger.d(TAG, "Could not extract searchable content for ${item.id}: ${e.message}")
            ""
        } finally {
            decrypted?.fill(0)
        }
    }

    private fun addExtraFields(parts: MutableList<String>, fields: List<ItemV1.ExtraField>) {
        fields.forEach { field ->
            parts.addIfNotBlank(field.fieldName)
            when (field.contentCase) {
                ItemV1.ExtraField.ContentCase.TEXT -> parts.addIfNotBlank(field.text.content)
                else -> { /* Skip hidden, totp, timestamp for search */ }
            }
        }
    }

    private fun MutableList<String>.addIfNotBlank(value: String) {
        if (value.isNotBlank()) add(value)
    }
}
