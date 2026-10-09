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

package proton.android.pass.commonrust.fakes

import proton.android.pass.commonrust.api.ItemContentSerializer
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.domain.AddressDetails
import proton.android.pass.domain.AddressDetailsContent
import proton.android.pass.domain.AutofillUrl
import proton.android.pass.domain.AutofillUrlMode
import proton.android.pass.domain.ContactDetails
import proton.android.pass.domain.ContactDetailsContent
import proton.android.pass.domain.CustomField
import proton.android.pass.domain.CustomFieldContent
import proton.android.pass.domain.ExtraSection
import proton.android.pass.domain.ExtraSectionContent
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.ParsedItem
import proton.android.pass.domain.Passkey
import proton.android.pass.domain.PersonalDetails
import proton.android.pass.domain.PersonalDetailsContent
import proton.android.pass.domain.VaultProperties
import proton.android.pass.domain.WorkDetails
import proton.android.pass.domain.WorkDetailsContent
import proton.android.pass.domain.attachments.AttachmentContent
import proton.android.pass.domain.entity.PackageInfo
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import java.util.UUID

class FakeItemContentSerializer @Inject constructor() : ItemContentSerializer {

    private data class StoredItem(val itemUuid: String, val contents: ItemContents)

    override fun serializeItem(
        contents: ItemContents,
        encryptionContext: EncryptionContext,
        itemUuid: String
    ): ByteArray = storeItem(StoredItem(itemUuid, contents))

    override fun deserializeItem(
        bytes: ByteArray,
        encryptionContext: EncryptionContext,
        aliasEmail: String?
    ): ParsedItem {
        val stored = readItem(bytes)
        val contents = stored.contents
        return ParsedItem(
            itemUuid = stored.itemUuid,
            title = contents.title,
            note = contents.note,
            itemType = contents.toItemType(encryptionContext, aliasEmail),
            packageInfoSet = (contents as? ItemContents.Login)?.packageInfoSet.orEmpty()
        )
    }

    override fun deserializeItems(
        bytes: List<ByteArray>,
        encryptionContext: EncryptionContext,
        aliasEmails: List<String?>
    ): List<ParsedItem> = bytes.mapIndexed { index, item ->
        runCatching { deserializeItem(item, encryptionContext, aliasEmails[index]) }.getOrElse {
            ParsedItem(
                itemUuid = UUID.randomUUID().toString(),
                title = "",
                note = "",
                itemType = ItemType.Unknown,
                packageInfoSet = emptySet()
            )
        }
    }

    override fun extractSearchableContent(bytes: ByteArray): List<String> = readItem(bytes).contents.searchableContent()

    override fun performItemUpdate(
        original: ByteArray,
        contents: ItemContents,
        encryptionContext: EncryptionContext
    ): ByteArray = storeItem(StoredItem(readItem(original).itemUuid, contents))

    override fun addPackageInfoToItem(original: ByteArray, packageInfo: PackageInfo): ByteArray? {
        val stored = readItem(original)
        val login = stored.contents as? ItemContents.Login ?: return null
        val existing = login.packageInfoSet.firstOrNull { it.packageName == packageInfo.packageName }
        val updated = existing
            ?.copy(hashes = existing.hashes + packageInfo.hashes)
            ?: packageInfo
        if (existing == updated) return null
        val packageInfoSet = login.packageInfoSet.filterNot { it.packageName == packageInfo.packageName }
            .toSet() + updated
        return storeItem(stored.copy(contents = login.copy(packageInfoSet = packageInfoSet)))
    }

    override fun addUrlToItem(
        original: ByteArray,
        url: String,
        isAutofillUrlAdvancedModesEnabled: Boolean
    ): ByteArray {
        val stored = readItem(original)
        val login = stored.contents as? ItemContents.Login ?: return original
        val existingAutofillUrls = if (isAutofillUrlAdvancedModesEnabled && login.autofillUrls.isEmpty()) {
            login.urls.filter { it.isNotBlank() }.map { AutofillUrl(url = it, mode = AutofillUrlMode.Default) }
        } else {
            login.autofillUrls
        }
        val autofillUrls = if (isAutofillUrlAdvancedModesEnabled) {
            existingAutofillUrls + AutofillUrl(url = url, mode = AutofillUrlMode.Default)
        } else {
            existingAutofillUrls
        }
        return storeItem(
            stored.copy(contents = login.copy(urls = login.urls + url, autofillUrls = autofillUrls))
        )
    }

    override fun addPasskeyToItem(original: ByteArray, passkey: Passkey): ByteArray {
        val stored = readItem(original)
        val login = stored.contents as? ItemContents.Login ?: return original
        return storeItem(stored.copy(contents = login.copy(passkeys = login.passkeys + passkey)))
    }

    override fun serializeVault(vault: VaultProperties): ByteArray = store(vaults, VAULT_PREFIX, vault)

