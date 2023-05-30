package proton.android.pass.featureitemcreate.impl.bottomsheets.customfield

import androidx.activity.compose.BackHandler
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.featureitemcreate.impl.R
import me.proton.core.presentation.compose.R as CoreR

@Composable
fun AddCustomFieldBottomSheet(
    modifier: Modifier = Modifier,
    onNavigate: (AddCustomFieldNavigation) -> Unit
) {
    BackHandler { onNavigate(CustomFieldNavigation.Close) }

    BottomSheetItemList(
        modifier = modifier.bottomSheet(),
        items = listOf(
            textField { onNavigate(AddCustomFieldNavigation.AddText) },
            totpField { onNavigate(AddCustomFieldNavigation.AddTotp) },
            hiddenField { onNavigate(AddCustomFieldNavigation.AddHidden) },
        ).withDividers().toPersistentList()
    )
}

private fun textField(onClick: () -> Unit): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = { BottomSheetItemTitle(text = stringResource(id = R.string.bottomsheet_custom_field_type_text)) }
    override val subtitle: (@Composable () -> Unit)?
        get() = null
    override val leftIcon: (@Composable () -> Unit)
        get() = {
            Icon(
                painter = painterResource(CoreR.drawable.ic_proton_text_align_left),
                contentDescription = stringResource(R.string.bottomsheet_custom_field_type_text_content_description)
            )
        }
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
        get() = {
            Icon(
                painter = painterResource(CoreR.drawable.ic_proton_eye_slash),
                contentDescription = stringResource(R.string.bottomsheet_custom_field_type_hidden_content_description)
            )
        }
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
    override val leftIcon: (@Composable () -> Unit)
        get() = {
            Icon(
                painter = painterResource(CoreR.drawable.ic_proton_lock),
                contentDescription = stringResource(R.string.bottomsheet_custom_field_type_totp_content_description)
            )
        }
    override val endIcon: (@Composable () -> Unit)?
        get() = null
    override val onClick: () -> Unit = onClick
    override val isDivider = false
}

@Preview
@Composable
fun AddCustomFieldBottomSheetPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            AddCustomFieldBottomSheet(onNavigate = {})
        }
    }
}

