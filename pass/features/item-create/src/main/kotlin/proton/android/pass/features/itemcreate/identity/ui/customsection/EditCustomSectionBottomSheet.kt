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

package proton.android.pass.features.itemcreate.identity.ui.customsection

import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.features.itemcreate.R
import proton.android.pass.features.itemcreate.identity.navigation.customsection.ExtraSectionNavigation
import proton.android.pass.features.itemcreate.identity.presentation.customsection.EditCustomSectionEvent
import proton.android.pass.features.itemcreate.identity.presentation.customsection.EditCustomSectionViewModel
import me.proton.core.presentation.R as CoreR

@Composable
fun EditCustomSectionBottomSheet(
    modifier: Modifier = Modifier,
    onNavigate: (ExtraSectionNavigation) -> Unit,
    viewModel: EditCustomSectionViewModel = hiltViewModel()
) {
    val state by viewModel.eventState.collectAsStateWithLifecycle()
    LaunchedEffect(state) {
        when (val event = state) {
            is EditCustomSectionEvent.EditSection ->
                onNavigate(ExtraSectionNavigation.EditCustomSection(event.index, event.title))
            EditCustomSectionEvent.RemovedField ->
                onNavigate(ExtraSectionNavigation.RemoveCustomSection)
            EditCustomSectionEvent.Unknown -> {}
        }
    }

    BottomSheetItemList(
        modifier = modifier.bottomSheet(),
        items = listOf(
            editField(viewModel::onEdit),
            deleteField(viewModel::onRemove)
        ).withDividers().toPersistentList()
    )
}

private fun editField(onClick: () -> Unit): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = { BottomSheetItemTitle(text = stringResource(id = R.string.bottomsheet_custom_field_option_edit)) }
    override val subtitle: (@Composable () -> Unit)?
        get() = null
    override val leftIcon: (@Composable () -> Unit)
        get() = {
            Icon(
                painter = painterResource(CoreR.drawable.ic_proton_pencil),
                contentDescription = null
            )
        }
    override val endIcon: (@Composable () -> Unit)?
        get() = null
    override val onClick: () -> Unit = onClick
    override val isDivider = false
}

private fun deleteField(onClick: () -> Unit): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = { BottomSheetItemTitle(text = stringResource(id = R.string.bottomsheet_custom_section_option_remove)) }
    override val subtitle: (@Composable () -> Unit)?
        get() = null
    override val leftIcon: (@Composable () -> Unit)
        get() = {
            Icon(
                painter = painterResource(CoreR.drawable.ic_proton_cross_circle),
                contentDescription = null
            )
        }
    override val endIcon: (@Composable () -> Unit)?
        get() = null
    override val onClick: () -> Unit = onClick
    override val isDivider = false
}

