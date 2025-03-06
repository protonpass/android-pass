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

package proton.android.pass.features.itemcreate.common

import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.domain.CustomFieldContent
import proton.android.pass.domain.ExtraSectionContent
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.ItemContents

private fun CustomFieldContent.compareDecrypted(b: CustomFieldContent, context: EncryptionContext): Int = when {
    this is CustomFieldContent.Text && b is CustomFieldContent.Text ->
        this.value.compareTo(b.value)

    this is CustomFieldContent.Hidden && b is CustomFieldContent.Hidden ->
        this.value.compareDecrypted(b.value, context)

    this is CustomFieldContent.Totp && b is CustomFieldContent.Totp ->
        this.value.compareDecrypted(b.value, context)

    else -> this::class.simpleName!!.compareTo(b::class.simpleName!!)
}

private fun List<CustomFieldContent>.compareDecrypted(
    other: List<CustomFieldContent>,
    context: EncryptionContext
): Int = compareLists(this, other) { a, b -> a.compareDecrypted(b, context) }

private fun HiddenState.compareDecrypted(other: HiddenState, context: EncryptionContext): Int {
    val (decryptedThis, decryptedOther) = context.decrypt(this.encrypted) to context.decrypt(other.encrypted)
    return decryptedThis.compareTo(decryptedOther)
}

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
    context: EncryptionContext
): Int = compareLists(this, other) { a, b ->
    when {
        a.title != b.title -> a.title.compareTo(b.title)
        else -> a.customFieldList.compareDecrypted(b.customFieldList, context)
    }
}

fun areItemContentsEqual(
    a: ItemContents,
    b: ItemContents,
    encryptionContext: EncryptionContext
): Boolean = when {
    a is ItemContents.Login && b is ItemContents.Login ->
        areLoginItemsEqual(a, b, encryptionContext)

    a is ItemContents.Note && b is ItemContents.Note ->
        areNoteItemsEqual(a, b)

    a is ItemContents.Alias && b is ItemContents.Alias ->
        areAliasItemsEqual(a, b)

    a is ItemContents.CreditCard && b is ItemContents.CreditCard ->
        areCreditCardItemsEqual(a, b, encryptionContext)

    a is ItemContents.Identity && b is ItemContents.Identity ->
        areIdentityItemsEqual(a, b, encryptionContext)

    a is ItemContents.Custom && b is ItemContents.Custom ->
        areCustomItemEqual(a, b, encryptionContext)

    a is ItemContents.WifiNetwork && b is ItemContents.WifiNetwork ->
        areWifiNetworkItemEqual(a, b, encryptionContext)

    a is ItemContents.SSHKey && b is ItemContents.SSHKey ->
        areSSHKeyItemEqual(a, b, encryptionContext)

    else -> false
}

private fun areSSHKeyItemEqual(
    a: ItemContents.SSHKey,
    b: ItemContents.SSHKey,
    encryptionContext: EncryptionContext
) = a.title == b.title &&
    a.note == b.note &&
    a.publicKey == b.publicKey &&
    a.privateKey.compareDecrypted(b.privateKey, encryptionContext) == 0 &&
    a.customFieldList.compareDecrypted(b.customFieldList, encryptionContext) == 0 &&
    a.sectionContentList.compareSections(b.sectionContentList, encryptionContext) == 0

private fun areWifiNetworkItemEqual(
    a: ItemContents.WifiNetwork,
    b: ItemContents.WifiNetwork,
    encryptionContext: EncryptionContext
) = a.title == b.title &&
    a.note == b.note &&
    a.ssid == b.ssid &&
    a.password.compareDecrypted(b.password, encryptionContext) == 0 &&
    a.customFieldList.compareDecrypted(b.customFieldList, encryptionContext) == 0 &&
    a.sectionContentList.compareSections(b.sectionContentList, encryptionContext) == 0

private fun areCustomItemEqual(
    a: ItemContents.Custom,
    b: ItemContents.Custom,
    encryptionContext: EncryptionContext
) = a.title == b.title &&
    a.note == b.note &&
    a.customFieldList.compareDecrypted(b.customFieldList, encryptionContext) == 0 &&
    a.sectionContentList.compareSections(b.sectionContentList, encryptionContext) == 0

private fun areIdentityItemsEqual(
    a: ItemContents.Identity,
    b: ItemContents.Identity,
    encryptionContext: EncryptionContext
) = a.title == b.title &&
    a.note == b.note &&
    a.personalDetailsContent.copy(customFields = emptyList()) == b.personalDetailsContent.copy(
        customFields = emptyList()
    ) &&
    a.personalDetailsContent.customFields.compareDecrypted(
        b.personalDetailsContent.customFields,
        encryptionContext
    ) == 0 &&
    a.addressDetailsContent.copy(customFields = emptyList()) == b.addressDetailsContent.copy(
        customFields = emptyList()
    ) &&
    a.addressDetailsContent.customFields.compareDecrypted(
        b.addressDetailsContent.customFields,
        encryptionContext
    ) == 0 &&
    a.contactDetailsContent.copy(customFields = emptyList()) == b.contactDetailsContent.copy(
        customFields = emptyList()
    ) &&
    a.contactDetailsContent.customFields.compareDecrypted(
        b.contactDetailsContent.customFields,
        encryptionContext
    ) == 0 &&
    a.workDetailsContent.copy(customFields = emptyList()) == b.workDetailsContent.copy(customFields = emptyList()) &&
    a.workDetailsContent.customFields.compareDecrypted(
        b.workDetailsContent.customFields,
        encryptionContext
    ) == 0 &&
    a.extraSectionContentList.compareSections(b.extraSectionContentList, encryptionContext) == 0

private fun areCreditCardItemsEqual(
    a: ItemContents.CreditCard,
    b: ItemContents.CreditCard,
    encryptionContext: EncryptionContext
) = a.title == b.title &&
    a.note == b.note &&
    a.cardHolder == b.cardHolder &&
    a.type == b.type &&
    a.number == b.number &&
    a.cvv.compareDecrypted(b.cvv, encryptionContext) == 0 &&
    a.pin.compareDecrypted(b.pin, encryptionContext) == 0 &&
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
    encryptionContext: EncryptionContext
) = a.title == b.title &&
    a.note == b.note &&
    a.itemEmail == b.itemEmail &&
    a.itemUsername == b.itemUsername &&
    a.password.compareDecrypted(b.password, encryptionContext) == 0 &&
    a.primaryTotp.compareDecrypted(b.primaryTotp, encryptionContext) == 0 &&
    a.customFields.compareDecrypted(b.customFields, encryptionContext) == 0 &&
    a.urls == b.urls &&
    a.packageInfoSet == b.packageInfoSet &&
    a.passkeys == b.passkeys
