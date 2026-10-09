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
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.byte
import io.kotest.property.arbitrary.byteArray
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.constant
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.set
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Test
import proton.android.pass.commonrust.impl.ItemTestData.encryptionContext
import proton.android.pass.domain.AddressDetailsContent
import proton.android.pass.domain.AutofillUrl
import proton.android.pass.domain.AutofillUrlMode
import proton.android.pass.domain.ByteArrayWrapper
import proton.android.pass.domain.ContactDetailsContent
import proton.android.pass.domain.CreditCardType
import proton.android.pass.domain.CustomFieldContent
import proton.android.pass.domain.ExtraSectionContent
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemFlags
import proton.android.pass.domain.Passkey
import proton.android.pass.domain.PasskeyCreationData
import proton.android.pass.domain.PasskeyId
import proton.android.pass.domain.PersonalDetailsContent
import proton.android.pass.domain.WifiSecurityType
import proton.android.pass.domain.WorkDetailsContent
import proton.android.pass.domain.entity.AppName
import proton.android.pass.domain.entity.PackageInfo
import proton.android.pass.domain.entity.PackageName
import proton.android.pass.domain.toItemContents

internal class ItemRoundTripPropertyTest {

    @Test
    internal fun `WHEN mapping any item to library and back THEN only lossy-by-design fields change`() = runTest {
        checkAll(ITERATIONS, itemContents) { original ->
            assertThat(roundTrip(original)).isEqualTo(normalised(original))
        }
    }

    private fun roundTrip(original: ItemContents): ItemContents {
        val aliasEmail = (original as? ItemContents.Alias)?.aliasEmail
        val libraryData = original.toLibraryItemData(
            itemUuid = "uuid",
            existing = null,
            encryptionContext = encryptionContext
        )
        return toItemContents(
            decrypt = encryptionContext::decrypt,
            itemType = libraryData.toItemType(encryptionContext, aliasEmail),
            title = encryptionContext.encrypt(libraryData.title),
            note = encryptionContext.encrypt(libraryData.note),
            itemFlags = ItemFlags(0)
        )
    }

    private fun normalised(item: ItemContents): ItemContents = when (item) {
        is ItemContents.Login -> item.copy(
            customFields = item.customFields.map { it.asStored() },
            password = item.password.asStored(),
            urls = item.urls.filter { it.isNotBlank() },
            primaryTotp = item.primaryTotp.asStored(),
            passkeys = item.passkeys.map { it.asStored() }
        )
        is ItemContents.Note -> item.copy(customFields = item.customFields.map { it.asStored() })
        is ItemContents.Alias -> item.copy(customFields = item.customFields.map { it.asStored() })
        is ItemContents.CreditCard -> item.copy(
            customFields = item.customFields.map { it.asStored() },
            cvv = item.cvv.asStored(),
            pin = item.pin.asStored()
        )
        is ItemContents.Identity -> item.copy(
            customFields = IDENTITY_TYPE_HAS_NO_TOP_LEVEL_FIELDS,
            personalDetailsContent = item.personalDetailsContent.let {
                it.copy(customFields = it.customFields.map { field -> field.asStored() })
            },
            addressDetailsContent = item.addressDetailsContent.let {
                it.copy(customFields = it.customFields.map { field -> field.asStored() })
            },
            contactDetailsContent = item.contactDetailsContent.let {
                it.copy(
                    socialSecurityNumber = it.socialSecurityNumber.asStored(),
                    customFields = it.customFields.map { field -> field.asStored() }
                )
            },
            workDetailsContent = item.workDetailsContent.let {
                it.copy(customFields = it.customFields.map { field -> field.asStored() })
            },
            extraSectionContentList = item.extraSectionContentList.map { it.asStored() }
        )
        is ItemContents.Custom -> item.copy(
            customFields = item.customFields.map { it.asStored() },
            sectionContentList = item.sectionContentList.map { it.asStored() }
        )
        is ItemContents.WifiNetwork -> item.copy(
            customFields = item.customFields.map { it.asStored() },
            password = item.password.asStored(),
            sectionContentList = item.sectionContentList.map { it.asStored() }
        )
        is ItemContents.SSHKey -> item.copy(
            customFields = item.customFields.map { it.asStored() },
            privateKey = item.privateKey.asStored(),
            sectionContentList = item.sectionContentList.map { it.asStored() }
        )
        is ItemContents.Unknown -> item
    }