    override fun deserializeVault(bytes: ByteArray): VaultProperties = read(vaults, VAULT_PREFIX, bytes)

    override fun performVaultUpdate(original: ByteArray, updated: VaultProperties): ByteArray {
        read(vaults, VAULT_PREFIX, original)
        return serializeVault(updated)
    }

    override fun serializeFolder(name: String): ByteArray = store(folders, FOLDER_PREFIX, name)

    override fun deserializeFolder(bytes: ByteArray): String = read(folders, FOLDER_PREFIX, bytes)

    override fun deserializeFolders(bytes: List<ByteArray>): List<String?> =
        bytes.map { runCatching { deserializeFolder(it) }.getOrNull() }

    override fun performFolderUpdate(original: ByteArray, name: String): ByteArray {
        read(folders, FOLDER_PREFIX, original)
        return serializeFolder(name)
    }

    override fun serializeAttachmentContent(content: AttachmentContent): ByteArray =
        store(attachments, ATTACHMENT_PREFIX, content)

    override fun deserializeAttachmentContent(bytes: ByteArray): AttachmentContent =
        read(attachments, ATTACHMENT_PREFIX, bytes)

    override fun updateAttachmentName(original: ByteArray, name: String): ByteArray =
        serializeAttachmentContent(deserializeAttachmentContent(original).copy(name = name))

    private fun storeItem(item: StoredItem): ByteArray = store(items, ITEM_PREFIX, item)

    private fun readItem(bytes: ByteArray): StoredItem = read(items, ITEM_PREFIX, bytes)

    private fun <T : Any> store(
        map: ConcurrentHashMap<String, T>,
        prefix: String,
        value: T
    ): ByteArray {
        val key = "$prefix${counter.incrementAndGet()}"
        map[key] = value
        return key.encodeToByteArray()
    }

    private fun <T : Any> read(
        map: ConcurrentHashMap<String, T>,
        prefix: String,
        bytes: ByteArray
    ): T {
        val key = bytes.decodeToString()
        require(key.startsWith(prefix)) { "Bytes were not produced by FakeItemContentSerializer for $prefix" }
        return requireNotNull(map[key]) { "Unknown fake serialized bytes: $key" }
    }

    private fun ItemContents.toItemType(context: EncryptionContext, aliasEmail: String?): ItemType {
        fun hidden(state: HiddenState): String = when (state) {
            is HiddenState.Empty -> context.encrypt("")
            is HiddenState.Concealed -> context.encrypt(context.decrypt(state.encrypted))
            is HiddenState.Revealed -> context.encrypt(context.decrypt(state.encrypted))
        }

        fun field(content: CustomFieldContent): CustomField = when (content) {
            is CustomFieldContent.Text -> CustomField.Text(content.label, content.value)
            is CustomFieldContent.Hidden -> CustomField.Hidden(content.label, hidden(content.value))
            is CustomFieldContent.Totp -> CustomField.Totp(content.label, hidden(content.value))
            is CustomFieldContent.Date -> CustomField.Date(content.label, content.value)
        }

        fun fields(list: List<CustomFieldContent>): List<CustomField> = list.map(::field)

        fun sections(list: List<ExtraSectionContent>): List<ExtraSection> =
            list.map { ExtraSection(sectionName = it.title, customFields = fields(it.customFieldList)) }

        return when (this) {
            is ItemContents.Login -> ItemType.Login(
                itemEmail = itemEmail,
                itemUsername = itemUsername,
                password = hidden(password),
                websites = urls,
                packageInfoSet = packageInfoSet,
                primaryTotp = hidden(primaryTotp),
                customFields = fields(customFields),
                passkeys = passkeys,
                autofillUrls = autofillUrls
            )

            is ItemContents.Note -> ItemType.Note(text = note, customFields = fields(customFields))

            is ItemContents.Alias -> ItemType.Alias(
                aliasEmail = aliasEmail ?: this.aliasEmail,
                customFields = fields(customFields)
            )

            is ItemContents.CreditCard -> ItemType.CreditCard(
                cardHolder = cardHolder,
                number = context.encrypt(number),
                cvv = hidden(cvv),
                pin = hidden(pin),
                creditCardType = type,
                expirationDate = expirationDate,
                customFields = fields(customFields)
            )

            is ItemContents.Identity -> ItemType.Identity(
                personalDetails = personalDetailsContent.toDetails(::fields),
                addressDetails = addressDetailsContent.toDetails(::fields),
                contactDetails = contactDetailsContent.toDetails(::hidden, ::fields),
                workDetails = workDetailsContent.toDetails(::fields),
                extraSections = sections(extraSectionContentList)
            )

            is ItemContents.Custom -> ItemType.Custom(
                customFields = fields(customFields),
                extraSections = sections(sectionContentList)
            )

            is ItemContents.WifiNetwork -> ItemType.WifiNetwork(
                ssid = ssid,
                password = hidden(password),
                wifiSecurityType = wifiSecurityType,
                customFields = fields(customFields),
                extraSections = sections(sectionContentList)
            )

            is ItemContents.SSHKey -> ItemType.SSHKey(
                publicKey = publicKey,
                privateKey = hidden(privateKey),
                customFields = fields(customFields),
                extraSections = sections(sectionContentList)
            )

            is ItemContents.Unknown -> ItemType.Unknown
        }
    }

