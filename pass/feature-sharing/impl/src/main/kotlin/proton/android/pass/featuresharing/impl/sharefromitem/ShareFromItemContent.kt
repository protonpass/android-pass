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

package proton.android.pass.featuresharing.impl.sharefromitem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultHighlightNorm
import me.proton.core.compose.theme.defaultWeak
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.featuresharing.impl.R
import me.proton.core.presentation.R as CoreR

@Composable
fun ShareFromItemContent(
    modifier: Modifier = Modifier,
    state: ShareFromItemUiState,
    onEvent: (ShareFromItemEvent) -> Unit
) {
    Column(
        modifier = modifier.padding(
            horizontal = PassTheme.dimens.bottomsheetHorizontalPadding,
            vertical = PassTheme.dimens.bottomsheetVerticalPadding
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.sharing_from_item_title),
                style = ProtonTheme.typography.defaultHighlightNorm,
                textAlign = TextAlign.Center
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.sharing_from_item_description),
                style = ProtonTheme.typography.defaultWeak,
                textAlign = TextAlign.Center
            )
        }

        if (state.vault is Some) {
            ShareThisVaultRow(
                vault = state.vault.value,
                onShareClick = { onEvent(ShareFromItemEvent.ShareVault) }
            )
        }

        if (state.showMoveToSharedVault) {
            ShareFromItemActionRow(
                modifier = Modifier.fillMaxWidth(),
                icon = CoreR.drawable.ic_proton_folder_arrow_in,
                title = R.string.sharing_from_item_move_to_shared_vault_action,
                onClick = { onEvent(ShareFromItemEvent.MoveToSharedVault) }
            )
        }

        if (state.showCreateVault) {
            ShareFromItemActionRow(
                modifier = Modifier.fillMaxWidth(),
                icon = CoreR.drawable.ic_proton_plus,
                title = R.string.sharing_from_item_create_vault_to_share_action,
                onClick = { onEvent(ShareFromItemEvent.CreateNewVault) }
            )
        }
    }
}
