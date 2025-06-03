/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.features.itemcreate.alias

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.toOption
import proton.android.pass.domain.ShareId
import proton.android.pass.features.itemcreate.alias.bottomsheet.CreateAliasBottomSheet
import proton.android.pass.features.itemcreate.alias.mailboxes.ui.SelectMailboxesBottomsheet
import proton.android.pass.features.itemcreate.alias.suffixes.ui.SelectSuffixBottomsheet
import proton.android.pass.features.itemcreate.bottomsheets.customfield.customFieldBottomSheetGraph
import proton.android.pass.features.itemcreate.common.CustomFieldPrefix
import proton.android.pass.features.itemcreate.common.KEY_VAULT_SELECTED
import proton.android.pass.features.itemcreate.dialogs.customfield.CustomFieldNameNavigation
import proton.android.pass.features.itemcreate.dialogs.customfield.customFieldNameDialogGraph
import proton.android.pass.features.itemcreate.totp.INDEX_NAV_PARAMETER_KEY
import proton.android.pass.features.itemcreate.totp.TOTP_NAV_PARAMETER_KEY
import proton.android.pass.features.itemcreate.totp.createTotpGraph
import proton.android.pass.navigation.api.AliasOptionalNavArgId
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavItemType
import proton.android.pass.navigation.api.OptionalNavArgId
import proton.android.pass.navigation.api.ShowUpgradeNavArgId
import proton.android.pass.navigation.api.bottomSheet
import proton.android.pass.navigation.api.composable
import proton.android.pass.navigation.api.toPath

object IsEditAliasNavArg : OptionalNavArgId {
    override val key = "isEdit"
    override val navType = NavType.BoolType
}

object CreateAliasNavItem : NavItem(
    baseRoute = "alias/create/screen",
    optionalArgIds = listOf(CommonOptionalNavArgId.ShareId, AliasOptionalNavArgId.Title)
) {
    fun createNavRoute(shareId: Option<ShareId> = None, title: Option<String> = None) = buildString {
        append(baseRoute)
        val map = mutableMapOf<String, Any>()
        if (shareId is Some) {
            map[CommonOptionalNavArgId.ShareId.key] = shareId.value.id
        }
        if (title is Some) {
            map[AliasOptionalNavArgId.Title.key] = title.value
        }
        val path = map.toPath()
        append(path)
    }
}

object CreateAliasBottomSheet : NavItem(
    baseRoute = "alias/create/bottomsheet",
    navArgIds = listOf(CommonOptionalNavArgId.ShareId, ShowUpgradeNavArgId),
    optionalArgIds = listOf(AliasOptionalNavArgId.Title, IsEditAliasNavArg),
    navItemType = NavItemType.Bottomsheet
) {
    fun createNavRoute(
        shareId: ShareId,
        showUpgrade: Boolean,
        title: Option<String> = None,
        isEdit: Boolean = false
    ): String = buildString {
        append("$baseRoute/${shareId.id}/$showUpgrade")

        val map = mutableMapOf<String, Any>(
            IsEditAliasNavArg.key to isEdit
        )
        if (title is Some) {
            map[AliasOptionalNavArgId.Title.key] = title.value
        }

        val optionalPath = map.toPath()
        append(optionalPath)
    }
}

data object AliasSelectSuffixBottomSheetNavItem : NavItem(
    baseRoute = "alias/select/suffix/bottomsheet",
    navItemType = NavItemType.Bottomsheet
)

data object AliasSelectMailboxBottomSheetNavItem : NavItem(
    baseRoute = "alias/select/mailbox/bottomsheet",
    navItemType = NavItemType.Bottomsheet
)

@Suppress("LongMethod")
fun NavGraphBuilder.createAliasGraph(
    canUseAttachments: Boolean,
    canAddMailbox: Boolean,
    onNavigate: (BaseAliasNavigation) -> Unit
) {
    composable(CreateAliasNavItem) { navBackStack ->
        val selectVault by navBackStack.savedStateHandle
            .getStateFlow<String?>(KEY_VAULT_SELECTED, null)
            .collectAsStateWithLifecycle()

        CreateAliasScreen(
            selectVault = selectVault.toOption().map { ShareId(it) }.value(),
            canUseAttachments = canUseAttachments,
            onNavigate = onNavigate
        )
    }

    bottomSheet(CreateAliasBottomSheet) {
        val itemTitle = it.arguments?.getString(AliasOptionalNavArgId.Title.key) ?: ""
        CreateAliasBottomSheet(
            itemTitle = itemTitle,
            onNavigate = onNavigate
        )
    }

    bottomSheet(AliasSelectSuffixBottomSheetNavItem) {
        SelectSuffixBottomsheet(
            onNavigate = onNavigate
        )
    }
    bottomSheet(AliasSelectMailboxBottomSheetNavItem) {
        SelectMailboxesBottomsheet(
            canAddMailbox = canAddMailbox,
            onNavigate = onNavigate
        )
    }
    customFieldBottomSheetGraph(
        prefix = CustomFieldPrefix.CreateAlias,
        onAddCustomFieldNavigate = { type, _ ->
            onNavigate(BaseAliasNavigation.CustomFieldTypeSelected(type))
        },
        onEditCustomFieldNavigate = { title: String, index: Int, _: Option<Int> ->
            onNavigate(BaseAliasNavigation.EditCustomField(title, index))
        },
        onRemoveCustomFieldNavigate = { onNavigate(BaseAliasNavigation.RemovedCustomField) },
        onDismissBottomsheet = { onNavigate(BaseAliasNavigation.CloseBottomsheet) }
    )
    customFieldNameDialogGraph(CustomFieldPrefix.CreateAlias) {
        when (it) {
            is CustomFieldNameNavigation.CloseScreen -> {
                onNavigate(BaseAliasNavigation.CloseScreen)
            }
        }
    }
    createTotpGraph(
        prefix = CustomFieldPrefix.CreateAlias,
        onSuccess = { totp, _, index ->
            val values = buildMap<String, Any> {
                put(TOTP_NAV_PARAMETER_KEY, totp)
                index?.let { put(INDEX_NAV_PARAMETER_KEY, it) }
            }
            onNavigate(BaseAliasNavigation.TotpSuccess(values))
        },
        onCloseTotp = { onNavigate(BaseAliasNavigation.TotpCancel) },
        onOpenImagePicker = { _, index ->
            onNavigate(BaseAliasNavigation.OpenImagePicker(index.toOption()))
        }
    )
}
