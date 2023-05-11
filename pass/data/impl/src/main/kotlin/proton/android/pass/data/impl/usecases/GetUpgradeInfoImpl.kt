package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import me.proton.core.payment.domain.PaymentManager
import proton.android.pass.data.api.usecases.GetUpgradeInfo
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.ObserveMFACount
import proton.android.pass.data.api.usecases.ObserveVaultCount
import proton.android.pass.data.api.usecases.UpgradeInfo
import proton.android.pass.data.impl.repositories.PlanRepository
import proton.android.pass.preferences.FeatureFlags
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton.pass.domain.Plan
import proton.pass.domain.PlanType
import javax.inject.Inject

class GetUpgradeInfoImpl @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUser,
    private val observeMFACount: ObserveMFACount,
    private val paymentManager: PaymentManager,
    private val planRepository: PlanRepository,
    private val featureFlagsPreferencesRepository: FeatureFlagsPreferencesRepository,
    private val observeVaultCount: ObserveVaultCount
) : GetUpgradeInfo {
    override fun invoke(forceRefresh: Boolean): Flow<UpgradeInfo> = observeCurrentUser()
        .distinctUntilChanged()
        .flatMapLatest { user ->
            combine(
                planRepository.sendUserAccessAndObservePlan(
                    userId = user.userId,
                    forceRefresh = forceRefresh
                ),
                featureFlagsPreferencesRepository.get<Boolean>(FeatureFlags.IAP_ENABLED),
                flowOf(paymentManager.isUpgradeAvailable()),
                observeMFACount(),
                observeVaultCount(user.userId)
            ) { plan: Plan, iapEnabled, isUpgradeAvailable, mfaCount, vaultCount ->
                val isPaid = plan.planType is PlanType.Paid
                UpgradeInfo(
                    isUpgradeAvailable = iapEnabled && isUpgradeAvailable && !isPaid,
                    plan = plan.copy(
                        vaultLimit = plan.vaultLimit.takeIf { it >= 0 } ?: Int.MAX_VALUE,
                        aliasLimit = plan.aliasLimit.takeIf { it >= 0 } ?: Int.MAX_VALUE,
                        totpLimit = plan.totpLimit.takeIf { it >= 0 } ?: Int.MAX_VALUE
                    ),
                    totalVaults = vaultCount,
                    totalAlias = 0,
                    totalTotp = mfaCount
                )
            }
        }
}
