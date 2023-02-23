package proton.android.pass.featurehome.impl

import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.persistentListOf
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmall
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.bottomSheetPadding
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.container.Circle
import proton.android.pass.composecomponents.impl.item.icon.AliasIcon
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
    Column(modifier.bottomSheetPadding()) {
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
        get() = {
            Circle(backgroundColor = PassTheme.colors.accentPurpleOpaque) {
                Icon(
                    painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_user),
                    contentDescription = stringResource(R.string.item_type_login_create_content_description),
                    tint = PassTheme.colors.accentPurpleOpaque
                )
            }
        }
    override val onClick: () -> Unit
        get() = onCreateLogin
    override val isDivider = false
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
    override val isDivider = false
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
    override val isDivider = false
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
        override val isDivider = false
    }

@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun FABBottomSheetContentsPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
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
