package proton.android.pass.featureprofile.impl

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import proton.android.pass.commonui.api.PassTypography
import proton.android.pass.composecomponents.impl.bottombar.BottomBar
import proton.android.pass.composecomponents.impl.bottombar.BottomBarSelected

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onListClick: () -> Unit,
    onCreateItemClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            ProtonTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.profile_screen_title),
                        style = PassTypography.hero
                    )
                }
            )
        },
        bottomBar = {
            BottomBar(
                bottomBarSelected = BottomBarSelected.Profile,
                onListClick = onListClick,
                onCreateClick = onCreateItemClick,
                onProfileClick = {}
            )
        }
    ) { padding ->
        ProfileContent(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            onAccountClick = { },
            onSettingsClick = onSettingsClick
        )
    }
}