    private fun Passkey.asStored(): Passkey = copy(
        rpId = rpId.orEmpty(),
        createTime = Instant.fromEpochSeconds(createTime.epochSeconds),
        userHandle = userHandle ?: ByteArrayWrapper(ByteArray(0))
    )

    private fun HiddenState.asStored(): HiddenState = if (encryptionContext.decrypt(encrypted).isEmpty()) {
        HiddenState.Empty(encrypted)
    } else {
        HiddenState.Concealed(encrypted)
    }

    private fun HiddenState.asRevealedTotp(): HiddenState = encryptionContext.decrypt(encrypted).let { clear ->
        if (clear.isEmpty()) HiddenState.Empty(encrypted) else HiddenState.Revealed(encrypted, clear)
    }

    private fun CustomFieldContent.asStored(): CustomFieldContent = when (this) {
        is CustomFieldContent.Text -> this
        is CustomFieldContent.Hidden -> copy(value = value.asStored())
        is CustomFieldContent.Totp -> copy(value = value.asRevealedTotp())
        is CustomFieldContent.Date -> copy(value = value?.truncatedToWholeSeconds())
    }

    private fun ExtraSectionContent.asStored(): ExtraSectionContent =
        copy(customFieldList = customFieldList.map { it.asStored() })

    private fun Long.truncatedToWholeSeconds(): Long? = (this / MILLIS_IN_SECOND)
        .takeIf { it != 0L }
        ?.let { it * MILLIS_IN_SECOND }

