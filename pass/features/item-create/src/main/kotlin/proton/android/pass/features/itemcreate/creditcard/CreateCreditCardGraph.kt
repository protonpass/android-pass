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

package proton.android.pass.features.itemcreate.creditcard

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.toOption
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.features.itemcreate.bottomsheets.customfield.customFieldBottomSheetGraph
import proton.android.pass.features.itemcreate.common.CustomFieldPrefix
import proton.android.pass.features.itemcreate.common.KEY_FOLDER_SELECTED
import proton.android.pass.features.itemcreate.common.KEY_VAULT_SELECTED
import proton.android.pass.features.itemcreate.dialogs.customfield.CustomFieldNameNavigation
import proton.android.pass.features.itemcreate.dialogs.customfield.customFieldNameDialogGraph
import proton.android.pass.features.itemcreate.totp.INDEX_NAV_PARAMETER_KEY
import proton.android.pass.features.itemcreate.totp.TOTP_NAV_PARAMETER_KEY
import proton.android.pass.features.itemcreate.totp.createTotpGraph
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable
import proton.android.pass.navigation.api.toPath

object CreateCreditCardNavItem : NavItem(
    baseRoute = "creditcard/create/screen",
    optionalArgIds = listOf(
        CommonOptionalNavArgId.ShareId,
        CommonOptionalNavArgId.ItemId,
        CommonOptionalNavArgId.FolderId
    )
) {
    fun createNavRoute(
        shareId: Option<ShareId> = None,
        itemId: Option<ItemId> = None,
        folderId: Option<FolderId> = None
    ) = buildString {
        append(baseRoute)
        val map = mutableMapOf<String, Any>()
        if (shareId is Some) {
            map[CommonOptionalNavArgId.ShareId.key] = shareId.value.id
        }
        if (itemId is Some) {
            map[CommonOptionalNavArgId.ItemId.key] = itemId.value.id
        }
        if (folderId is Some) {
            map[CommonOptionalNavArgId.FolderId.key] = folderId.value.id
        }
        val path = map.toPath()
        append(path)
    }
}

sealed interface CreateCreditCardNavigation : BaseCreditCardNavigation {
    @JvmInline
    value class ItemCreated(val itemUiModel: ItemUiModel) : CreateCreditCardNavigation

    data class SelectVault(val shareId: ShareId, val folderId: FolderId? = null) : CreateCreditCardNavigation
}

@Suppress("LongMethod")
fun NavGraphBuilder.createCreditCardGraph(canUseAttachments: Boolean, onNavigate: (BaseCreditCardNavigation) -> Unit) {
    composable(CreateCreditCardNavItem) { navBackStack ->
        val selectVault by navBackStack.savedStateHandle
            .getStateFlow<String?>(KEY_VAULT_SELECTED, null)
            .collectAsStateWithLifecycle()

        val selectFolder by navBackStack.savedStateHandle
            .getStateFlow<String?>(KEY_FOLDER_SELECTED, null)
            .collectAsStateWithLifecycle()

        val navTotpUri by navBackStack.savedStateHandle
            .getStateFlow<String?>(TOTP_NAV_PARAMETER_KEY, null)
            .collectAsStateWithLifecycle()

        LaunchedEffect(navTotpUri) {
            navBackStack.savedStateHandle.remove<String?>(TOTP_NAV_PARAMETER_KEY)
        }

        val navTotpIndex by navBackStack.savedStateHandle
            .getStateFlow<Int?>(INDEX_NAV_PARAMETER_KEY, null)
            .collectAsStateWithLifecycle()

        LaunchedEffect(navTotpIndex) {
            navBackStack.savedStateHandle.remove<Int?>(INDEX_NAV_PARAMETER_KEY)
        }

        CreateCreditCardScreen(
            selectVault = selectVault.toOption().map { ShareId(it) }.value(),
            selectFolder = selectFolder.toOption().map { FolderId(it) }.value(),
            navTotpUri = navTotpUri,
            navTotpIndex = navTotpIndex,
            canUseAttachments = canUseAttachments,
            onNavigate = onNavigate
        )
    }
    customFieldBottomSheetGraph(
        prefix = CustomFieldPrefix.CreateCreditCard,
        onAddCustomFieldNavigate = { type, _ ->
            onNavigate(BaseCreditCardNavigation.CustomFieldTypeSelected(type))
        },
        onEditCustomFieldNavigate = { title: String, index: Int, _: Option<Int> ->
            onNavigate(BaseCreditCardNavigation.EditCustomField(title, index))
        },
        onRemoveCustomFieldNavigate = { onNavigate(BaseCreditCardNavigation.RemovedCustomField) },
        onDismissBottomsheet = { onNavigate(BaseCreditCardNavigation.DismissBottomsheet) }
    )
    customFieldNameDialogGraph(CustomFieldPrefix.CreateCreditCard) {
        when (it) {
            is CustomFieldNameNavigation.CloseScreen -> {
                onNavigate(BaseCreditCardNavigation.CloseScreen)
            }
        }
    }
    createTotpGraph(
        prefix = CustomFieldPrefix.CreateCreditCard,
        onSuccess = { totp, _, _, index ->
            val values = buildMap<String, Any> {
                put(TOTP_NAV_PARAMETER_KEY, totp)
                index?.let { put(INDEX_NAV_PARAMETER_KEY, it) }
            }
            onNavigate(BaseCreditCardNavigation.TotpSuccess(values))
        },
        onCloseTotp = { onNavigate(BaseCreditCardNavigation.TotpCancel) },
        onOpenImagePicker = { _, _, index ->
            onNavigate(BaseCreditCardNavigation.OpenImagePicker(index.toOption()))
        }
    )
}

