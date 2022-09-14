package me.proton.android.pass.ui.home

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import me.proton.android.pass.R
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.pass.domain.ShareId

@ExperimentalMaterialApi
@Composable
fun BottomSheetContents(
    state: ModalBottomSheetState,
    shareId: ShareId?,
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
        BottomSheetItem(R.drawable.ic_proton_key, R.string.action_login, onItemClick = {
            scope.launch {
                state.hide()
                if (shareId != null) {
                    navigation.toCreateLogin(shareId)
                }
            }
        })
        BottomSheetItem(R.drawable.ic_proton_alias, R.string.action_alias, onItemClick = {
            scope.launch {
                state.hide()
                if (shareId != null) {
                    navigation.toCreateAlias(shareId)
                }
            }
        })
        BottomSheetItem(R.drawable.ic_proton_note, R.string.action_note, onItemClick = {
            scope.launch {
                state.hide()
                if (shareId != null) {
                    navigation.toCreateNote(shareId)
                }
            }
        })
        BottomSheetItem(R.drawable.ic_proton_arrows_rotate, R.string.action_password, onItemClick = {
            scope.launch {
                state.hide()
                if (shareId != null) {
                    navigation.toCreatePassword(shareId)
                }
            }
        })
    }
}

@Composable
private fun BottomSheetItem(
    @DrawableRes icon: Int,
    @StringRes title: Int,
    onItemClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { onItemClick() })
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(painter = painterResource(icon), contentDescription = stringResource(title))
        Text(
            text = stringResource(title),
            fontSize = 16.sp,
            modifier = Modifier.padding(start = 20.dp),
            fontWeight = FontWeight.W400,
            color = ProtonTheme.colors.textNorm
        )
    }
}
