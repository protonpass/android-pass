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

package proton.android.pass.features.passkeys.select.ui.bottomsheet.selectpasskey

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.datetime.Instant
import proton.android.pass.common.api.SpecialCharacters
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemSubtitle
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.domain.ByteArrayWrapper
import proton.android.pass.domain.Passkey
import proton.android.pass.domain.PasskeyId
import me.proton.core.presentation.R as CoreR

@Composable
fun SelectPasskeyBottomsheetContent(
    modifier: Modifier = Modifier,
    isLoading: IsLoadingState,
    passkeys: ImmutableList<Passkey>,
    onPasskeySelected: (Passkey) -> Unit
) {
    if (isLoading.value()) {
        Box(modifier = modifier.size(120.dp)) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.Center)
            )
        }
    } else {

        val items = passkeys.map { pk -> passkeyItem(pk) { onPasskeySelected(pk) } }

        BottomSheetItemList(
            modifier = modifier.bottomSheet(),
            items = items.withDividers().toPersistentList()
        )
    }
}

internal fun passkeyItem(item: Passkey, onClick: () -> Unit): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = {
            BottomSheetItemTitle(
                text = item.userName,
                color = PassTheme.colors.textNorm
            )
        }
    override val subtitle: (@Composable () -> Unit)
        get() = {
            val text = "${item.domain} ${SpecialCharacters.DOT_SEPARATOR} ${item.id.value}"
            BottomSheetItemSubtitle(
                text = text,
                color = PassTheme.colors.textWeak
            )
        }
    override val leftIcon: (@Composable () -> Unit)
        get() = {
            BottomSheetItemIcon(iconId = CoreR.drawable.ic_proton_key_skeleton)
        }
    override val endIcon: (@Composable () -> Unit)?
        get() = null
    override val onClick: (() -> Unit)
        get() = onClick
    override val isDivider = false
}

@Preview
@Composable
@Suppress("MagicNumber")
fun SelectPasskeyBSContentPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    val createPasskey = { id: String, domain: String, username: String ->
        Passkey(
            id = PasskeyId(id),
            domain = domain,
            rpId = null,
            rpName = domain,
            userName = username,
            userDisplayName = "",
            userId = ByteArrayWrapper(byteArrayOf()),
            contents = ByteArrayWrapper(byteArrayOf()),
            note = "",
            createTime = Instant.fromEpochSeconds(1_708_327_525),
            credentialId = ByteArrayWrapper(byteArrayOf()),
            userHandle = null,
            creationData = null
        )

    }

    PassTheme(isDark = isDark) {
        Surface {
            SelectPasskeyBottomsheetContent(
                isLoading = IsLoadingState.NotLoading,
                passkeys = listOf(
                    createPasskey("A1B2C3D4E5F6G7H8I9J0", "example.com", "user1"),
                    createPasskey("789ABC123DEF987DBC3A", "other.test", "user2"),
                    createPasskey("C1D2E5A9B4C5D6E4F80A", "some.local", "user3")
                ).toPersistentList(),
                onPasskeySelected = {}
            )
        }
    }
}