    private companion object {
        const val ITERATIONS = 1_000
        const val MAX_DATE_MILLIS = 253_402_214_400_000L
        const val MAX_UINT32_SECONDS = 4_294_967_295L
        val IDENTITY_TYPE_HAS_NO_TOP_LEVEL_FIELDS = emptyList<CustomFieldContent>()

        val text: Arb<String> = Arb.string(0..12)

        val hiddenState: Arb<HiddenState> = arbitrary {
            val clear = text.bind()
            val encrypted = encryptionContext.encrypt(clear)
            Arb.choice(
                Arb.constant(HiddenState.Empty(encrypted)),
                Arb.constant(HiddenState.Concealed(encrypted)),
                Arb.constant(HiddenState.Revealed(encrypted, clear))
            ).bind()
        }

        val customField: Arb<CustomFieldContent> = Arb.choice(
            Arb.bind(text, text, CustomFieldContent::Text),
            Arb.bind(text, hiddenState, CustomFieldContent::Hidden),
            Arb.bind(text, hiddenState, CustomFieldContent::Totp),
            Arb.bind(text, Arb.long(-MAX_DATE_MILLIS..MAX_DATE_MILLIS).orNull(), CustomFieldContent::Date)
        )

        val customFields: Arb<List<CustomFieldContent>> = Arb.list(customField, 0..4)

        val section: Arb<ExtraSectionContent> = Arb.bind(text, customFields, ::ExtraSectionContent)

        val sections: Arb<List<ExtraSectionContent>> = Arb.list(section, 0..3)

        val packageInfo: Arb<PackageInfo> = Arb.bind(text, text, Arb.set(text, 0..2)) { name, app, hashes ->
            PackageInfo(PackageName(name), AppName(app), hashes)
        }

        val bytes: Arb<ByteArrayWrapper> = Arb.byteArray(Arb.int(0..16), Arb.byte())
            .map(::ByteArrayWrapper)

        val passkey: Arb<Passkey> = arbitrary {
            Passkey(
                id = PasskeyId(text.bind()),
                domain = text.bind(),
                rpId = text.orNull().bind(),
                rpName = text.bind(),
                userName = text.bind(),
                userDisplayName = text.bind(),
                userId = bytes.bind(),
                note = text.bind(),
                createTime = Instant.fromEpochMilliseconds(Arb.long(0L..MAX_UINT32_SECONDS * MILLIS_IN_SECOND).bind()),
                contents = bytes.bind(),
                userHandle = bytes.orNull().bind(),
                credentialId = bytes.bind(),
                creationData = Arb.bind(text, text, text, text, ::PasskeyCreationData).orNull().bind()
            )
        }

        val login: Arb<ItemContents> = arbitrary {
            ItemContents.Login(
                title = text.bind(),
                note = text.bind(),
                customFields = customFields.bind(),
                itemEmail = text.bind(),
                itemUsername = text.bind(),
                password = hiddenState.bind(),
                urls = Arb.list(text, 0..3).bind(),
                packageInfoSet = Arb.set(packageInfo, 0..2).bind(),
                primaryTotp = hiddenState.bind(),
                passkeys = Arb.list(passkey, 0..2).bind(),
                autofillUrls = Arb.list(Arb.bind(text, Arb.enum<AutofillUrlMode>(), ::AutofillUrl), 0..2).bind()
            )
        }

        val note: Arb<ItemContents> = Arb.bind(text, text, customFields, ItemContents::Note)

        val alias: Arb<ItemContents> = Arb.bind(text, text, customFields, text) { title, note, fields, email ->
            ItemContents.Alias(
                title = title,
                note = note,
                customFields = fields,
                aliasEmail = email,
                isDisabled = false
            )
        }

        val creditCard: Arb<ItemContents> = arbitrary {
            ItemContents.CreditCard(
                title = text.bind(),
                note = text.bind(),
                customFields = customFields.bind(),
                cardHolder = text.bind(),
                type = Arb.enum<CreditCardType>().bind(),
                number = text.bind(),
                cvv = hiddenState.bind(),
                pin = hiddenState.bind(),
                expirationDate = text.bind()
            )
        }

        val identity: Arb<ItemContents> = arbitrary {
            ItemContents.Identity(
                title = text.bind(),
                note = text.bind(),
                customFields = customFields.bind(),
                personalDetailsContent = PersonalDetailsContent(
                    fullName = text.bind(),
                    firstName = text.bind(),
                    middleName = text.bind(),
                    lastName = text.bind(),
                    birthdate = text.bind(),
                    gender = text.bind(),
                    email = text.bind(),
                    phoneNumber = text.bind(),
                    customFields = customFields.bind()
                ),
                addressDetailsContent = AddressDetailsContent(
                    organization = text.bind(),
                    streetAddress = text.bind(),
                    zipOrPostalCode = text.bind(),
                    city = text.bind(),
                    stateOrProvince = text.bind(),
                    countryOrRegion = text.bind(),
                    floor = text.bind(),
                    county = text.bind(),
                    customFields = customFields.bind()
                ),
                contactDetailsContent = ContactDetailsContent(
                    socialSecurityNumber = hiddenState.bind(),
                    passportNumber = text.bind(),
                    licenseNumber = text.bind(),
                    website = text.bind(),
                    xHandle = text.bind(),
                    secondPhoneNumber = text.bind(),
                    linkedin = text.bind(),
                    reddit = text.bind(),
                    facebook = text.bind(),
                    yahoo = text.bind(),
                    instagram = text.bind(),
                    customFields = customFields.bind()
                ),
                workDetailsContent = WorkDetailsContent(
                    company = text.bind(),
                    jobTitle = text.bind(),
                    personalWebsite = text.bind(),
                    workPhoneNumber = text.bind(),
                    workEmail = text.bind(),
                    customFields = customFields.bind()
                ),
                extraSectionContentList = sections.bind()
            )
        }

        val custom: Arb<ItemContents> = Arb.bind(text, text, customFields, sections, ItemContents::Custom)

        val wifi: Arb<ItemContents> = arbitrary {
            ItemContents.WifiNetwork(
                title = text.bind(),
                note = text.bind(),
                customFields = customFields.bind(),
                ssid = text.bind(),
                password = hiddenState.bind(),
                wifiSecurityType = Arb.enum<WifiSecurityType>().bind(),
                sectionContentList = sections.bind()
            )
        }

        val sshKey: Arb<ItemContents> = arbitrary {
            ItemContents.SSHKey(
                title = text.bind(),
                note = text.bind(),
                customFields = customFields.bind(),
                publicKey = text.bind(),
                privateKey = hiddenState.bind(),
                sectionContentList = sections.bind()
            )
        }

        val itemContents: Arb<ItemContents> = Arb.choice(login, note, alias, creditCard, identity, custom, wifi, sshKey)
    }
}
