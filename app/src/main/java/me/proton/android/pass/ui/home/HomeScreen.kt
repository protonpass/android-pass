package me.proton.android.pass.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.insets.systemBarsPadding
import me.proton.core.domain.entity.UserId
import me.proton.core.pass.presentation.components.navigation.drawer.NavigationDrawer
import me.proton.core.pass.presentation.components.common.rememberFlowWithLifecycle
import me.proton.core.pass.presentation.components.navigation.drawer.NavigationDrawerViewEvent

@ExperimentalMaterialApi
object HomeScreen {
    operator fun invoke(userId: UserId) = "home/${userId.id}"
    const val route = "home/{userId}"
    const val userId = "userId"

    @Composable
    fun view(
        userId: UserId,
        modifier: Modifier = Modifier,
        onDrawerStateChanged: (Boolean) -> Unit,
        viewModel: HomeViewModel = hiltViewModel(),
    ) {
        val homeScaffoldState = rememberHomeScaffoldState()
        val isDrawerOpen = with(homeScaffoldState.scaffoldState.drawerState) {
            (isOpen && !isAnimationRunning) || (isClosed && isAnimationRunning)
        }
        LaunchedEffect(isDrawerOpen) {
            onDrawerStateChanged(isDrawerOpen)
        }
        val drawerGesturesEnabled by homeScaffoldState.drawerGesturesEnabled

        val homeViewModel = remember { viewModel }
        val viewState by rememberFlowWithLifecycle(flow = homeViewModel.viewState)
            .collectAsState(initial = homeViewModel.initialViewState)

        val viewEvent = homeViewModel.viewEvent(
            navigateToSigningOut = { },
        )

        Scaffold(
            modifier = modifier,
            drawerContent = {
                NavigationDrawer(
                    drawerState = homeScaffoldState.scaffoldState.drawerState,
                    viewState = viewState.navigationDrawerViewState,
                    viewEvent = viewEvent.navigationDrawerViewEvent,
                    modifier = Modifier
                        .statusBarsPadding()
                        .navigationBarsPadding(),
                )
            },
            drawerGesturesEnabled = drawerGesturesEnabled,
//            drawerScrimColor = MaterialTheme.colors.drawerScrim,
        ) {
                contentPadding ->
            Box(
                Modifier
                    .padding(contentPadding)
                    .systemBarsPadding()
            ) {
                Home()
            }
        }
    }
}

@Composable
internal fun Home() {

}

@Stable
@ExperimentalMaterialApi
data class HomeScaffoldState(
    val scaffoldState: ScaffoldState,
    val drawerGesturesEnabled: MutableState<Boolean>,
)

@Composable
@ExperimentalMaterialApi
fun rememberHomeScaffoldState(
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    drawerGesturesEnabled: MutableState<Boolean> = mutableStateOf(true),
): HomeScaffoldState = remember {
    HomeScaffoldState(
        scaffoldState,
        drawerGesturesEnabled,
    )
}

interface HomeViewEvent {
    val navigationDrawerViewEvent: NavigationDrawerViewEvent
}