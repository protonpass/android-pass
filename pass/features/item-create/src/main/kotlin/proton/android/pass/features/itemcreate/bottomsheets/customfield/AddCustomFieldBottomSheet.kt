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

package proton.android.pass.features.itemcreate.bottomsheets.customfield

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.composecomponents.impl.icon.Icon
import proton.android.pass.features.itemcreate.R
import proton.android.pass.features.itemcreate.common.CustomFieldPrefix
import me.proton.core.presentation.compose.R as CoreR

@Composable
fun AddCustomFieldBottomSheet(
    modifier: Modifier = Modifier,
    prefix: CustomFieldPrefix,
    viewModel: AddCustomFieldViewModel = hiltViewModel(),
    onNavigate: (AddCustomFieldNavigation, sectionIndex: Option<Int>) -> Unit
) {
    val isCustomItemEnabled by viewModel.isCustomItemEnabled.collectAsStateWithLifecycle()
    AddCustomFieldBottomContent(
        modifier = modifier,
        onNavigate = onNavigate,
        sectionIndex = viewModel.sectionIndex,
        prefix = prefix,
        isCustomItemEnabled = isCustomItemEnabled
    )
}

@Composable
fun AddCustomFieldBottomContent(
    modifier: Modifier = Modifier,
    onNavigate: (AddCustomFieldNavigation, sectionIndex: Option<Int>) -> Unit,
    sectionIndex: Option<Int>,
    prefix: CustomFieldPrefix,
    isCustomItemEnabled: Boolean
) {
    val list = mutableListOf<BottomSheetItem>()
    list.add(textField { onNavigate(AddCustomFieldNavigation.AddText, sectionIndex) })

    val isNotIdentity = prefix != CustomFieldPrefix.CreateIdentity && prefix != CustomFieldPrefix.UpdateIdentity
    if (isNotIdentity || isCustomItemEnabled) {
        list.add(totpField { onNavigate(AddCustomFieldNavigation.AddTotp, sectionIndex) })
    }
    list.add(hiddenField { onNavigate(AddCustomFieldNavigation.AddHidden, sectionIndex) })
    if (isCustomItemEnabled) {
        list.add(dateField { onNavigate(AddCustomFieldNavigation.AddDate, sectionIndex) })
    }
    BottomSheetItemList(
        modifier = modifier.bottomSheet(),
        items = list.withDividers().toPersistentList()
    )
}

private fun textField(onClick: () -> Unit): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = { BottomSheetItemTitle(text = stringResource(id = R.string.bottomsheet_custom_field_type_text)) }
    override val subtitle: (@Composable () -> Unit)?
        get() = null
    override val leftIcon: (@Composable () -> Unit)
        get() = { Icon.Default(CoreR.drawable.ic_proton_text_align_left) }
    override val endIcon: (@Composable () -> Unit)?
        get() = null
    override val onClick: () -> Unit = onClick
    override val isDivider = false
}

private fun hiddenField(onClick: () -> Unit): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = { BottomSheetItemTitle(text = stringResource(id = R.string.bottomsheet_custom_field_type_hidden)) }
    override val subtitle: (@Composable () -> Unit)?
        get() = null
    override val leftIcon: (@Composable () -> Unit)
        get() = { Icon.Default(CoreR.drawable.ic_proton_eye_slash) }
    override val endIcon: (@Composable () -> Unit)?
        get() = null
    override val onClick: () -> Unit = onClick
    override val isDivider = false
}

private fun totpField(onClick: () -> Unit): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = { BottomSheetItemTitle(text = stringResource(id = R.string.bottomsheet_custom_field_type_totp)) }
    override val subtitle: (@Composable () -> Unit)?
        get() = null
    override val leftIcon: @Composable () -> Unit
        get() = { Icon.Default(id = CoreR.drawable.ic_proton_lock) }
    override val endIcon: (@Composable () -> Unit)?
        get() = null
    override val onClick: () -> Unit = onClick
    override val isDivider = false
}

private fun dateField(onClick: () -> Unit): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = { BottomSheetItemTitle(text = stringResource(id = R.string.bottomsheet_custom_field_type_date)) }
    override val subtitle: (@Composable () -> Unit)?
        get() = null
    override val leftIcon: @Composable () -> Unit
        get() = { Icon.Default(id = CoreR.drawable.ic_proton_calendar_today) }
    override val endIcon: (@Composable () -> Unit)?
        get() = null
    override val onClick: () -> Unit = onClick
    override val isDivider = false
}

@Preview
@Composable
fun AddCustomFieldBottomContentPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            AddCustomFieldBottomContent(
                onNavigate = { _, _ -> },
                sectionIndex = None,
                prefix = CustomFieldPrefix.CreateLogin,
                isCustomItemEnabled = true
            )
        }
    }
}

