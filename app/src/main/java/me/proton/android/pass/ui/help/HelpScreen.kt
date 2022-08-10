package me.proton.android.pass.ui.help

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import me.proton.android.pass.R
import me.proton.android.pass.ui.home.HomeScreenNavigation
import me.proton.android.pass.ui.shared.TopBarTitleView
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.pass.presentation.components.common.rememberFlowWithLifecycle
import me.proton.core.pass.presentation.components.navigation.drawer.NavDrawerNavigation
import me.proton.core.pass.presentation.components.navigation.drawer.NavigationDrawer

@ExperimentalMaterialApi
@Composable
fun HelpScreen(
    modifier: Modifier = Modifier,
    navDrawerNavigation: NavDrawerNavigation,
    navigation: HomeScreenNavigation,
    viewModel: HelpViewModel = hiltViewModel()
) {
    val scaffoldState = rememberHelpScaffoldState()
    val isDrawerOpen = with(scaffoldState.scaffoldState.drawerState) {
        isOpen && !isAnimationRunning || isClosed && isAnimationRunning
    }
    LaunchedEffect(isDrawerOpen) {
        navDrawerNavigation.onDrawerStateChanged(isDrawerOpen)
    }

    val viewState by rememberFlowWithLifecycle(flow = viewModel.state)
        .collectAsState(initial = viewModel.initialViewState)
    val drawerGesturesEnabled by scaffoldState.drawerGesturesEnabled
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        scaffoldState = scaffoldState.scaffoldState,
        drawerContent = {
            NavigationDrawer(
                drawerState = scaffoldState.scaffoldState.drawerState,
                viewState = viewState.navigationDrawerViewState,
                navigation = navDrawerNavigation,
            )
        },
        drawerGesturesEnabled = drawerGesturesEnabled,
        topBar = {
            ProtonTopAppBar(
                title = { TopBarTitleView(title = stringResource(id = R.string.title_help)) },
                navigationIcon = {
                    Icon(
                        Icons.Default.Menu,
                        modifier = Modifier.clickable(onClick = {
                            val drawerState = scaffoldState.scaffoldState.drawerState
                            coroutineScope.launch { if (drawerState.isClosed) drawerState.open() else drawerState.close() }
                        }),
                        contentDescription = null,
                    )
                }
            )
        }
    ) { contentPadding ->
        Box(modifier = modifier.padding(contentPadding)) {
            Text(text = "Future Help screen")
        }
    }
}

@Stable
@ExperimentalMaterialApi
data class HelpScaffoldState(
    val scaffoldState: ScaffoldState,
    val drawerGesturesEnabled: MutableState<Boolean>,
)

@Composable
@ExperimentalMaterialApi
fun rememberHelpScaffoldState(
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    drawerGesturesEnabled: MutableState<Boolean> = mutableStateOf(true),
): HelpScaffoldState = remember {
    HelpScaffoldState(
        scaffoldState,
        drawerGesturesEnabled,
    )
}
