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

package proton.android.pass.features.sl.sync.domains.select.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.body3Bold
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.features.sl.sync.R
import proton.android.pass.features.sl.sync.domains.select.presentation.SimpleLoginSyncDomainSelectState

@Composable
internal fun SimpleLoginSyncDomainSelectContent(
    modifier: Modifier = Modifier,
    state: SimpleLoginSyncDomainSelectState,
    onUiEvent: (SimpleLoginSyncDomainSelectUiEvent) -> Unit
) = with(state) {
    Column(
        modifier = modifier.bottomSheet()
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.simple_login_sync_domain_select_title),
            textAlign = TextAlign.Center,
            style = PassTheme.typography.body3Bold(),
            color = PassTheme.colors.textNorm
        )

        aliasDomains.map { aliasDomain ->
            simpleLoginAliasDomainSelectItem(
                aliasDomain = aliasDomain,
                canSelectPremiumDomains = canSelectPremiumDomains,
                updatingAliasDomainOption = updatingAliasDomainOption,
                onClick = {
                    if (aliasDomain.isPremium && !canSelectPremiumDomains) {
                        SimpleLoginSyncDomainSelectUiEvent.OnUpsellClicked
                    } else {
                        SimpleLoginSyncDomainSelectUiEvent.OnDomainSelected(aliasDomain.domain)
                    }.also(onUiEvent)
                }
            )
        }.also { items ->
            BottomSheetItemList(
                items = items.withDividers().toPersistentList()
            )
        }
    }
}
