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
import me.proton.pass.common.api.Option
import me.proton.pass.domain.ShareId
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetItem

@ExperimentalMaterialApi
@Composable
fun BottomSheetContents(
    state: ModalBottomSheetState,
    shareId: Option<ShareId>,
    navigation: HomeScreenNavigation
) {
    val scope = rememberCoroutineScope()
    Column {
        Text(
            text = stringResource(R.string.title_new),
            fontSize = 16.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        Divider(modifier = Modifier.fillMaxWidth())
        BottomSheetItem(me.proton.core.presentation.R.drawable.ic_proton_key, R.string.action_login, onItemClick = {
            scope.launch {
                state.hide()
                shareId.map { navigation.toCreateLogin(it) }
            }
        })
        BottomSheetItem(me.proton.core.presentation.R.drawable.ic_proton_alias, R.string.action_alias, onItemClick = {
            scope.launch {
                state.hide()
                shareId.map { navigation.toCreateAlias(it) }
            }
        })
        BottomSheetItem(me.proton.core.presentation.R.drawable.ic_proton_note, R.string.action_note, onItemClick = {
            scope.launch {
                state.hide()
                shareId.map { navigation.toCreateNote(it) }
            }
        })
        BottomSheetItem(
            icon = me.proton.core.presentation.R.drawable.ic_proton_arrows_rotate,
            title = R.string.action_password,
            onItemClick = {
                scope.launch {
                    state.hide()
                    shareId.map { navigation.toCreatePassword(it) }
                }
            }
        )
    }
}
