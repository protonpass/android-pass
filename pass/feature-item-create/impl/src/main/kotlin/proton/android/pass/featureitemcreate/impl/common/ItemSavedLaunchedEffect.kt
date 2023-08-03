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

package proton.android.pass.featureitemcreate.impl.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.commonui.api.findActivity
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

@Composable
fun ItemSavedLaunchedEffect(
    isItemSaved: ItemSavedState,
    selectedShareId: ShareId?,
    viewModel: ItemSavedViewModel = hiltViewModel(),
    onSuccess: (ShareId, ItemId, ItemUiModel) -> Unit
) {
    val activity = LocalContext.current.findActivity()
    val shouldRequestReview by viewModel.shouldRequestReview.collectAsStateWithLifecycle()
    if (isItemSaved !is ItemSavedState.Success) return
    selectedShareId ?: return
    LaunchedEffect(Unit) {
        if (shouldRequestReview) {
            viewModel.requestReview(activity.value())
        }
        onSuccess(selectedShareId, isItemSaved.itemId, isItemSaved.item)
    }
}
