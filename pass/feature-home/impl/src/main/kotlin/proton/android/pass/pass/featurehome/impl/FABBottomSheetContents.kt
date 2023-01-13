package proton.android.pass.pass.featurehome.impl

import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.persistentListOf
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmall
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.item.icon.AliasIcon
import proton.android.pass.composecomponents.impl.item.icon.LoginIcon
import proton.android.pass.composecomponents.impl.item.icon.NoteIcon
import proton.android.pass.composecomponents.impl.item.icon.PasswordIcon

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
        BottomSheetItemList(
            items = persistentListOf(
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
        get() = { BottomSheetItemTitle(text = stringResource(id = R.string.action_login)) }
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
        get() = { BottomSheetItemTitle(text = stringResource(id = R.string.action_alias)) }
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
        get() = { BottomSheetItemTitle(text = stringResource(id = R.string.action_note)) }
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
            get() = { BottomSheetItemTitle(text = stringResource(id = R.string.action_password)) }
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
