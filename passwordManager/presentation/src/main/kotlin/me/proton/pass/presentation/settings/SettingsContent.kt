package me.proton.pass.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import me.proton.android.pass.preferences.ThemePreference
import me.proton.android.pass.ui.shared.TopBarTitleView
import me.proton.core.compose.component.ProtonModalBottomSheetLayout
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.pass.presentation.R
import me.proton.pass.presentation.uievents.IsButtonEnabled

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SettingsContent(
    modifier: Modifier = Modifier,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    state: SettingsUiState,
    onThemeChange: (ThemePreference) -> Unit,
    onFingerPrintLockChange: (IsButtonEnabled) -> Unit,
    onDrawerIconClick: () -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()

    ProtonModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetContent = {
            ThemeSelectionBottomSheet(
                onThemeSelected = { theme ->
                    scope.launch {
                        bottomSheetState.hide()
                        onThemeChange(theme)
                    }
                }
            )
        }
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
                onOpenThemeSelection = { scope.launch { bottomSheetState.show() } },
                onFingerPrintLockChange = onFingerPrintLockChange
            )
        }
    }
}
