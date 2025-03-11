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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.parcelize.Parcelize
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.some
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.domain.ExtraSectionContent
import proton.android.pass.domain.ItemContents
import proton.android.pass.features.itemcreate.common.UICustomFieldContent
import proton.android.pass.features.itemcreate.common.UIExtraSection
import proton.android.pass.features.itemcreate.common.UIHiddenState
import proton.android.pass.totp.api.TotpManager

@Parcelize
@Immutable
sealed interface ItemStaticFields : Parcelable {

    @Parcelize
    data object Custom : ItemStaticFields

    @Parcelize
    data class WifiNetwork(
        val ssid: String,
        val password: UIHiddenState
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
    val sectionList: List<UIExtraSection>
) : Parcelable {

    constructor(itemContents: ItemContents.Custom) : this(
        title = itemContents.title,
        itemStaticFields = ItemStaticFields.Custom,
        customFieldList = itemContents.customFieldList.map(UICustomFieldContent.Companion::from),
        sectionList = itemContents.sectionContentList.map(::UIExtraSection)
    )

    constructor(itemContents: ItemContents.WifiNetwork) : this(
        title = itemContents.title,
        itemStaticFields = ItemStaticFields.WifiNetwork(
            ssid = itemContents.ssid,
            password = UIHiddenState.from(itemContents.password)
        ),
        customFieldList = itemContents.customFieldList.map(UICustomFieldContent.Companion::from),
        sectionList = itemContents.sectionContentList.map(::UIExtraSection)
    )

    constructor(itemContents: ItemContents.SSHKey) : this(
        title = itemContents.title,
        itemStaticFields = ItemStaticFields.SSHKey(
            publicKey = itemContents.publicKey,
            privateKey = UIHiddenState.from(itemContents.privateKey)
        ),
        customFieldList = itemContents.customFieldList.map(UICustomFieldContent.Companion::from),
        sectionList = itemContents.sectionContentList.map(::UIExtraSection)
    )

    suspend fun validate(
        originalCustomFields: List<UICustomFieldContent>,
        originalSections: List<UIExtraSection>,
        totpManager: TotpManager,
        encryptionContextProvider: EncryptionContextProvider
    ): Set<ItemValidationErrors> {
        val errors = mutableSetOf<ItemValidationErrors>()
        if (title.isBlank()) errors.add(ItemValidationErrors.BlankTitle)

        encryptionContextProvider.withEncryptionContextSuspendable {
            errors += validateTotpFields(
                entries = customFieldList,
                originalEntriesById = originalCustomFields
                    .filterIsInstance<UICustomFieldContent.Totp>()
                    .associateBy { it.id },
                sectionIndex = None,
                totpManager = totpManager,
                encryptionContext = this
            )

            sectionList.forEachIndexed { sectionIndex, section ->
                errors += validateTotpFields(
                    entries = section.customFields,
                    originalEntriesById = originalSections.getOrNull(sectionIndex)
                        ?.customFields
                        .orEmpty()
                        .filterIsInstance<UICustomFieldContent.Totp>()
                        .associateBy { it.id },
                    sectionIndex = sectionIndex.some(),
                    totpManager = totpManager,
                    encryptionContext = this
                )
            }
        }

        return errors.toSet()
    }

    private suspend fun validateTotpFields(
        entries: List<UICustomFieldContent>,
        originalEntriesById: Map<String, UICustomFieldContent.Totp>,
        sectionIndex: Option<Int>,
        totpManager: TotpManager,
        encryptionContext: EncryptionContext
    ): Set<ItemValidationErrors> {
        val errors = mutableSetOf<ItemValidationErrors>()
        entries.forEachIndexed { index, entry ->
            if (entry !is UICustomFieldContent.Totp) return@forEachIndexed
            val decrypted = encryptionContext.decrypt(entry.value.encrypted)
            if (decrypted.isBlank()) {
                errors.add(ItemValidationErrors.EmptyTotp(sectionIndex, index))
                return@forEachIndexed
            }
            val original = originalEntriesById[entry.id]
                ?.let { encryptionContext.decrypt(it.value.encrypted) }
                .orEmpty()
            val result = totpManager.sanitiseToSave(original, decrypted)

            result.fold(
                onSuccess = { sanitisedUri ->
                    totpManager.parse(sanitisedUri).getOrElse {
                        errors.add(ItemValidationErrors.InvalidTotp(sectionIndex, index))
                    }

                    val totpCodeResult =
                        runCatching { totpManager.observeCode(sanitisedUri).firstOrNull() }
                    if (totpCodeResult.isFailure) {
                        errors.add(ItemValidationErrors.InvalidTotp(sectionIndex, index))
                    }
                },
                onFailure = {
                    errors.add(ItemValidationErrors.InvalidTotp(sectionIndex, index))
                }
            )
        }

        return errors
    }

    fun toItemContents(): ItemContents = when (itemStaticFields) {
        ItemStaticFields.Custom -> ItemContents.Custom(
            title = title,
            note = "",
            customFieldList = customFieldList.map(UICustomFieldContent::toCustomFieldContent),
            sectionContentList = sectionList.map {
                ExtraSectionContent(
                    title = it.title,
                    customFieldList = it.customFields.map(UICustomFieldContent::toCustomFieldContent)
                )
            }
        )

        is ItemStaticFields.SSHKey -> ItemContents.SSHKey(
            title = title,
            note = "",
            publicKey = itemStaticFields.publicKey,
            privateKey = itemStaticFields.privateKey.toHiddenState(),
            customFieldList = customFieldList.map(UICustomFieldContent::toCustomFieldContent),
            sectionContentList = sectionList.map {
                ExtraSectionContent(
                    title = it.title,
                    customFieldList = it.customFields.map(UICustomFieldContent::toCustomFieldContent)
                )
            }
        )

        is ItemStaticFields.WifiNetwork -> ItemContents.WifiNetwork(
            title = title,
            note = "",
            ssid = itemStaticFields.ssid,
            password = itemStaticFields.password.toHiddenState(),
            customFieldList = customFieldList.map(UICustomFieldContent::toCustomFieldContent),
            sectionContentList = sectionList.map {
                ExtraSectionContent(
                    title = it.title,
                    customFieldList = it.customFields.map(UICustomFieldContent::toCustomFieldContent)
                )
            }
        )
    }

    companion object {
        val EMPTY = ItemFormState(
            title = "",
            itemStaticFields = ItemStaticFields.Custom,
            customFieldList = emptyList(),
            sectionList = emptyList()
        )
    }
}

sealed interface ItemValidationErrors {
    data object BlankTitle : ItemValidationErrors
    data class EmptyTotp(
        val sectionIndex: Option<Int>,
        val index: Int
    ) : ItemValidationErrors

    data class InvalidTotp(
        val sectionIndex: Option<Int>,
        val index: Int
    ) : ItemValidationErrors
}
