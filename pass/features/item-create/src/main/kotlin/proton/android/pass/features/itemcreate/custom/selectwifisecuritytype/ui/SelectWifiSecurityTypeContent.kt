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

package proton.android.pass.features.itemcreate.custom.selectwifisecuritytype.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.toPersistentList
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.composecomponents.impl.stringhelpers.getWifiSecurityTypeText
import proton.android.pass.domain.WifiSecurityType
import me.proton.core.presentation.R as CoreR

@Composable
fun SelectWifiSecurityTypeContent(
    modifier: Modifier = Modifier,
    selectedWifiSecurityType: WifiSecurityType,
    onSelect: (WifiSecurityType) -> Unit
) {
    BottomSheetItemList(
        modifier = modifier,
        items = WifiSecurityType.entries.map {
            wifiSecurityTypeRow(it, it == selectedWifiSecurityType, onSelect)
        }.withDividers().toPersistentList()
    )
}

private fun wifiSecurityTypeRow(
    wifiSecurityType: WifiSecurityType,
    isSelected: Boolean,
    onClick: (WifiSecurityType) -> Unit
): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = {
            BottomSheetItemTitle(text = getWifiSecurityTypeText(wifiSecurityType))
        }
    override val subtitle: (@Composable () -> Unit)?
        get() = null
    override val leftIcon: (@Composable () -> Unit)?
        get() = null
    override val endIcon: @Composable (() -> Unit)? = if (isSelected) {
        {
            BottomSheetItemIcon(
                iconId = CoreR.drawable.ic_proton_checkmark,
                tint = ProtonTheme.colors.textNorm
            )
        }
    } else null
    override val onClick: () -> Unit
        get() = { onClick(wifiSecurityType) }
    override val isDivider = false
}
