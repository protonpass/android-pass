package proton.android.pass.featurehome.impl.onboardingtips

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableSet
import proton.android.pass.featurehome.impl.onboardingtips.OnBoardingTipPage.AUTOFILL

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun OnBoardingTipContent(
    modifier: Modifier = Modifier,
    tipsSetToShow: ImmutableSet<OnBoardingTipPage>,
    onClick: (OnBoardingTipPage) -> Unit,
    onDismiss: (OnBoardingTipPage) -> Unit
) {
    AnimatedVisibility(
        visible = tipsSetToShow.isNotEmpty(),
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Box(modifier = modifier.padding(16.dp)) {
            if (tipsSetToShow.contains(AUTOFILL)) {
                val dismissState = rememberDismissState(
                    confirmStateChange = {
                        if (it != DismissValue.Default) {
                            onDismiss(AUTOFILL)
                        }
                        true
                    }
                )
                SwipeToDismiss(state = dismissState, background = {}) {
                    AutofillCard(
                        onClick = { onClick(AUTOFILL) },
                        onDismiss = { onDismiss(AUTOFILL) }
                    )
                }
            }
        }
    }
}
