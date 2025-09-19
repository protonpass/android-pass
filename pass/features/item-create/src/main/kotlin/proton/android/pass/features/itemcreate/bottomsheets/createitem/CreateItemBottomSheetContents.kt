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

package proton.android.pass.features.itemcreate.bottomsheets.createitem

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.collections.immutable.toPersistentList
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import me.proton.core.compose.theme.defaultSmallNorm
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.composecomponents.impl.item.icon.AliasIcon
import proton.android.pass.composecomponents.impl.item.icon.CreditCardIcon
import proton.android.pass.composecomponents.impl.item.icon.CustomItemIcon
import proton.android.pass.composecomponents.impl.item.icon.IdentityIcon
import proton.android.pass.composecomponents.impl.item.icon.LoginIcon
import proton.android.pass.composecomponents.impl.item.icon.NoteIcon
import proton.android.pass.composecomponents.impl.item.icon.PasswordIcon
import proton.android.pass.features.itemcreate.R
import proton.android.pass.features.itemcreate.bottomsheets.createitem.CreateItemBottomsheetNavigation.CreateAlias
import proton.android.pass.features.itemcreate.bottomsheets.createitem.CreateItemBottomsheetNavigation.CreateCreditCard
import proton.android.pass.features.itemcreate.bottomsheets.createitem.CreateItemBottomsheetNavigation.CreateCustom
import proton.android.pass.features.itemcreate.bottomsheets.createitem.CreateItemBottomsheetNavigation.CreateIdentity
import proton.android.pass.features.itemcreate.bottomsheets.createitem.CreateItemBottomsheetNavigation.CreateLogin
import proton.android.pass.features.itemcreate.bottomsheets.createitem.CreateItemBottomsheetNavigation.CreateNote
import proton.android.pass.features.itemcreate.bottomsheets.createitem.CreateItemBottomsheetNavigation.CreatePassword

@Composable
internal fun CreateItemBottomSheetContents(
    modifier: Modifier = Modifier,
    state: CreateItemBottomSheetUIState,
    onNavigate: (CreateItemBottomsheetNavigation) -> Unit
) = with(state) {
    when (mode) {
        CreateItemBottomSheetMode.HomeFull -> buildList {
            if (canCreateItems) {
                createLogin(
                    onClick = { onNavigate(CreateLogin(shareId.toOption())) }
                ).also(::add)

                createAlias(
                    createItemAliasUIState = createItemAliasUIState,
                    onClick = { onNavigate(CreateAlias(shareId.toOption())) }
                ).also(::add)

                createNote(
                    onClick = { onNavigate(CreateNote(shareId.toOption())) }
                ).also(::add)
            }

            createPassword(
                onClick = { onNavigate(CreatePassword) }
            ).also(::add)

            if (canCreateItems) {
                createCreditCard(
                    onClick = { onNavigate(CreateCreditCard(shareId.toOption())) }
                ).also(::add)

                createIdentity(
                    onClick = { onNavigate(CreateIdentity(shareId.toOption())) }
                ).also(::add)

                createCustom(
                    onClick = { onNavigate(CreateCustom(shareId.toOption())) }
                ).also(::add)
            }
        }

        CreateItemBottomSheetMode.AutofillLogin -> buildList {
            if (canCreateItems) {
                createLogin(
                    onClick = { onNavigate(CreateLogin(shareId.toOption())) }
                ).also(::add)

                createAlias(
                    createItemAliasUIState = createItemAliasUIState,
                    onClick = { onNavigate(CreateAlias(shareId.toOption())) }
                ).also(::add)
            }
        }

        CreateItemBottomSheetMode.AutofillCreditCard -> buildList {
            if (canCreateItems) {
                createCreditCard(
                    onClick = { onNavigate(CreateCreditCard(shareId.toOption())) }
                ).also(::add)
            }
        }

        CreateItemBottomSheetMode.AutofillIdentity -> buildList {
            if (canCreateItems) {
                createIdentity(
                    onClick = { onNavigate(CreateIdentity(shareId.toOption())) }
                ).also(::add)
            }
        }

        null -> emptyList()
    }.let { items ->
        BottomSheetItemList(
            modifier = modifier.bottomSheet(),
            items = items.withDividers().toPersistentList()
        )
    }
}

