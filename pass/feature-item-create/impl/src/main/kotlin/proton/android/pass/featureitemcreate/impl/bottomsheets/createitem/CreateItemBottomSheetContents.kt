package proton.android.pass.featureitemcreate.impl.bottomsheets.createitem

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
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
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
import proton.android.pass.featureitemcreate.impl.R
import proton.pass.domain.ShareId

@ExperimentalMaterialApi
@Composable
fun CreateItemBottomSheetContents(
    modifier: Modifier = Modifier,
    shareId: ShareId? = null,
    onCreateLogin: (Option<ShareId>) -> Unit,
    onCreateAlias: (Option<ShareId>) -> Unit,
    onCreateNote: (Option<ShareId>) -> Unit,
    onCreatePassword: () -> Unit
) {
    BottomSheetItemList(
        modifier = modifier.bottomSheetPadding(),
        items = persistentListOf(
            createLogin(shareId, onCreateLogin),
            createAlias(shareId, onCreateAlias),
            createNote(shareId, onCreateNote),
            createPassword(onCreatePassword)
        )
    )
}

private fun createLogin(
    shareId: ShareId?,
    onCreateLogin: (Option<ShareId>) -> Unit
): BottomSheetItem = object : BottomSheetItem {
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
    override val leftIcon: (@Composable () -> Unit)
        get() = {
            Circle(backgroundColor = PassTheme.colors.accentPurpleOpaque) {
                Icon(
                    painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_user),
                    contentDescription = stringResource(R.string.item_type_login_create_content_description),
                    tint = PassTheme.colors.accentPurpleOpaque
                )
            }
        }
    override val endIcon: (@Composable () -> Unit)?
        get() = null
    override val onClick: () -> Unit
        get() = { onCreateLogin(shareId.toOption()) }
    override val isDivider = false
}

private fun createAlias(
    shareId: ShareId?,
    onCreateAlias: (Option<ShareId>) -> Unit
): BottomSheetItem =
    object : BottomSheetItem {
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
        override val leftIcon: (@Composable () -> Unit)
            get() = { AliasIcon() }
        override val endIcon: (@Composable () -> Unit)?
            get() = null
        override val onClick: () -> Unit
            get() = { onCreateAlias(shareId.toOption()) }
        override val isDivider = false
    }

private fun createNote(
    shareId: ShareId?,
    onCreateNote: (Option<ShareId>) -> Unit
): BottomSheetItem =
    object : BottomSheetItem {
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
        override val leftIcon: (@Composable () -> Unit)
            get() = { NoteIcon() }
        override val endIcon: (@Composable () -> Unit)?
            get() = null
        override val onClick: () -> Unit
            get() = { onCreateNote(shareId.toOption()) }
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
        override val leftIcon: (@Composable () -> Unit)
            get() = { PasswordIcon() }
        override val endIcon: (@Composable () -> Unit)?
            get() = null
        override val onClick: () -> Unit
            get() = onCreatePassword
        override val isDivider = false
    }

@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun CreateItemBottomSheetContentsPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            CreateItemBottomSheetContents(
                onCreateLogin = {},
                onCreateAlias = {},
                onCreateNote = {},
                onCreatePassword = {}
            )
        }
    }
}