    private fun PersonalDetailsContent.toDetails(fields: (List<CustomFieldContent>) -> List<CustomField>) =
        PersonalDetails(
            fullName = fullName,
            firstName = firstName,
            middleName = middleName,
            lastName = lastName,
            birthdate = birthdate,
            gender = gender,
            email = email,
            phoneNumber = phoneNumber,
            customFields = fields(customFields)
        )

    private fun AddressDetailsContent.toDetails(fields: (List<CustomFieldContent>) -> List<CustomField>) =
        AddressDetails(
            organization = organization,
            streetAddress = streetAddress,
            zipOrPostalCode = zipOrPostalCode,
            city = city,
            stateOrProvince = stateOrProvince,
            countryOrRegion = countryOrRegion,
            floor = floor,
            county = county,
            customFields = fields(customFields)
        )

    private companion object {
        const val ITEM_PREFIX = "fake-item:"
        const val VAULT_PREFIX = "fake-vault:"
        const val FOLDER_PREFIX = "fake-folder:"
        const val ATTACHMENT_PREFIX = "fake-attachment:"

        val counter = AtomicLong()
        val items = ConcurrentHashMap<String, StoredItem>()
        val vaults = ConcurrentHashMap<String, VaultProperties>()
        val folders = ConcurrentHashMap<String, String>()
        val attachments = ConcurrentHashMap<String, AttachmentContent>()
    }
}

private fun WorkDetailsContent.toDetails(fields: (List<CustomFieldContent>) -> List<CustomField>) = WorkDetails(
    company = company,
    jobTitle = jobTitle,
    personalWebsite = personalWebsite,
    workPhoneNumber = workPhoneNumber,
    workEmail = workEmail,
    customFields = fields(customFields)
)

private fun ContactDetailsContent.toDetails(
    hidden: (HiddenState) -> String,
    fields: (List<CustomFieldContent>) -> List<CustomField>
) = ContactDetails(
    socialSecurityNumber = hidden(socialSecurityNumber),
    passportNumber = passportNumber,
    licenseNumber = licenseNumber,
    website = website,
    xHandle = xHandle,
    secondPhoneNumber = secondPhoneNumber,
    linkedin = linkedin,
    reddit = reddit,
    facebook = facebook,
    yahoo = yahoo,
    instagram = instagram,
    customFields = fields(customFields)
)

private fun ItemContents.searchableContent(): List<String> {
    fun fields(list: List<CustomFieldContent>): List<String> = list.flatMap { field ->
        when (field) {
            is CustomFieldContent.Text -> listOf(field.label, field.value)
            is CustomFieldContent.Hidden,
            is CustomFieldContent.Totp,
            is CustomFieldContent.Date -> listOf(field.label)
        }
    }

    fun sections(list: List<ExtraSectionContent>): List<String> = list.flatMap { fields(it.customFieldList) }

    val content = when (this) {
        is ItemContents.Login -> listOf(itemEmail, itemUsername) + urls
        is ItemContents.CreditCard -> listOf(cardHolder)
        is ItemContents.Identity -> identitySearchableContent(::fields) + sections(extraSectionContentList)
        is ItemContents.WifiNetwork -> listOf(ssid) + sections(sectionContentList)
        is ItemContents.SSHKey -> sections(sectionContentList)
        is ItemContents.Custom -> sections(sectionContentList)
        is ItemContents.Note,
        is ItemContents.Alias,
        is ItemContents.Unknown -> emptyList()
    }
    return (fields(customFields) + content).filter { it.isNotBlank() }
}

private fun ItemContents.Identity.identitySearchableContent(
    fields: (List<CustomFieldContent>) -> List<String>
): List<String> = with(personalDetailsContent) {
    listOf(fullName, firstName, middleName, lastName, birthdate, gender, email, phoneNumber) +
        fields(customFields)
} + with(addressDetailsContent) {
    listOf(organization, streetAddress, zipOrPostalCode, city, stateOrProvince, countryOrRegion, floor, county) +
        fields(customFields)
} + with(contactDetailsContent) {
    listOf(
        passportNumber, licenseNumber, website, xHandle, secondPhoneNumber,
        linkedin, reddit, facebook, yahoo, instagram
    ) + fields(customFields)
} + with(workDetailsContent) {
    listOf(company, jobTitle, personalWebsite, workPhoneNumber, workEmail) + fields(customFields)
}
