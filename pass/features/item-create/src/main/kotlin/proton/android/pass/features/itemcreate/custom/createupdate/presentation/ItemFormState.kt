/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.itemcreate.custom.createupdate.presentation

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize
import proton.android.pass.common.api.Some
import proton.android.pass.domain.ExtraSectionContent
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.WifiSecurityType
import proton.android.pass.features.itemcreate.common.UICustomFieldContent
import proton.android.pass.features.itemcreate.common.UIExtraSection
import proton.android.pass.features.itemcreate.common.UIHiddenState
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldIdentifier

@Parcelize
@Immutable
sealed interface ItemStaticFields : Parcelable {

    @Parcelize
    data object Custom : ItemStaticFields

    @Parcelize
    data class WifiNetwork(
        val ssid: String,
        val password: UIHiddenState,
        val wifiSecurityType: WifiSecurityType
    ) : ItemStaticFields

    @Parcelize
    data class SSHKey(
        val publicKey: String,
        val privateKey: UIHiddenState
    ) : ItemStaticFields
}

@Parcelize
@Immutable
data class ItemFormState(
    val title: String,
    val itemStaticFields: ItemStaticFields,
    val customFieldList: List<UICustomFieldContent>,
    val sectionList: List<UIExtraSection>,
    val note: String
) : Parcelable {

    constructor(itemContents: ItemContents.Custom) : this(
        title = itemContents.title,
        itemStaticFields = ItemStaticFields.Custom,
        customFieldList = itemContents.customFields.map(UICustomFieldContent.Companion::from),
        sectionList = itemContents.sectionContentList.map(::UIExtraSection),
        note = itemContents.note
    )

    constructor(itemContents: ItemContents.WifiNetwork) : this(
        title = itemContents.title,
        itemStaticFields = ItemStaticFields.WifiNetwork(
            ssid = itemContents.ssid,
            password = UIHiddenState.from(itemContents.password),
            wifiSecurityType = itemContents.wifiSecurityType
        ),
        customFieldList = itemContents.customFields.map(UICustomFieldContent.Companion::from),
        sectionList = itemContents.sectionContentList.map(::UIExtraSection),
        note = itemContents.note
    )

    constructor(itemContents: ItemContents.SSHKey) : this(
        title = itemContents.title,
        itemStaticFields = ItemStaticFields.SSHKey(
            publicKey = itemContents.publicKey,
            privateKey = UIHiddenState.from(itemContents.privateKey)
        ),
        customFieldList = itemContents.customFields.map(UICustomFieldContent.Companion::from),
        sectionList = itemContents.sectionContentList.map(::UIExtraSection),
        note = itemContents.note
    )

    fun toItemContents(): ItemContents = when (itemStaticFields) {
        ItemStaticFields.Custom -> ItemContents.Custom(
            title = title,
            note = note,
            customFields = customFieldList.map(UICustomFieldContent::toCustomFieldContent),
            sectionContentList = sectionList.map {
                ExtraSectionContent(
                    title = it.title,
                    customFieldList = it.customFields.map(UICustomFieldContent::toCustomFieldContent)
                )
            }
        )

        is ItemStaticFields.SSHKey -> ItemContents.SSHKey(
            title = title,
            note = note,
            publicKey = itemStaticFields.publicKey,
            privateKey = itemStaticFields.privateKey.toHiddenState(),
            customFields = customFieldList.map(UICustomFieldContent::toCustomFieldContent),
            sectionContentList = sectionList.map {
                ExtraSectionContent(
                    title = it.title,
                    customFieldList = it.customFields.map(UICustomFieldContent::toCustomFieldContent)
                )
            }
        )

        is ItemStaticFields.WifiNetwork -> ItemContents.WifiNetwork(
            title = title,
            note = note,
            ssid = itemStaticFields.ssid,
            password = itemStaticFields.password.toHiddenState(),
            wifiSecurityType = itemStaticFields.wifiSecurityType,
            customFields = customFieldList.map(UICustomFieldContent::toCustomFieldContent),
            sectionContentList = sectionList.map {
                ExtraSectionContent(
                    title = it.title,
                    customFieldList = it.customFields.map(UICustomFieldContent::toCustomFieldContent)
                )
            }
        )
    }

    fun findCustomField(field: CustomFieldIdentifier): UICustomFieldContent = if (field.sectionIndex is Some) {
        sectionList[field.sectionIndex.value].customFields[field.index]
    } else {
        customFieldList[field.index]
    }

    companion object {
        val EMPTY = ItemFormState(
            title = "",
            itemStaticFields = ItemStaticFields.Custom,
            customFieldList = emptyList(),
            sectionList = emptyList(),
            note = ""
        )
    }
}

