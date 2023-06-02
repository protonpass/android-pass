package proton.android.pass.featurehome.impl.onboardingtips

import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf

sealed interface OnBoardingTipsEvent {
    object Unknown : OnBoardingTipsEvent
    object OpenTrialScreen : OnBoardingTipsEvent
}

data class OnBoardingTipsUiState(
    val tipsToShow: ImmutableSet<OnBoardingTipPage> = persistentSetOf(),
    val event: OnBoardingTipsEvent = OnBoardingTipsEvent.Unknown
)
