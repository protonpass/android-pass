package me.proton.pass.presentation.navigation.drawer

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.presentation.R
import me.proton.pass.presentation.settings.ThemedBooleanPreviewProvider

@Composable
fun ReportProblemItem(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    closeDrawerAction: () -> Unit,
    onClick: () -> Unit
) {
    NavigationDrawerListItem(
        modifier = modifier,
        title = stringResource(R.string.navigation_item_bug_report),
        icon = me.proton.core.presentation.R.drawable.ic_proton_bug,
        isSelected = isSelected,
        closeDrawerAction = closeDrawerAction,
        onClick = onClick
    )
}

@Preview
@Composable
fun ReportProblemItemPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    ProtonTheme(isDark = input.first) {
        Surface {
            ReportProblemItem(
                isSelected = input.second,
                closeDrawerAction = {},
                onClick = {}
            )
        }
    }
}
