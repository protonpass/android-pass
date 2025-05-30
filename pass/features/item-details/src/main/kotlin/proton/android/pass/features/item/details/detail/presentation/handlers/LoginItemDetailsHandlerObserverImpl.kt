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

import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsFieldType
import proton.android.pass.commonpresentation.api.items.details.handlers.ItemDetailsHandlerObserver
import proton.android.pass.commonrust.api.passwords.strengths.PasswordStrengthCalculator
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.commonuimodels.api.UIPasskeyContent
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.commonuimodels.api.items.DetailEvent
import proton.android.pass.commonuimodels.api.items.ItemDetailNavScope
import proton.android.pass.commonuimodels.api.items.ItemDetailState
import proton.android.pass.commonuimodels.api.items.LoginMonitorState
import proton.android.pass.commonuimodels.api.items.LoginMonitorState.ReusedPasswordDisplayMode
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.CanDisplayTotp
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemDiffType
import proton.android.pass.domain.ItemDiffs
import proton.android.pass.domain.ItemSection
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.Passkey
import proton.android.pass.domain.Share
import proton.android.pass.domain.TotpState
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.domain.entity.PackageInfo
import proton.android.pass.features.item.details.detail.navigation.ItemDetailScopeNavArgId
import proton.android.pass.features.item.details.detail.presentation.PassMonitorItemDetailFromMissing2FA
import proton.android.pass.features.item.details.detail.presentation.PassMonitorItemDetailFromReusedPassword
import proton.android.pass.features.item.details.detail.presentation.PassMonitorItemDetailFromWeakPassword
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import proton.android.pass.securitycenter.api.passwords.DuplicatedPasswordChecker
import proton.android.pass.securitycenter.api.passwords.InsecurePasswordChecker
import proton.android.pass.securitycenter.api.passwords.MissingTfaChecker
import proton.android.pass.telemetry.api.TelemetryManager
import proton.android.pass.totp.api.TotpManager
import javax.inject.Inject

private const val REUSED_PASSWORD_DISPLAY_MODE_THRESHOLD = 5

