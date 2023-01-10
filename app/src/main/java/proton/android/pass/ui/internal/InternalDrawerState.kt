package proton.android.pass.ui.internal

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeableDefaults
import androidx.compose.material.SwipeableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable

@OptIn(ExperimentalMaterialApi::class)
class InternalDrawerState(
    initialValue: InternalDrawerValue,
    animationSpec: AnimationSpec<Float> = SwipeableDefaults.AnimationSpec,
    val confirmStateChange: (InternalDrawerValue) -> Boolean = { true }
) : SwipeableState<InternalDrawerValue>(
    initialValue = initialValue,
    animationSpec = animationSpec,
    confirmStateChange = confirmStateChange
) {

    val isOpen: Boolean
        get() = currentValue != InternalDrawerValue.Closed

    suspend fun open() = animateTo(InternalDrawerValue.Open)

    suspend fun close() = animateTo(InternalDrawerValue.Closed)

    companion object {
        fun Saver(
            animationSpec: AnimationSpec<Float>,
            confirmStateChange: (InternalDrawerValue) -> Boolean
        ): Saver<InternalDrawerState, *> = Saver(
            save = { it.currentValue },
            restore = {
                InternalDrawerState(
                    initialValue = it,
                    animationSpec = animationSpec,
                    confirmStateChange = confirmStateChange
                )
            }
        )
    }
}

@Composable
fun rememberInternalDrawerState(
    initialValue: InternalDrawerValue,
    animationSpec: AnimationSpec<Float> = SwipeableDefaults.AnimationSpec,
    confirmStateChange: (InternalDrawerValue) -> Boolean = { true }
): InternalDrawerState {
    return rememberSaveable(saver = InternalDrawerState.Saver(animationSpec, confirmStateChange)) {
        InternalDrawerState(initialValue, animationSpec, confirmStateChange)
    }
}

enum class InternalDrawerValue {
    Open,
    Closed
}
