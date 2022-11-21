package me.proton.android.pass.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import me.proton.android.pass.R
import me.proton.pass.domain.ShareId
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetItem
import me.proton.pass.presentation.components.common.item.icon.AliasIcon
import me.proton.pass.presentation.components.common.item.icon.LoginIcon
import me.proton.pass.presentation.components.common.item.icon.NoteIcon
import me.proton.pass.presentation.components.common.item.icon.PasswordIcon

@ExperimentalMaterialApi
@Composable
fun FABBottomSheetContents(
    modifier: Modifier = Modifier,
    state: ModalBottomSheetState,
    shareId: ShareId?,
    navigation: HomeScreenNavigation
) {
    val scope = rememberCoroutineScope()
    Column(modifier) {
        Text(
            text = stringResource(R.string.title_new),
            fontSize = 16.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        Divider(modifier = Modifier.fillMaxWidth())
        BottomSheetItem(
            icon = { LoginIcon() },
            title = R.string.action_login,
            subtitle = me.proton.pass.presentation.R.string.item_type_login_description,
            onItemClick = {
                scope.launch {
                    state.hide()
                    shareId?.let { navigation.toCreateLogin(it) }
                }
            }
        )
        BottomSheetItem(
            icon = { AliasIcon() },
            title = R.string.action_alias,
            subtitle = me.proton.pass.presentation.R.string.item_type_alias_description,
            onItemClick = {
                scope.launch {
                    state.hide()
                    shareId?.let { navigation.toCreateAlias(it) }
                }
            }
        )
        BottomSheetItem(
            icon = { NoteIcon() },
            title = R.string.action_note,
            subtitle = me.proton.pass.presentation.R.string.item_type_note_description,
            onItemClick = {
                scope.launch {
                    state.hide()
                    shareId?.let { navigation.toCreateNote(it) }
                }
            }
        )
        BottomSheetItem(
            icon = { PasswordIcon() },
            title = R.string.action_password,
            subtitle = me.proton.pass.presentation.R.string.item_type_password_description,
            onItemClick = {
                scope.launch {
                    state.hide()
                    shareId?.let { navigation.toCreatePassword(it) }
                }
            }
        )
    }
}
