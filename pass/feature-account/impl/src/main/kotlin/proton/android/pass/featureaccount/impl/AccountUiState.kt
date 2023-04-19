package proton.android.pass.featureaccount.impl

import androidx.compose.runtime.Stable
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState

sealed interface PlanSection {

    fun name(): String = ""
    fun isLoading(): Boolean

    object Hide : PlanSection {
        override fun isLoading(): Boolean = false
    }
    object Loading : PlanSection {
        override fun isLoading(): Boolean = true
    }
    data class Data(val planName: String) : PlanSection {
        override fun name(): String = planName
        override fun isLoading(): Boolean = false
    }
}

@Stable
data class AccountUiState(
    val email: String?,
    val plan: PlanSection,
    val isLoadingState: IsLoadingState
) {
    companion object {
        val Initial = AccountUiState(
            email = null,
            plan = PlanSection.Hide,
            isLoadingState = IsLoadingState.Loading
        )
    }
}

