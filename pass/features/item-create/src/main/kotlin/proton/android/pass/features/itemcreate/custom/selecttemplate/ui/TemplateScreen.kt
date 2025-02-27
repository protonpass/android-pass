/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.features.itemcreate.custom.selecttemplate.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import proton.android.pass.features.itemcreate.custom.selecttemplate.navigation.SelectTemplateNavigation
import proton.android.pass.features.itemcreate.custom.selecttemplate.presentation.TemplateViewmodel

@Composable
fun TemplateScreen(
    modifier: Modifier = Modifier,
    viewmodel: TemplateViewmodel = hiltViewModel(),
    onNavigate: (SelectTemplateNavigation) -> Unit
) {
    TemplateContent(
        modifier = modifier,
        onEvent = {
            when (it) {
                is TemplateEvent.OnTemplateSelected ->
                    onNavigate(
                        SelectTemplateNavigation.NavigateToCreate(
                            shareId = viewmodel.optionalShareId,
                            templateType = it.templateType
                        )
                    )

                TemplateEvent.OnBackClick ->
                    onNavigate(SelectTemplateNavigation.NavigateBack)
            }
        }
    )
}
