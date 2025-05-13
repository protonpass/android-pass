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

package proton.android.pass.features.secure.links.listmenu.ui.options

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemAction
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.features.secure.links.listmenu.ui.SecureLinksListMenuUiEvent
import proton.android.pass.features.secure.links.listmenu.ui.copyLink
import proton.android.pass.features.secure.links.listmenu.ui.removeLink

@Composable
internal fun SecureLinksListMenuOptionsSingleLink(
    modifier: Modifier = Modifier,
    isLinkActive: Boolean,
    action: BottomSheetItemAction,
    onUiEvent: (SecureLinksListMenuUiEvent) -> Unit
) {
    listOfNotNull(
        copyLink(
            onClick = { onUiEvent(SecureLinksListMenuUiEvent.OnCopyLinkClicked) }
        ).takeIf { isLinkActive },

        removeLink(
            action = action,
            isActive = isLinkActive,
            onClick = { onUiEvent(SecureLinksListMenuUiEvent.OnRemoveLinkClicked) }
        )
    ).also { items ->
        BottomSheetItemList(
            modifier = modifier.bottomSheet(),
            items = items.withDividers().toPersistentList()
        )
    }
}

@[Preview Composable]
internal fun SecureLinksOptionsSingleLinkPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    val (isDark, isLinkActive) = input

    PassTheme(isDark = isDark) {
        Surface {
            SecureLinksListMenuOptionsSingleLink(
                isLinkActive = isLinkActive,
                action = BottomSheetItemAction.None,
                onUiEvent = {}
            )
        }
    }
}
