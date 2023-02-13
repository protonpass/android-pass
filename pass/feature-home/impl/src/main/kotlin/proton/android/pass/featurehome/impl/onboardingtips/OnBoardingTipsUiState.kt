package proton.android.pass.featurehome.impl.onboardingtips

import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf

data class OnBoardingTipsUiState(
    val tipsToShow: ImmutableSet<OnBoardingTipPage> = persistentSetOf()
)
