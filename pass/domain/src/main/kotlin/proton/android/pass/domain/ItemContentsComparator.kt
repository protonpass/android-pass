/*
 * Copyright (c) 2025 Proton AG
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

import me.proton.core.crypto.common.keystore.EncryptedString

private fun HiddenState.compareDecrypted(other: HiddenState, decrypt: (EncryptedString) -> String): Int {
    val (decryptedThis, decryptedOther) = decrypt(this.encrypted) to decrypt(other.encrypted)
    return decryptedThis.compareTo(decryptedOther)
}

private fun CustomFieldContent.compareDecrypted(b: CustomFieldContent, decrypt: (EncryptedString) -> String): Int {
    val labelsComparison = this.label.compareTo(b.label)
    if (labelsComparison != 0) return labelsComparison

    return when {
        this is CustomFieldContent.Text && b is CustomFieldContent.Text ->
            this.value.compareTo(b.value)

        this is CustomFieldContent.Hidden && b is CustomFieldContent.Hidden ->
            this.value.compareDecrypted(b.value, decrypt)

        this is CustomFieldContent.Totp && b is CustomFieldContent.Totp ->
            this.value.compareDecrypted(b.value, decrypt)

        else -> this::class.simpleName!!.compareTo(b::class.simpleName!!)
    }
}

private fun List<CustomFieldContent>.compareDecrypted(
    other: List<CustomFieldContent>,
    decrypt: (EncryptedString) -> String
): Int = compareLists(this, other) { a, b -> a.compareDecrypted(b, decrypt) }

private fun <T> compareLists(
    a: List<T>,
    b: List<T>,
    comparator: (T, T) -> Int
): Int {
    val sizeComparison = a.size.compareTo(b.size)
    if (sizeComparison != 0) return sizeComparison
    return a.zip(b).map { (x, y) -> comparator(x, y) }.firstOrNull { it != 0 } ?: 0
}

private fun List<ExtraSectionContent>.compareSections(
    other: List<ExtraSectionContent>,
    decrypt: (EncryptedString) -> String
): Int = compareLists(this, other) { a, b ->
    a.title.compareTo(b.title).takeIf { it != 0 }
        ?: a.customFieldList.compareDecrypted(b.customFieldList, decrypt)
}

fun areItemContentsEqual(
    a: ItemContents,
    b: ItemContents,
    decrypt: (EncryptedString) -> String
): Boolean = when {
    a is ItemContents.Login && b is ItemContents.Login ->
        areLoginItemsEqual(a, b, decrypt)

    a is ItemContents.Note && b is ItemContents.Note ->
        areNoteItemsEqual(a, b)

    a is ItemContents.Alias && b is ItemContents.Alias ->
        areAliasItemsEqual(a, b)

    a is ItemContents.CreditCard && b is ItemContents.CreditCard ->
        areCreditCardItemsEqual(a, b, decrypt)

    a is ItemContents.Identity && b is ItemContents.Identity ->
        areIdentityItemsEqual(a, b, decrypt)

    a is ItemContents.Custom && b is ItemContents.Custom ->
        areCustomItemEqual(a, b, decrypt)

    a is ItemContents.WifiNetwork && b is ItemContents.WifiNetwork ->
        areWifiNetworkItemEqual(a, b, decrypt)

    a is ItemContents.SSHKey && b is ItemContents.SSHKey ->
        areSSHKeyItemEqual(a, b, decrypt)

    else -> false
}

private fun areSSHKeyItemEqual(
    a: ItemContents.SSHKey,
    b: ItemContents.SSHKey,
    decrypt: (EncryptedString) -> String
) = a.title == b.title &&
    a.note == b.note &&
    a.publicKey == b.publicKey &&
    a.privateKey.compareDecrypted(b.privateKey, decrypt) == 0 &&
    a.customFieldList.compareDecrypted(b.customFieldList, decrypt) == 0 &&
    a.sectionContentList.compareSections(b.sectionContentList, decrypt) == 0

private fun areWifiNetworkItemEqual(
    a: ItemContents.WifiNetwork,
    b: ItemContents.WifiNetwork,
    decrypt: (EncryptedString) -> String
) = a.title == b.title &&
    a.note == b.note &&
    a.ssid == b.ssid &&
    a.wifiSecurityType == b.wifiSecurityType &&
    a.password.compareDecrypted(b.password, decrypt) == 0 &&
    a.customFieldList.compareDecrypted(b.customFieldList, decrypt) == 0 &&
    a.sectionContentList.compareSections(b.sectionContentList, decrypt) == 0

private fun areCustomItemEqual(
    a: ItemContents.Custom,
    b: ItemContents.Custom,
    decrypt: (EncryptedString) -> String
) = a.title == b.title &&
    a.note == b.note &&
    a.customFieldList.compareDecrypted(b.customFieldList, decrypt) == 0 &&
    a.sectionContentList.compareSections(b.sectionContentList, decrypt) == 0

private fun areIdentityItemsEqual(
    a: ItemContents.Identity,
    b: ItemContents.Identity,
    decrypt: (EncryptedString) -> String
) = a.title == b.title &&
    a.note == b.note &&
    a.personalDetailsContent.copy(customFields = emptyList()) ==
    b.personalDetailsContent.copy(customFields = emptyList()) &&
    a.personalDetailsContent.customFields
        .compareDecrypted(b.personalDetailsContent.customFields, decrypt) == 0 &&
    a.addressDetailsContent.copy(customFields = emptyList()) == b.addressDetailsContent.copy(
        customFields = emptyList()
    ) &&
    a.addressDetailsContent.customFields
        .compareDecrypted(b.addressDetailsContent.customFields, decrypt) == 0 &&
    a.contactDetailsContent.copy(customFields = emptyList()) ==
    b.contactDetailsContent.copy(customFields = emptyList()) &&
    a.contactDetailsContent.customFields
        .compareDecrypted(b.contactDetailsContent.customFields, decrypt) == 0 &&
    a.workDetailsContent.copy(customFields = emptyList()) ==
    b.workDetailsContent.copy(customFields = emptyList()) &&
    a.workDetailsContent.customFields
        .compareDecrypted(b.workDetailsContent.customFields, decrypt) == 0 &&
    a.extraSectionContentList.compareSections(b.extraSectionContentList, decrypt) == 0

private fun areCreditCardItemsEqual(
    a: ItemContents.CreditCard,
    b: ItemContents.CreditCard,
    decrypt: (EncryptedString) -> String
) = a.title == b.title &&
    a.note == b.note &&
    a.cardHolder == b.cardHolder &&
    a.type == b.type &&
    a.number == b.number &&
    a.cvv.compareDecrypted(b.cvv, decrypt) == 0 &&
    a.pin.compareDecrypted(b.pin, decrypt) == 0 &&
    a.expirationDate == b.expirationDate

private fun areAliasItemsEqual(a: ItemContents.Alias, b: ItemContents.Alias) = a.title == b.title &&
    a.note == b.note &&
    a.aliasEmail == b.aliasEmail &&
    a.isEnabled == b.isEnabled

private fun areNoteItemsEqual(a: ItemContents.Note, b: ItemContents.Note) = a.title == b.title &&
    a.note == b.note

private fun areLoginItemsEqual(
    a: ItemContents.Login,
    b: ItemContents.Login,
    decrypt: (String) -> String
) = a.title == b.title &&
    a.note == b.note &&
    a.itemEmail == b.itemEmail &&
    a.itemUsername == b.itemUsername &&
    a.password.compareDecrypted(b.password, decrypt) == 0 &&
    a.primaryTotp.compareDecrypted(b.primaryTotp, decrypt) == 0 &&
    a.customFields.compareDecrypted(b.customFields, decrypt) == 0 &&
    compareLists(a.urls, b.urls) { urlA, urlB -> urlA.compareTo(urlB) } == 0 &&
    a.packageInfoSet == b.packageInfoSet &&
    a.passkeys == b.passkeys
