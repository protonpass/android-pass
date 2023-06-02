package proton.android.pass.composecomponents.impl.bottombar

import androidx.compose.runtime.Stable
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.getOrNull
import proton.android.pass.common.api.map
import proton.pass.domain.Plan
import proton.pass.domain.PlanType

@Stable
enum class AccountType {
    Free,
    Trial,
    Unlimited;

    companion object {
        fun fromPlan(planType: PlanType): AccountType = when (planType) {
            PlanType.Free -> Free
            is PlanType.Paid -> Unlimited
            is PlanType.Trial -> Trial
            is PlanType.Unknown -> Free
        }

        fun fromPlan(result: LoadingResult<Plan>): AccountType = result.map {
            fromPlan(it.planType)
        }.getOrNull() ?: Free
    }
}
