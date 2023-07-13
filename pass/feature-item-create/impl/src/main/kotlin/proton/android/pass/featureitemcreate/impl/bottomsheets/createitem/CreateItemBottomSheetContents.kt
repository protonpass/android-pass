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

package proton.android.pass.featureitemcreate.impl.bottomsheets.createitem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.toPersistentList
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import me.proton.core.compose.theme.defaultSmallNorm
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.composecomponents.impl.item.icon.AliasIcon
import proton.android.pass.composecomponents.impl.item.icon.CreditCardIcon
import proton.android.pass.composecomponents.impl.item.icon.LoginIcon
import proton.android.pass.composecomponents.impl.item.icon.NoteIcon
import proton.android.pass.composecomponents.impl.item.icon.PasswordIcon
import proton.android.pass.featureitemcreate.impl.R
import proton.android.pass.featureitemcreate.impl.bottomsheets.createitem.CreateItemBottomsheetNavigation.CreateAlias
import proton.android.pass.featureitemcreate.impl.bottomsheets.createitem.CreateItemBottomsheetNavigation.CreateLogin
import proton.android.pass.featureitemcreate.impl.bottomsheets.createitem.CreateItemBottomsheetNavigation.CreateNote
import proton.android.pass.featureitemcreate.impl.bottomsheets.createitem.CreateItemBottomsheetNavigation.CreatePassword
import proton.pass.domain.ShareId

enum class CreateItemBottomSheetMode {
    Full,
    Autofill;
}

@ExperimentalMaterialApi
@Composable
fun CreateItemBottomSheetContents(
    modifier: Modifier = Modifier,
    state: CreateItemBottomSheetUIState,
    mode: CreateItemBottomSheetMode,
    onNavigate: (CreateItemBottomsheetNavigation) -> Unit,
) {

    val items = when (mode) {
        CreateItemBottomSheetMode.Full -> listOf(
            createLogin(state.shareId) { onNavigate(CreateLogin(it)) },
            createAlias(
                shareId = state.shareId,
                createItemAliasUIState = state.createItemAliasUIState
            ) { onNavigate(CreateAlias(it)) },
            createCreditCard(state.shareId) {
                onNavigate(CreateItemBottomsheetNavigation.CreateCreditCard(it))
            },
            createNote(state.shareId) { onNavigate(CreateNote(it)) },
            createPassword { onNavigate(CreatePassword) }
        )

        CreateItemBottomSheetMode.Autofill -> listOf(
            createLogin(state.shareId) { onNavigate(CreateLogin(it)) },
            createAlias(state.shareId, state.createItemAliasUIState) { onNavigate(CreateAlias(it)) }
        )
    }

    BottomSheetItemList(
        modifier = modifier.bottomSheet(),
        items = items.withDividers().toPersistentList()
    )
}

private fun createLogin(
    shareId: ShareId?,
    onCreateLogin: (Option<ShareId>) -> Unit
): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = { BottomSheetItemTitle(text = stringResource(id = R.string.action_login)) }
    override val subtitle: (@Composable () -> Unit)
        get() = {
            Text(
                text = stringResource(R.string.item_type_login_description),
                style = ProtonTheme.typography.defaultSmallNorm,
                color = ProtonTheme.colors.textWeak
            )
        }
    override val leftIcon: (@Composable () -> Unit)
        get() = { LoginIcon() }
    override val endIcon: (@Composable () -> Unit)?
        get() = null
    override val onClick: () -> Unit
        get() = { onCreateLogin(shareId.toOption()) }
    override val isDivider = false
}

