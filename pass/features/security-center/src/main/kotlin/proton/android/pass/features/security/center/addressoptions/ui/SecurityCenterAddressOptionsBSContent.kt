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

package proton.android.pass.features.security.center.addressoptions.ui

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
import proton.android.pass.features.security.center.R
import proton.android.pass.features.security.center.addressoptions.navigation.AddressOptionsType
import proton.android.pass.features.security.center.addressoptions.presentation.SecurityCenterAddressOptionsState
import me.proton.core.presentation.R as CoreR

@Composable
internal fun SecurityCenterAddressOptionsBSContent(
    modifier: Modifier = Modifier,
    state: SecurityCenterAddressOptionsState,
    onClick: (SecurityCenterAddressOptionsUiEvent) -> Unit
) {
    val list = mutableListOf<BottomSheetItem>()
    when (state.addressOptionsType) {
        AddressOptionsType.EnableMonitoring -> list.add(
            enableMonitoring(state.isLoading()) {
                onClick(SecurityCenterAddressOptionsUiEvent.EnableMonitoring)
            }
        )

        AddressOptionsType.DisableMonitoring -> list.add(
            disableMonitoring(state.isLoading()) {
                onClick(SecurityCenterAddressOptionsUiEvent.DisableMonitoring)
            }
        )

        AddressOptionsType.Unknown -> {}
    }
    BottomSheetItemList(
        modifier = modifier.bottomSheet(),
        items = list.withDividers().toPersistentList()
    )
}

private fun enableMonitoring(loading: Boolean, onClick: () -> Unit): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = {
            BottomSheetItemTitle(
                text = stringResource(R.string.security_center_address_bottomsheet_enable_monitoring)
            )
        }
    override val subtitle: (@Composable () -> Unit)?
        get() = null
    override val leftIcon: (@Composable () -> Unit)
        get() = { BottomSheetItemIcon(iconId = CoreR.drawable.ic_proton_eye) }
    override val endIcon: (@Composable () -> Unit)?
        get() = if (loading) {
            { CircularProgressIndicator(modifier = Modifier.size(24.dp)) }
        } else null
    override val onClick: () -> Unit
        get() = { onClick() }
    override val isDivider = false
}

private fun disableMonitoring(loading: Boolean, onClick: () -> Unit): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = {
            BottomSheetItemTitle(
                text = stringResource(R.string.security_center_address_bottomsheet_disable_monitoring)
            )
        }
    override val subtitle: (@Composable () -> Unit)?
        get() = null
    override val leftIcon: (@Composable () -> Unit)
        get() = { BottomSheetItemIcon(iconId = CoreR.drawable.ic_proton_eye_slash) }
    override val endIcon: (@Composable () -> Unit)?
        get() = if (loading) {
            { CircularProgressIndicator(modifier = Modifier.size(24.dp)) }
        } else null
    override val onClick: () -> Unit
        get() = { onClick() }
    override val isDivider = false
}
