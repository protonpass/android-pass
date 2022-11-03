package me.proton.pass.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import me.proton.android.pass.ui.shared.TopBarTitleView
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.pass.presentation.R
import me.proton.pass.presentation.uievents.IsButtonEnabled

@Composable
fun SettingsContent(
    modifier: Modifier = Modifier,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    state: SettingsUiState,
    onFingerPrintLockChange: (IsButtonEnabled) -> Unit,
    onDrawerIconClick: () -> Unit
) {
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            ProtonTopAppBar(
                title = {
                    TopBarTitleView(
                        title = stringResource(id = R.string.title_settings)
                    )
                },
                navigationIcon = {
                    Icon(
                        Icons.Default.Menu,
                        modifier = Modifier.clickable { onDrawerIconClick() },
                        contentDescription = null
                    )
                }
            )
        }
    ) { contentPadding ->
        Settings(
            modifier = modifier.padding(contentPadding),
            state = state,
            onFingerPrintLockChange = onFingerPrintLockChange
        )
    }
}