private fun createAlias(
    shareId: ShareId?,
    createItemAliasUIState: CreateItemAliasUIState,
    onCreateAlias: (Option<ShareId>) -> Unit
): BottomSheetItem =
    object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.action_alias),
                        style = ProtonTheme.typography.defaultNorm,
                        color = PassTheme.colors.textNorm
                    )
                    if (createItemAliasUIState.canUpgrade) {
                        val color =
                            if (createItemAliasUIState.aliasCount >= createItemAliasUIState.aliasLimit) {
                                PassTheme.colors.signalDanger
                            } else {
                                PassTheme.colors.textWeak
                            }
                        Text(
                            text = "(${createItemAliasUIState.aliasCount}/${createItemAliasUIState.aliasLimit})",
                            style = ProtonTheme.typography.defaultNorm,
                            color = color
                        )
                    }
                }
            }
        override val subtitle: (@Composable () -> Unit)
            get() = {
                Text(
                    text = stringResource(R.string.item_type_alias_description),
                    style = ProtonTheme.typography.defaultSmallNorm,
                    color = ProtonTheme.colors.textWeak
                )
            }
        override val leftIcon: (@Composable () -> Unit)
            get() = { AliasIcon() }
        override val endIcon: (@Composable () -> Unit)?
            get() = null
        override val onClick: () -> Unit
            get() = { onCreateAlias(shareId.toOption()) }
        override val isDivider = false
    }

private fun createCreditCard(
    shareId: ShareId?,
    onCreateCreditCard: (Option<ShareId>) -> Unit
): BottomSheetItem =
    object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = { BottomSheetItemTitle(text = stringResource(id = R.string.action_credit_card)) }
        override val subtitle: (@Composable () -> Unit)
            get() = {
                Text(
                    text = stringResource(R.string.item_type_credit_card_description),
                    style = ProtonTheme.typography.defaultSmallNorm,
                    color = ProtonTheme.colors.textWeak
                )
            }
        override val leftIcon: (@Composable () -> Unit)
            get() = { CreditCardIcon() }
        override val endIcon: (@Composable () -> Unit)?
            get() = null
        override val onClick: () -> Unit
            get() = { onCreateCreditCard(shareId.toOption()) }
        override val isDivider = false
    }

private fun createNote(
    shareId: ShareId?,
    onCreateNote: (Option<ShareId>) -> Unit
): BottomSheetItem =
    object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = { BottomSheetItemTitle(text = stringResource(id = R.string.action_note)) }
        override val subtitle: (@Composable () -> Unit)
            get() = {
                Text(
                    text = stringResource(R.string.item_type_note_description),
                    style = ProtonTheme.typography.defaultSmallNorm,
                    color = ProtonTheme.colors.textWeak
                )
            }
        override val leftIcon: (@Composable () -> Unit)
            get() = { NoteIcon() }
        override val endIcon: (@Composable () -> Unit)?
            get() = null
        override val onClick: () -> Unit
            get() = { onCreateNote(shareId.toOption()) }
        override val isDivider = false
    }

private fun createPassword(onCreatePassword: () -> Unit): BottomSheetItem =
    object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = { BottomSheetItemTitle(text = stringResource(id = R.string.action_password)) }
        override val subtitle: (@Composable () -> Unit)
            get() = {
                Text(
                    text = stringResource(R.string.item_type_password_description),
                    style = ProtonTheme.typography.defaultSmallNorm,
                    color = ProtonTheme.colors.textWeak
                )
            }
        override val leftIcon: (@Composable () -> Unit)
            get() = { PasswordIcon() }
        override val endIcon: (@Composable () -> Unit)?
            get() = null
        override val onClick: () -> Unit
            get() = onCreatePassword
        override val isDivider = false
    }


class ThemeCreateItemBSPreviewProvider : ThemePairPreviewProvider<CreateItemBottomSheetUIState>(
    CreateItemBottomSheetUIStatePreviewProvider()
)

@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun CreateItemBottomSheetLimitPreview(
    @PreviewParameter(ThemeCreateItemBSPreviewProvider::class) input: Pair<Boolean, CreateItemBottomSheetUIState>
) {
    PassTheme(isDark = input.first) {
        Surface {
            CreateItemBottomSheetContents(
                mode = CreateItemBottomSheetMode.Full,
                onNavigate = {},
                state = input.second
            )
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun CreateItemBottomSheetContentsPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    val mode =
        if (input.second) CreateItemBottomSheetMode.Full else CreateItemBottomSheetMode.Autofill
    PassTheme(isDark = input.first) {
        Surface {
            CreateItemBottomSheetContents(
                mode = mode,
                onNavigate = {},
                state = CreateItemBottomSheetUIState.DEFAULT
            )
        }
    }
}
