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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import proton.android.pass.common.api.combineN
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
import proton.android.pass.domain.Totp
import proton.android.pass.domain.items.ItemCustomField
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
) : ItemDetailsHandlerObserver {

    private val loginItemContentsFlow = MutableStateFlow<ItemContents.Login?>(null)

    override fun observe(item: Item): Flow<ItemDetailState> = combineN(
        observeLoginItemContents(item),
        observePrimaryTotp(item),
        observeCustomFields(item),
        getVaultById(shareId = item.shareId),
        userPreferencesRepository.getUseFaviconsPreference(),
        featureFlagsRepository.get<Boolean>(FeatureFlag.USERNAME_SPLIT)
    ) { loginItemContents, primaryTotp, customFields, vault, useFaviconsPreference, isUsernameSplitEnabled ->
        ItemDetailState.Login(
            itemContents = loginItemContents,
            isItemPinned = item.isPinned,
            itemVault = vault,
            itemCreatedAt = item.createTime,
            itemModifiedAt = item.modificationTime,
            canLoadExternalImages = useFaviconsPreference.value(),
            passwordStrength = encryptionContextProvider.withEncryptionContext {
                decrypt(loginItemContents.password.encrypted)
                    .let(passwordStrengthCalculator::calculateStrength)
            },
            primaryTotp = primaryTotp,
            customFields = customFields,
            passkeys = loginItemContents.passkeys.map { passkey -> UIPasskeyContent.from(passkey) },
            isUsernameSplitEnabled = isUsernameSplitEnabled
        )
    }

    private fun observeLoginItemContents(item: Item): Flow<ItemContents.Login> = combine(
        loginItemContentsFlow,
        featureFlagsRepository.get<Boolean>(FeatureFlag.USERNAME_SPLIT)
    ) { loginItemContents, isUsernameSplitEnabled ->
        loginItemContents ?: encryptionContextProvider.withEncryptionContext {
            item.toItemContents(
                encryptionContext = this@withEncryptionContext,
                isUsernameSplitEnabled = isUsernameSplitEnabled,
                emailValidator = emailValidator
            ) as ItemContents.Login
        }
    }
        .distinctUntilChanged()
        .onEach { loginItemContents ->
            loginItemContentsFlow.update { loginItemContents }
        }

    private fun observePrimaryTotp(item: Item): Flow<Totp?> = observeLoginItemContents(item)
        .flatMapLatest { loginItemContents ->
            observeTotp(loginItemContents.primaryTotp)
        }

    private fun observeCustomFields(item: Item): Flow<List<ItemCustomField>> =
        observeLoginItemContents(item).flatMapLatest { loginItemContents ->
            if (loginItemContents.customFields.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(getCustomFieldsFlows(loginItemContents.customFields)) { itemCustomFields ->
                    itemCustomFields.asList()
                }
            }
        }

    private fun getCustomFieldsFlows(customFieldsContents: List<CustomFieldContent>): List<Flow<ItemCustomField>> =
        customFieldsContents.map { customFieldContent ->
            when (customFieldContent) {
                is CustomFieldContent.Hidden -> flowOf(
                    ItemCustomField.Hidden(
                        title = customFieldContent.label,
                        hiddenState = customFieldContent.value
                    )
                )

                is CustomFieldContent.Text -> flowOf(
                    ItemCustomField.Plain(
                        title = customFieldContent.label,
                        content = customFieldContent.value
                    )
                )

                is CustomFieldContent.Totp -> observeTotp(customFieldContent.value)
                    .map { customFieldTotp ->
                        ItemCustomField.Totp(
                            title = customFieldContent.label,
                            totp = customFieldTotp
                        )
                    }
            }
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

    override fun updateHiddenState(hiddenFieldType: ItemDetailsFieldType.Hidden, hiddenState: HiddenState) {
        loginItemContentsFlow.update { loginItemContents ->
            when (hiddenFieldType) {
                is ItemDetailsFieldType.Hidden.CustomField -> loginItemContents?.copy(
                    customFields = loginItemContents.customFields
                        .mapIndexed { index, customFieldContent ->
                            if (index == hiddenFieldType.index && customFieldContent is CustomFieldContent.Hidden) {
                                customFieldContent.copy(value = hiddenState)
                            } else {
                                customFieldContent
                            }
                        }
                )

                ItemDetailsFieldType.Hidden.Password -> loginItemContents?.copy(
                    password = hiddenState
                )

                ItemDetailsFieldType.Hidden.Cvv,
                ItemDetailsFieldType.Hidden.Pin -> loginItemContents

            }
        }
    }

}
