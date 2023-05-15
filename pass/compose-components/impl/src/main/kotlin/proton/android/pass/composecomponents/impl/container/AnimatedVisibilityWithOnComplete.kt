package proton.android.pass.composecomponents.impl.container

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AnimatedVisibilityState(initialState: Boolean) {
    val state = MutableTransitionState(initialState)

    fun toggle() {
        state.targetState = !state.targetState
    }
}

@Composable
fun rememberAnimatedVisibilityState(initialState: Boolean): AnimatedVisibilityState =
    remember {
        AnimatedVisibilityState(
            initialState = initialState,
        )
    }


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedVisibilityWithOnComplete(
    modifier: Modifier = Modifier,
    visibilityState: AnimatedVisibilityState,
    onComplete: () -> Unit,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    val scope = rememberCoroutineScope()
    AnimatedVisibility(
        modifier = modifier,
        exit = fadeOut() + shrinkVertically(),
        visibleState = visibilityState.state,
    ) {
        if (!this.transition.isRunning && !visibilityState.state.targetState) {
            LaunchedEffect(true) {
                scope.launch {
                    delay(DELAY_BEFORE_CALLBACK_MS)
                    onComplete()
                }
            }
        }
        content()
    }
}

private const val DELAY_BEFORE_CALLBACK_MS = 350L
