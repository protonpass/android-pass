/*
 * Copyright (c) 2024-2025 Proton AG
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

package proton.android.pass.features.item.details.detail.presentation.handlers

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsFieldType
import proton.android.pass.commonpresentation.api.items.details.handlers.ItemDetailsHandlerObserver
import proton.android.pass.commonrust.api.passwords.strengths.PasswordStrengthCalculator
import proton.android.pass.commonuimodels.api.UIPasskeyContent
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.commonuimodels.api.items.ItemDetailState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemDiffType
import proton.android.pass.domain.ItemDiffs
import proton.android.pass.domain.ItemSection
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.Passkey
import proton.android.pass.domain.Share
import proton.android.pass.domain.Totp
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.domain.entity.PackageInfo
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import proton.android.pass.totp.api.TotpManager
import javax.inject.Inject

class LoginItemDetailsHandlerObserverImpl @Inject constructor(
    override val encryptionContextProvider: EncryptionContextProvider,
    override val totpManager: TotpManager,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val passwordStrengthCalculator: PasswordStrengthCalculator
) : ItemDetailsHandlerObserver<ItemContents.Login>(encryptionContextProvider, totpManager) {

    override fun observe(
        share: Share,
        item: Item,
        attachmentsState: AttachmentsState
    ): Flow<ItemDetailState> = combine(
        observeItemContents(item),
        observePrimaryTotp(item),
        observeCustomFieldTotps(item),
        userPreferencesRepository.getUseFaviconsPreference()
    ) { loginItemContents, primaryTotp, customFieldTotps, useFaviconsPreference ->
        ItemDetailState.Login(
            itemContents = loginItemContents,
            itemId = item.id,
            shareId = item.shareId,
            isItemPinned = item.isPinned,
            itemShare = share,
            itemCreatedAt = item.createTime,
            itemModifiedAt = item.modificationTime,
            itemLastAutofillAtOption = item.lastAutofillTime,
            itemRevision = item.revision,
            itemState = ItemState.from(item.state),
            itemDiffs = ItemDiffs.Login(),
            itemShareCount = item.shareCount,
            canLoadExternalImages = useFaviconsPreference.value(),
            passwordStrength = encryptionContextProvider.withEncryptionContext {
                decrypt(loginItemContents.password.encrypted)
                    .let(passwordStrengthCalculator::calculateStrength)
            },
            primaryTotp = primaryTotp,
            customFieldTotps = customFieldTotps,
            passkeys = loginItemContents.passkeys.map { passkey -> UIPasskeyContent.from(passkey) },
            attachmentsState = attachmentsState
        )
    }

    private fun observePrimaryTotp(item: Item): Flow<Totp?> = observeItemContents(item)
        .flatMapLatest { loginItemContents ->
            observeTotp(loginItemContents.primaryTotp)
        }

    private fun observeTotp(hiddenTotpState: HiddenState): Flow<Totp?> = when (hiddenTotpState) {
        is HiddenState.Empty -> ""
        is HiddenState.Revealed -> hiddenTotpState.clearText
        is HiddenState.Concealed -> encryptionContextProvider.withEncryptionContext {
            decrypt(hiddenTotpState.encrypted)
        }
    }.let { totpUri ->
        if (totpUri.isEmpty()) {
            flowOf(null)
        } else {
            totpManager.observeCode(totpUri).map { totpWrapper ->
                Totp(
                    code = totpWrapper.code,
                    remainingSeconds = totpWrapper.remainingSeconds,
                    totalSeconds = totpWrapper.totalSeconds
                )
            }
        }
    }

    override fun updateHiddenFieldsContents(
        itemContents: ItemContents.Login,
        revealedHiddenFields: Map<ItemSection, Set<ItemDetailsFieldType.Hidden>>
    ): ItemContents {
        val revealedFields = revealedHiddenFields[ItemSection.Login] ?: emptyList()
        val mutableCustomFields = itemContents.customFields.toMutableList()

        mutableCustomFields.forEachIndexed { index, field ->
            val shouldBeRevealed = revealedHiddenFields[ItemSection.CustomField]
                ?.any { it is ItemDetailsFieldType.Hidden.CustomField && it.index == index } == true
            mutableCustomFields[index] = updateHiddenState(field, shouldBeRevealed, encryptionContextProvider)
        }

        return itemContents.copy(
            password = updateHiddenStateValue(
                hiddenState = itemContents.password,
                shouldBeRevealed = revealedFields.contains(ItemDetailsFieldType.Hidden.Password),
                encryptionContextProvider = encryptionContextProvider
            ),
            customFields = mutableCustomFields
        )
    }

    override fun calculateItemDiffs(
        baseItemContents: ItemContents.Login,
        otherItemContents: ItemContents.Login,
        baseAttachments: List<Attachment>,
        otherAttachments: List<Attachment>
    ): ItemDiffs = encryptionContextProvider.withEncryptionContext {
        ItemDiffs.Login(
            title = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.title,
                otherItemFieldValue = otherItemContents.title
            ),
            email = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.itemEmail,
                otherItemFieldValue = otherItemContents.itemEmail
            ),
            username = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.itemUsername,
                otherItemFieldValue = otherItemContents.itemUsername
            ),
            password = calculateItemDiffType(
                encryptionContext = this@withEncryptionContext,
                baseItemFieldHiddenState = baseItemContents.password,
                otherItemFieldHiddenState = otherItemContents.password
            ),
            totp = calculateItemDiffType(
                encryptionContext = this@withEncryptionContext,
                baseItemFieldHiddenState = baseItemContents.primaryTotp,
                otherItemFieldHiddenState = otherItemContents.primaryTotp
            ),
            note = calculateItemDiffType(
                baseItemFieldValue = baseItemContents.note,
                otherItemFieldValue = otherItemContents.note
            ),
            urls = calculateItemDiffTypes(
                baseItemFieldValues = baseItemContents.urls,
                otherItemFieldValues = otherItemContents.urls
            ),
            linkedApps = calculateItemDiffTypes(
                basePackagesInfo = baseItemContents.packageInfoSet,
                otherPackagesInfo = otherItemContents.packageInfoSet
            ),
            customFields = calculateItemDiffTypes(
                encryptionContext = this@withEncryptionContext,
                baseItemCustomFieldsContent = baseItemContents.customFields,
                otherItemCustomFieldsContent = otherItemContents.customFields
            ),
            passkeys = calculateItemDiffTypes(
                baseItemPasskeys = baseItemContents.passkeys,
                otherItemPasskeys = otherItemContents.passkeys
            ),
            attachments = calculateItemDiffType(
                baseItemAttachments = baseAttachments,
                otherItemAttachments = otherAttachments
            )
        )
    }

    private fun calculateItemDiffTypes(
        baseItemPasskeys: List<Passkey>,
        otherItemPasskeys: List<Passkey>
    ): Map<String, ItemDiffType> = otherItemPasskeys
        .map { otherPasskey -> otherPasskey.id }
        .toSet()
        .let { otherPasskeysIds ->
            baseItemPasskeys.associate { basePasskey ->
                basePasskey.id.value to if (otherPasskeysIds.contains(basePasskey.id)) {
                    ItemDiffType.None
                } else {
                    ItemDiffType.Field
                }
            }
        }

    private fun calculateItemDiffTypes(
        basePackagesInfo: Set<PackageInfo>,
        otherPackagesInfo: Set<PackageInfo>
    ): Pair<ItemDiffType, List<ItemDiffType>> = when {
        basePackagesInfo.isEmpty() -> {
            ItemDiffType.None to emptyList()
        }

        otherPackagesInfo.isEmpty() -> {
            ItemDiffType.Field to List(basePackagesInfo.size) { ItemDiffType.None }
        }

        else -> {
            otherPackagesInfo.associate { otherPackageInfo ->
                otherPackageInfo.packageName.value to otherPackageInfo.appName.value
            }.let { otherPackagesInfoValues ->
                basePackagesInfo.map { basePackageInfo ->
                    calculateItemDiffType(
                        baseItemFieldValue = basePackageInfo.appName.value,
                        otherItemFieldValue = otherPackagesInfoValues[basePackageInfo.packageName.value].orEmpty()
                    )
                }
            }.let { itemDiffTypes ->
                ItemDiffType.None to itemDiffTypes
            }
        }
    }

}
