package proton.android.pass.featuretrial.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.topbar.iconbutton.CrossBackIconButton

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TrialScreenContent(
    modifier: Modifier = Modifier,
    state: TrialUiState,
    onNavigate: (TrialNavigation) -> Unit,
    onLearnMore: () -> Unit
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(PassTheme.colors.backgroundStrong),
        topBar = {
            ProtonTopAppBar(
                backgroundColor = PassTheme.colors.itemDetailBackground,
                title = { },
                navigationIcon = {
                    CrossBackIconButton {
                        onNavigate(TrialNavigation.Close)
                    }
                }
            )
        }
    ) { padding ->
        TrialContent(
            modifier = Modifier.padding(padding),
            state = state,
            onNavigate = onNavigate,
            onLearnMore = onLearnMore
        )
    }
}
