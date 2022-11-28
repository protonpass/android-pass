package me.proton.pass.presentation.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmall
import me.proton.pass.commonui.api.ThemePreviewProvider
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetItem
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetItemList
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetItemText
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetTitle
import me.proton.pass.presentation.components.common.item.icon.AliasIcon
import me.proton.pass.presentation.components.common.item.icon.LoginIcon
import me.proton.pass.presentation.components.common.item.icon.NoteIcon
import me.proton.pass.presentation.components.common.item.icon.PasswordIcon

@ExperimentalMaterialApi
@Composable
fun FABBottomSheetContents(
    modifier: Modifier = Modifier,
    onCreateLogin: () -> Unit,
    onCreateAlias: () -> Unit,
    onCreateNote: () -> Unit,
    onCreatePassword: () -> Unit
) {
    Column(modifier) {
        BottomSheetTitle(title = R.string.title_new)
        Divider(modifier = Modifier.fillMaxWidth())
        BottomSheetItemList(
            items = listOf(
                createLogin(onCreateLogin),
                createAlias(onCreateAlias),
                createNote(onCreateNote),
                createPassword(onCreatePassword)
            )
        )
    }
}

private fun createLogin(onCreateLogin: () -> Unit): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = { BottomSheetItemText(textId = R.string.action_login) }
    override val subtitle: (@Composable () -> Unit)
        get() = {
            Text(
                text = stringResource(R.string.item_type_login_description),
                style = ProtonTheme.typography.defaultSmall,
                color = ProtonTheme.colors.textWeak
            )
        }
    override val icon: (@Composable () -> Unit)
        get() = { LoginIcon() }
    override val onClick: () -> Unit
        get() = onCreateLogin
}

private fun createAlias(onCreateAlias: () -> Unit): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = { BottomSheetItemText(textId = R.string.action_alias) }
    override val subtitle: (@Composable () -> Unit)
        get() = {
            Text(
                text = stringResource(R.string.item_type_alias_description),
                style = ProtonTheme.typography.defaultSmall,
                color = ProtonTheme.colors.textWeak
            )
        }
    override val icon: (@Composable () -> Unit)
        get() = { AliasIcon() }
    override val onClick: () -> Unit
        get() = onCreateAlias
}

private fun createNote(onCreateNote: () -> Unit): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = { BottomSheetItemText(textId = R.string.action_note) }
    override val subtitle: (@Composable () -> Unit)
        get() = {
            Text(
                text = stringResource(R.string.item_type_note_description),
                style = ProtonTheme.typography.defaultSmall,
                color = ProtonTheme.colors.textWeak
            )
        }
    override val icon: (@Composable () -> Unit)
        get() = { NoteIcon() }
    override val onClick: () -> Unit
        get() = onCreateNote
}

private fun createPassword(onCreatePassword: () -> Unit): BottomSheetItem =
    object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = { BottomSheetItemText(textId = R.string.action_password) }
        override val subtitle: (@Composable () -> Unit)
            get() = {
                Text(
                    text = stringResource(R.string.item_type_password_description),
                    style = ProtonTheme.typography.defaultSmall,
                    color = ProtonTheme.colors.textWeak
                )
            }
        override val icon: (@Composable () -> Unit)
            get() = { PasswordIcon() }
        override val onClick: () -> Unit
            get() = onCreatePassword
    }

@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun FABBottomSheetContentsPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    ProtonTheme(isDark = isDark) {
        Surface {
            FABBottomSheetContents(
                onCreateLogin = {},
                onCreateAlias = {},
                onCreateNote = {},
                onCreatePassword = {}
            )
        }
    }
}