@Composable
private fun createLogin(onClick: () -> Unit) = createItem(
    title = { BottomSheetItemTitle(text = stringResource(id = R.string.action_login)) },
    subtitleResId = R.string.item_type_login_description,
    leftIcon = { LoginIcon() },
    onClick = onClick
)

@Composable
private fun createAlias(createItemAliasUIState: CreateItemAliasUIState, onClick: () -> Unit) = createItem(
    title = {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = Spacing.extraSmall)
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
    },
    subtitleResId = R.string.item_type_alias_description,
    leftIcon = { AliasIcon() },
    onClick = onClick
)

@Composable
private fun createCreditCard(onClick: () -> Unit) = createItem(
    title = { BottomSheetItemTitle(text = stringResource(id = R.string.action_credit_card)) },
    subtitleResId = R.string.item_type_credit_card_description,
    leftIcon = { CreditCardIcon() },
    onClick = onClick
)

@Composable
private fun createNote(onClick: () -> Unit) = createItem(
    title = { BottomSheetItemTitle(text = stringResource(id = R.string.action_note)) },
    subtitleResId = R.string.item_type_note_description,
    leftIcon = { NoteIcon() },
    onClick = onClick
)

@Composable
private fun createIdentity(onClick: () -> Unit) = createItem(
    title = { BottomSheetItemTitle(text = stringResource(id = R.string.action_identity)) },
    subtitleResId = R.string.item_type_identity_description,
    leftIcon = { IdentityIcon() },
    onClick = onClick
)

@Composable
private fun createCustom(onClick: () -> Unit) = createItem(
    title = { BottomSheetItemTitle(text = stringResource(id = R.string.action_more)) },
    subtitleResId = R.string.item_type_more_description,
    leftIcon = { CustomItemIcon() },
    onClick = onClick
)

@Composable
private fun createPassword(onClick: () -> Unit) = createItem(
    title = { BottomSheetItemTitle(text = stringResource(id = R.string.action_password)) },
    subtitleResId = R.string.item_type_password_description,
    leftIcon = { PasswordIcon() },
    onClick = onClick
)

private fun createItem(
    title: @Composable () -> Unit,
    @StringRes subtitleResId: Int,
    leftIcon: @Composable () -> Unit,
    onClick: () -> Unit
) = object : BottomSheetItem {

    override val title: @Composable () -> Unit = title

    override val subtitle: (@Composable () -> Unit) = {
        Text(
            text = stringResource(id = subtitleResId),
            style = ProtonTheme.typography.defaultSmallNorm,
            color = ProtonTheme.colors.textWeak
        )
    }

    override val leftIcon: (@Composable () -> Unit) = leftIcon

    override val endIcon: (@Composable () -> Unit)? = null

    override val onClick: () -> Unit = onClick

    override val isDivider = false

}

internal class ThemeCreateItemBSPreviewProvider :
    ThemePairPreviewProvider<CreateItemBottomSheetUIState>(
        CreateItemBottomSheetUIStatePreviewProvider()
    )

@[Preview Composable]
internal fun CreateItemBottomSheetLimitPreview(
    @PreviewParameter(ThemeCreateItemBSPreviewProvider::class) input: Pair<Boolean, CreateItemBottomSheetUIState>
) {
    PassTheme(isDark = input.first) {
        Surface {
            CreateItemBottomSheetContents(
                onNavigate = {},
                state = input.second
            )
        }
    }
}

internal class CreateItemBottomSheetModePreviewProvider :
    PreviewParameterProvider<CreateItemBottomSheetMode> {
    override val values: Sequence<CreateItemBottomSheetMode>
        get() = CreateItemBottomSheetMode.entries.asSequence()
}

internal class ThemedCreateItemModePreviewProvider :
    ThemePairPreviewProvider<CreateItemBottomSheetMode>(
        CreateItemBottomSheetModePreviewProvider()
    )

@[Preview Composable]
internal fun CreateItemBottomSheetContentsPreview(
    @PreviewParameter(ThemedCreateItemModePreviewProvider::class) input: Pair<Boolean, CreateItemBottomSheetMode>
) {
    PassTheme(isDark = input.first) {
        Surface {
            CreateItemBottomSheetContents(
                onNavigate = {},
                state = CreateItemBottomSheetUIState.Initial.copy(
                    mode = input.second,
                    canCreateItems = true
                )
            )
        }
    }
}
