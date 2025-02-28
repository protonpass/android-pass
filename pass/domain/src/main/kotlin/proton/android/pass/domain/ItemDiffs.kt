/*
 * Copyright (c) 2024 Proton AG
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

import proton.android.pass.domain.attachments.AttachmentId

enum class ItemDiffType {
    Content,
    Field,
    None
}

sealed interface ItemDiffs {

    val title: ItemDiffType

    val note: ItemDiffType

    val customFields: List<ItemDiffType>

    val attachments: Map<AttachmentId, ItemDiffType>

    fun customField(index: Int): ItemDiffType = customFields.getOrElse(index) { ItemDiffType.None }

    data object None : ItemDiffs {
        override val title: ItemDiffType = ItemDiffType.None
        override val note: ItemDiffType = ItemDiffType.None
        override val customFields: List<ItemDiffType> = emptyList()
        override val attachments: Map<AttachmentId, ItemDiffType> = emptyMap()
    }

    data class Alias(
        override val title: ItemDiffType = ItemDiffType.None,
        override val note: ItemDiffType = ItemDiffType.None,
        override val customFields: List<ItemDiffType> = emptyList(),
        override val attachments: Map<AttachmentId, ItemDiffType> = emptyMap(),
        val aliasEmail: ItemDiffType = ItemDiffType.None
    ) : ItemDiffs

    data class CreditCard(
        override val title: ItemDiffType = ItemDiffType.None,
        override val note: ItemDiffType = ItemDiffType.None,
        override val customFields: List<ItemDiffType> = emptyList(),
        override val attachments: Map<AttachmentId, ItemDiffType> = emptyMap(),
        val cardHolder: ItemDiffType = ItemDiffType.None,
        val cardNumber: ItemDiffType = ItemDiffType.None,
        val cvv: ItemDiffType = ItemDiffType.None,
        val pin: ItemDiffType = ItemDiffType.None,
        val expirationDate: ItemDiffType = ItemDiffType.None
    ) : ItemDiffs

    data class Identity(
        override val title: ItemDiffType = ItemDiffType.None,
        override val note: ItemDiffType = ItemDiffType.None,
        override val customFields: List<ItemDiffType> = emptyList(),
        override val attachments: Map<AttachmentId, ItemDiffType> = emptyMap(),
        val organization: ItemDiffType = ItemDiffType.None,
        val streetAddress: ItemDiffType = ItemDiffType.None,
        val floor: ItemDiffType = ItemDiffType.None,
        val city: ItemDiffType = ItemDiffType.None,
        val zipOrPostalCode: ItemDiffType = ItemDiffType.None,
        val stateOrProvince: ItemDiffType = ItemDiffType.None,
        val county: ItemDiffType = ItemDiffType.None,
        val countryOrRegion: ItemDiffType = ItemDiffType.None,
        val socialSecurityNumber: ItemDiffType = ItemDiffType.None,
        val passportNumber: ItemDiffType = ItemDiffType.None,
        val licenseNumber: ItemDiffType = ItemDiffType.None,
        val website: ItemDiffType = ItemDiffType.None,
        val secondPhoneNumber: ItemDiffType = ItemDiffType.None,
        val linkedin: ItemDiffType = ItemDiffType.None,
        val xHandle: ItemDiffType = ItemDiffType.None,
        val instagram: ItemDiffType = ItemDiffType.None,
        val facebook: ItemDiffType = ItemDiffType.None,
        val reddit: ItemDiffType = ItemDiffType.None,
        val yahoo: ItemDiffType = ItemDiffType.None,
        val firstName: ItemDiffType = ItemDiffType.None,
        val middleName: ItemDiffType = ItemDiffType.None,
        val lastName: ItemDiffType = ItemDiffType.None,
        val fullName: ItemDiffType = ItemDiffType.None,
        val email: ItemDiffType = ItemDiffType.None,
        val gender: ItemDiffType = ItemDiffType.None,
        val phoneNumber: ItemDiffType = ItemDiffType.None,
        val birthdate: ItemDiffType = ItemDiffType.None,
        val company: ItemDiffType = ItemDiffType.None,
        val jobTitle: ItemDiffType = ItemDiffType.None,
        val workPhoneNumber: ItemDiffType = ItemDiffType.None,
        val workEmail: ItemDiffType = ItemDiffType.None,
        val personalWebsite: ItemDiffType = ItemDiffType.None,
        private val addressCustomFields: List<ItemDiffType> = emptyList(),
        private val contactCustomFields: List<ItemDiffType> = emptyList(),
        private val personalCustomFields: List<ItemDiffType> = emptyList(),
        private val workCustomFields: List<ItemDiffType> = emptyList(),
        private val extraCustomFields: List<List<ItemDiffType>> = emptyList()
    ) : ItemDiffs {

        fun customField(section: ItemCustomFieldSection, index: Int): ItemDiffType = when (section) {
            is ItemCustomFieldSection.Identity.Address -> addressCustomFields
            is ItemCustomFieldSection.Identity.Contact -> contactCustomFields
            is ItemCustomFieldSection.Identity.Personal -> personalCustomFields
            is ItemCustomFieldSection.Identity.Work -> workCustomFields
            is ItemCustomFieldSection.Identity.ExtraSection -> extraCustomFields.getOrElse(
                section.index
            ) {
                emptyList()
            }

            is ItemCustomFieldSection.CustomField,
            is ItemCustomFieldSection.CustomItem.ExtraSection ->
                throw IllegalStateException("Not supported sections")
        }.let { customFields -> customFields.getOrElse(index) { ItemDiffType.None } }

    }

    data class Login(
        override val title: ItemDiffType = ItemDiffType.None,
        override val note: ItemDiffType = ItemDiffType.None,
        override val attachments: Map<AttachmentId, ItemDiffType> = emptyMap(),
        override val customFields: List<ItemDiffType> = emptyList(),
        val email: ItemDiffType = ItemDiffType.None,
        val username: ItemDiffType = ItemDiffType.None,
        val password: ItemDiffType = ItemDiffType.None,
        val totp: ItemDiffType = ItemDiffType.None,
        val urls: Pair<ItemDiffType, List<ItemDiffType>> = Pair(ItemDiffType.None, emptyList()),
        val linkedApps: Pair<ItemDiffType, List<ItemDiffType>> = Pair(
            ItemDiffType.None,
            emptyList()
        ),
        private val passkeys: Map<String, ItemDiffType> = emptyMap()
    ) : ItemDiffs {

        fun passkey(passkeyId: String): ItemDiffType = passkeys.getOrElse(passkeyId) { ItemDiffType.None }

    }

    data class Custom(
        override val title: ItemDiffType = ItemDiffType.None,
        override val note: ItemDiffType = ItemDiffType.None,
        override val customFields: List<ItemDiffType> = emptyList(),
        override val attachments: Map<AttachmentId, ItemDiffType> = emptyMap(),
        private val extraCustomFields: List<List<ItemDiffType>> = emptyList()
    ) : ItemDiffs {

        fun customField(section: ItemCustomFieldSection, index: Int): ItemDiffType = when (section) {
            is ItemCustomFieldSection.CustomItem.ExtraSection ->
                extraCustomFields.getOrElse(section.index) { emptyList() }

            is ItemCustomFieldSection.CustomField,
            is ItemCustomFieldSection.Identity.Address,
            is ItemCustomFieldSection.Identity.Contact,
            is ItemCustomFieldSection.Identity.Personal,
            is ItemCustomFieldSection.Identity.Work,
            is ItemCustomFieldSection.Identity.ExtraSection ->
                throw UnsupportedOperationException("cannot use section ${section::class.simpleName} ")
        }.let { customFields -> customFields.getOrElse(index) { ItemDiffType.None } }

    }

    data class Note(
        override val title: ItemDiffType = ItemDiffType.None,
        override val note: ItemDiffType = ItemDiffType.None,
        override val customFields: List<ItemDiffType> = emptyList(),
        override val attachments: Map<AttachmentId, ItemDiffType> = emptyMap()
    ) : ItemDiffs

    data class Unknown(
        override val title: ItemDiffType = ItemDiffType.None,
        override val note: ItemDiffType = ItemDiffType.None,
        override val customFields: List<ItemDiffType> = emptyList(),
        override val attachments: Map<AttachmentId, ItemDiffType> = emptyMap()
    ) : ItemDiffs

}
