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

package proton.android.pass.features.alias.contacts.options.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.features.alias.contacts.options.presentation.OptionsAliasUIState
import proton.android.pass.features.alias.contacts.options.ui.OptionsAliasBottomSheetUiEvent.OnBlockContactClicked
import proton.android.pass.features.alias.contacts.options.ui.OptionsAliasBottomSheetUiEvent.OnCopyAddressClicked
import proton.android.pass.features.alias.contacts.options.ui.OptionsAliasBottomSheetUiEvent.OnDeleteContactClicked
import proton.android.pass.features.alias.contacts.options.ui.OptionsAliasBottomSheetUiEvent.OnSendEmailClicked
import proton.android.pass.features.alias.contacts.options.ui.OptionsAliasBottomSheetUiEvent.OnUnblockContactClicked
import proton.android.pass.features.aliascontacts.R
import me.proton.core.presentation.R as CoreR

@Composable
internal fun OptionsAliasContactContent(
    modifier: Modifier = Modifier,
    state: OptionsAliasUIState,
    onUiEvent: (OptionsAliasBottomSheetUiEvent) -> Unit
) {
    val bottomSheetItems = buildList {
        if (state.contact != null && !state.contact.blocked) {
            add(
                sendEmail(
                    isEnabled = !state.isAnyLoading,
                    onClick = { onUiEvent(OnSendEmailClicked) }
                )
            )
        }
        add(
            copyEmail(
                isEnabled = !state.isAnyLoading,
                onClick = { onUiEvent(OnCopyAddressClicked) }
            )
        )
        if (state.contact != null) {
            if (state.contact.blocked) {
                add(
                    unblockContact(
                        isEnabled = !state.isAnyLoading,
                        isLoading = state.isBlockLoading,
                        onClick = { onUiEvent(OnUnblockContactClicked) }
                    )
                )
            } else {
                add(
                    blockContact(
                        isEnabled = !state.isAnyLoading,
                        isLoading = state.isBlockLoading,
                        onClick = { onUiEvent(OnBlockContactClicked) }
                    )
                )
            }
        }
        add(
            deleteContact(
                isEnabled = !state.isAnyLoading,
                isLoading = state.isDeleteLoading,
                onClick = { onUiEvent(OnDeleteContactClicked) }
            )
        )
    }.withDividers().toPersistentList()

    BottomSheetItemList(
        modifier = modifier.bottomSheet(),
        items = bottomSheetItems
    )
}

internal fun sendEmail(isEnabled: Boolean, onClick: () -> Unit): BottomSheetItem = object : BottomSheetItem {

    override val title: @Composable () -> Unit = {
        BottomSheetItemTitle(
            text = stringResource(R.string.contact_option_send_email),
            isEnabled = isEnabled
        )
    }

    override val subtitle: (@Composable () -> Unit)? = null

    override val leftIcon: (@Composable () -> Unit) = {
        BottomSheetItemIcon(iconId = CoreR.drawable.ic_proton_paper_plane)
    }

    override val endIcon: (@Composable () -> Unit)? = null

    override val onClick: (() -> Unit)? = onClick.takeIf { isEnabled }

    override val isDivider = false

}

internal fun copyEmail(isEnabled: Boolean, onClick: () -> Unit): BottomSheetItem = object : BottomSheetItem {

    override val title: @Composable () -> Unit = {
        BottomSheetItemTitle(
            text = stringResource(R.string.contact_option_copy_address),
            isEnabled = isEnabled
        )
    }

    override val subtitle: (@Composable () -> Unit)? = null

    override val leftIcon: (@Composable () -> Unit) = {
        BottomSheetItemIcon(iconId = CoreR.drawable.ic_proton_squares)
    }

    override val endIcon: (@Composable () -> Unit)? = null

    override val onClick: (() -> Unit)? = onClick.takeIf { isEnabled }

    override val isDivider = false

}

internal fun blockContact(
    isLoading: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
): BottomSheetItem = object : BottomSheetItem {

    override val title: @Composable () -> Unit = {
        BottomSheetItemTitle(
            text = stringResource(R.string.contact_option_block_contact),
            isEnabled = isEnabled
        )
    }

    override val subtitle: (@Composable () -> Unit)? = null

    override val leftIcon: (@Composable () -> Unit) = {
        BottomSheetItemIcon(iconId = CoreR.drawable.ic_proton_circle_slash)
    }

    override val endIcon: (@Composable () -> Unit)? = if (isLoading) {
        { CircularProgressIndicator(modifier = Modifier.size(20.dp)) }
    } else null

    override val onClick: (() -> Unit)? = onClick.takeIf { isEnabled }

    override val isDivider = false

}

internal fun unblockContact(
    isLoading: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
): BottomSheetItem = object : BottomSheetItem {

    override val title: @Composable () -> Unit = {
        BottomSheetItemTitle(
            text = stringResource(R.string.contact_option_unblock_contact),
            isEnabled = isEnabled
        )
    }

    override val subtitle: (@Composable () -> Unit)? = null

    override val leftIcon: (@Composable () -> Unit) = {
        BottomSheetItemIcon(iconId = CoreR.drawable.ic_proton_circle_slash)
    }

    override val endIcon: (@Composable () -> Unit)? = if (isLoading) {
        { CircularProgressIndicator(modifier = Modifier.size(20.dp)) }
    } else null

    override val onClick: (() -> Unit)? = onClick.takeIf { isEnabled }

    override val isDivider = false

}

internal fun deleteContact(
    isLoading: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
): BottomSheetItem = object : BottomSheetItem {

    override val title: @Composable () -> Unit = {
        BottomSheetItemTitle(
            text = stringResource(R.string.contact_option_delete),
            isEnabled = isEnabled
        )
    }

    override val subtitle: (@Composable () -> Unit)? = null

    override val leftIcon: (@Composable () -> Unit) = {
        BottomSheetItemIcon(iconId = CoreR.drawable.ic_proton_trash)
    }

    override val endIcon: (@Composable () -> Unit)? = if (isLoading) {
        { CircularProgressIndicator(modifier = Modifier.size(20.dp)) }
    } else null

    override val onClick: (() -> Unit)? = onClick.takeIf { isEnabled }

    override val isDivider = false

}
