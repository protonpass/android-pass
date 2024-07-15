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

package proton.android.pass.commonpresentation.impl.items.details.handlers

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import proton.android.pass.common.api.combineN
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsFieldSection
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsFieldType
import proton.android.pass.commonpresentation.api.items.details.handlers.ItemDetailsHandlerObserver
import proton.android.pass.commonrust.api.EmailValidator
import proton.android.pass.commonrust.api.passwords.strengths.PasswordStrengthCalculator
import proton.android.pass.commonui.api.toItemContents
import proton.android.pass.commonuimodels.api.UIPasskeyContent
import proton.android.pass.commonuimodels.api.items.ItemDetailState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.GetVaultById
import proton.android.pass.domain.CustomFieldContent
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemDiffs
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.Totp
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import proton.android.pass.totp.api.TotpManager
import javax.inject.Inject

class LoginItemDetailsHandlerObserverImpl @Inject constructor(
    private val getVaultById: GetVaultById,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val passwordStrengthCalculator: PasswordStrengthCalculator,
    private val totpManager: TotpManager,
    private val featureFlagsRepository: FeatureFlagsPreferencesRepository,
    private val emailValidator: EmailValidator
) : ItemDetailsHandlerObserver<ItemContents.Login>() {

    override fun observe(item: Item): Flow<ItemDetailState> = combineN(
        observeLoginItemContents(item),
        observePrimaryTotp(item),
        observeSecondaryTotps(item),
        getVaultById(shareId = item.shareId),
        userPreferencesRepository.getUseFaviconsPreference(),
        featureFlagsRepository.get<Boolean>(FeatureFlag.USERNAME_SPLIT)
    ) { loginItemContents, primaryTotp, secondaryTotps, vault, useFaviconsPreference, isUsernameSplitEnabled ->
        ItemDetailState.Login(
            itemContents = loginItemContents,
            itemId = item.id,
            shareId = item.shareId,
            isItemPinned = item.isPinned,
            itemVault = vault,
            itemCreatedAt = item.createTime,
            itemModifiedAt = item.modificationTime,
            itemLastAutofillAtOption = item.lastAutofillTime,
            itemRevision = item.revision,
            itemState = ItemState.from(item.state),
            itemDiffs = ItemDiffs.Login(),
            canLoadExternalImages = useFaviconsPreference.value(),
            passwordStrength = encryptionContextProvider.withEncryptionContext {
                decrypt(loginItemContents.password.encrypted)
                    .let(passwordStrengthCalculator::calculateStrength)
            },
            primaryTotp = primaryTotp,
            secondaryTotps = secondaryTotps,
            passkeys = loginItemContents.passkeys.map { passkey -> UIPasskeyContent.from(passkey) },
            isUsernameSplitEnabled = isUsernameSplitEnabled
        )
    }

    private fun observeLoginItemContents(item: Item): Flow<ItemContents.Login> = flow {
        featureFlagsRepository.get<Boolean>(FeatureFlag.USERNAME_SPLIT).first()
            .let { isUsernameSplitEnabled ->
                encryptionContextProvider.withEncryptionContext {
                    item.toItemContents(
                        encryptionContext = this@withEncryptionContext,
                        isUsernameSplitEnabled = isUsernameSplitEnabled,
                        emailValidator = emailValidator
                    ) as ItemContents.Login
                }
            }
            .let { loginItemContents ->
                emit(loginItemContents)
            }
    }

    private fun observePrimaryTotp(item: Item): Flow<Totp?> = observeLoginItemContents(item)
        .flatMapLatest { loginItemContents ->
            observeTotp(loginItemContents.primaryTotp)
        }

    private fun observeSecondaryTotps(item: Item): Flow<Map<String, Totp?>> =
        observeLoginItemContents(item).flatMapLatest { loginItemContents ->
            loginItemContents.customFields
                .filterIsInstance<CustomFieldContent.Totp>()
                .let { totpCustomFieldsContent ->
                    combine(
                        observeTotpCustomFieldsLabels(totpCustomFieldsContent),
                        observeTotpCustomFieldsValues(totpCustomFieldsContent)
                    ) { totpCustomFieldsLabels, totpCustomFieldsValues ->
                        totpCustomFieldsLabels.zip(totpCustomFieldsValues).toMap()
                    }
                }
        }.onStart { emit(emptyMap()) }

    private fun observeTotpCustomFieldsLabels(
        totpCustomFieldsContent: List<CustomFieldContent.Totp>
    ): Flow<List<String>> = totpCustomFieldsContent.map { totpCustomFieldContent ->
        totpCustomFieldContent.label
    }.let(::flowOf)

    private fun observeTotpCustomFieldsValues(
        totpCustomFieldsContent: List<CustomFieldContent.Totp>
    ): Flow<List<Totp?>> = combine(
        totpCustomFieldsContent.map { totpCustomFieldContent -> observeTotp(totpCustomFieldContent.value) },
        Array<Totp?>::asList
    )

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

    override fun updateItemContents(
        itemContents: ItemContents.Login,
        hiddenFieldType: ItemDetailsFieldType.Hidden,
        hiddenFieldSection: ItemDetailsFieldSection,
        hiddenState: HiddenState
    ): ItemContents = when (hiddenFieldType) {
        is ItemDetailsFieldType.Hidden.CustomField -> itemContents.copy(
            customFields = toggleHiddenCustomField(
                customFieldsContent = itemContents.customFields,
                hiddenFieldType = hiddenFieldType,
                hiddenState = hiddenState
            )
        )

        ItemDetailsFieldType.Hidden.Password -> itemContents.copy(
            password = hiddenState
        )

        ItemDetailsFieldType.Hidden.Cvv,
        ItemDetailsFieldType.Hidden.Pin -> itemContents
    }

    override fun calculateItemDiffs(
        baseItemDetailState: ItemContents.Login,
        otherItemDetailState: ItemContents.Login
    ): ItemDiffs.Login = encryptionContextProvider.withEncryptionContext {
        ItemDiffs.Login(
            title = calculateItemDiffType(
                baseItemFieldValue = baseItemDetailState.title,
                otherItemFieldValue = otherItemDetailState.title
            ),
            email = calculateItemDiffType(
                baseItemFieldValue = baseItemDetailState.itemEmail,
                otherItemFieldValue = otherItemDetailState.itemEmail
            ),
            username = calculateItemDiffType(
                baseItemFieldValue = baseItemDetailState.itemUsername,
                otherItemFieldValue = otherItemDetailState.itemUsername
            ),
            password = calculateItemDiffType(
                encryptionContext = this@withEncryptionContext,
                baseItemFieldHiddenState = baseItemDetailState.password,
                otherItemFieldHiddenState = otherItemDetailState.password
            ),
            totp = calculateItemDiffType(
                encryptionContext = this@withEncryptionContext,
                baseItemFieldHiddenState = baseItemDetailState.password,
                otherItemFieldHiddenState = otherItemDetailState.password
            ),
            note = calculateItemDiffType(
                baseItemFieldValue = baseItemDetailState.note,
                otherItemFieldValue = otherItemDetailState.note
            )
        )
    }

}