class LoginItemDetailsHandlerObserverImpl @Inject constructor(
    override val encryptionContextProvider: EncryptionContextProvider,
    override val totpManager: TotpManager,
    override val canDisplayTotp: CanDisplayTotp,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val passwordStrengthCalculator: PasswordStrengthCalculator,
    private val insecurePasswordChecker: InsecurePasswordChecker,
    private val duplicatedPasswordChecker: DuplicatedPasswordChecker,
    private val missingTfaChecker: MissingTfaChecker,
    private val telemetryManager: TelemetryManager
) : ItemDetailsHandlerObserver<ItemContents.Login, ItemDetailsFieldType.LoginItemAction>(
    encryptionContextProvider,
    totpManager,
    canDisplayTotp
) {

    override fun observe(
        share: Share,
        item: Item,
        attachmentsState: AttachmentsState,
        savedStateEntries: Map<String, Any?>,
        detailEvent: DetailEvent
    ): Flow<ItemDetailState> = combine(
        observeItemContents(item),
        observePrimaryTotp(item),
        observeCustomFieldTotps(item),
        observeLoginMonitorState(
            item = item,
            scope = savedStateEntries[ItemDetailScopeNavArgId.key]
                ?.let { it as? ItemDetailNavScope }
                ?: ItemDetailNavScope.Default
        ),
        userPreferencesRepository.getUseFaviconsPreference()
    ) { loginItemContents, primaryTotp, customFieldTotps, loginMonitorState,
        useFaviconsPreference ->
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
            attachmentsState = attachmentsState,
            loginMonitorState = loginMonitorState,
            detailEvent = detailEvent
        )
    }

    private fun observeLoginMonitorState(item: Item, scope: ItemDetailNavScope) = flow {
        sendTelemetry(scope)
        val insecurePasswordsReport = insecurePasswordChecker(listOf(item))
        val duplicatedPasswordsReport = duplicatedPasswordChecker(item)
        val missing2faReport = missingTfaChecker(listOf(item))
        val hasExceededDuplicationThreshold =
            duplicatedPasswordsReport.duplicationCount > REUSED_PASSWORD_DISPLAY_MODE_THRESHOLD
        val state = LoginMonitorState(
            isExcludedFromMonitor = item.hasSkippedHealthCheck,
            navigationScope = scope,
            isPasswordInsecure = insecurePasswordsReport.hasInsecurePasswords,
            isPasswordReused = duplicatedPasswordsReport.hasDuplications,
            isMissingTwoFa = missing2faReport.isMissingTwoFa,
            reusedPasswordDisplayMode = if (hasExceededDuplicationThreshold) {
                ReusedPasswordDisplayMode.Compact
            } else {
                ReusedPasswordDisplayMode.Expanded
            },
            reusedPasswordCount = duplicatedPasswordsReport.duplicationCount,
            reusedPasswordItems = duplicatedPasswordsReport.duplications
                .map { item ->
                    encryptionContextProvider.withEncryptionContext {
                        item.toUiModel(this@withEncryptionContext)
                    }
                }
                .toPersistentList()
        )
        emit(state)
    }

    private fun sendTelemetry(scope: ItemDetailNavScope) {
        when (scope) {
            ItemDetailNavScope.MonitorWeakPassword ->
                telemetryManager.sendEvent(PassMonitorItemDetailFromWeakPassword)

            ItemDetailNavScope.MonitorReusedPassword ->
                telemetryManager.sendEvent(PassMonitorItemDetailFromReusedPassword)

            ItemDetailNavScope.MonitorMissing2fa ->
                telemetryManager.sendEvent(PassMonitorItemDetailFromMissing2FA)

            ItemDetailNavScope.Default,
            ItemDetailNavScope.MonitorExcluded,
            ItemDetailNavScope.MonitorReport -> {
            }
        }
    }

    private fun observePrimaryTotp(item: Item): Flow<TotpState> = combine(
        observeItemContents(item),
        canDisplayTotp(shareId = item.shareId, itemId = item.id)
    ) { contents, canDisplayTotp -> Pair(contents, canDisplayTotp) }
        .flatMapLatest { (contents, canDisplayTotp) ->
            val totpUri = when (val hiddenField = contents.primaryTotp) {
                is HiddenState.Empty -> ""
                is HiddenState.Revealed -> hiddenField.clearText
                is HiddenState.Concealed -> encryptionContextProvider.withEncryptionContext {
                    decrypt(hiddenField.encrypted)
                }
            }
            when {
                totpUri.isBlank() -> flowOf(TotpState.Hidden)
                canDisplayTotp -> totpManager.observeCode(totpUri).map { totpWrapper ->
                    TotpState.Visible(
                        code = totpWrapper.code,
                        remainingSeconds = totpWrapper.remainingSeconds,
                        totalSeconds = totpWrapper.totalSeconds
                    )
                }
                else -> flowOf(TotpState.Limited)
            }
        }

    override fun updateHiddenFieldsContents(
        itemContents: ItemContents.Login,
        revealedHiddenCopyableFields: Map<ItemSection, Set<ItemDetailsFieldType.HiddenCopyable>>
    ): ItemContents {
        val revealedFields = revealedHiddenCopyableFields[ItemSection.Login] ?: emptyList()
        return itemContents.copy(
            password = updateHiddenStateValue(
                hiddenState = itemContents.password,
                shouldBeRevealed = revealedFields.any { it is ItemDetailsFieldType.HiddenCopyable.Password },
                encryptionContextProvider = encryptionContextProvider
            ),
            customFields = updateHiddenCustomFieldContents(
                customFields = itemContents.customFields,
                revealedHiddenFields = revealedHiddenCopyableFields[ItemSection.CustomField].orEmpty()
            )
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

    override suspend fun performAction(
        fieldType: ItemDetailsFieldType.LoginItemAction,
        callback: suspend (DetailEvent) -> Unit
    ) = Unit

}
