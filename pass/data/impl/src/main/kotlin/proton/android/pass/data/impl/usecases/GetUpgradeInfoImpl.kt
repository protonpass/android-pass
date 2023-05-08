package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import me.proton.core.payment.domain.PaymentManager
import proton.android.pass.common.api.combine as combineN
import proton.android.pass.data.api.usecases.GetUpgradeInfo
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.ObserveMFACount
import proton.android.pass.data.api.usecases.ObserveVaultCount
import proton.android.pass.data.api.usecases.UpgradeInfo
import proton.android.pass.data.api.usecases.UserPlan
import proton.android.pass.data.impl.local.LocalPlanLimitsDataSource
import proton.android.pass.preferences.FeatureFlags
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import javax.inject.Inject

class GetUpgradeInfoImpl @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUser,
    private val observeMFACount: ObserveMFACount,
    private val getUserPlan: GetUserPlan,
    private val paymentManager: PaymentManager,
    private val localPlanLimitsDataSource: LocalPlanLimitsDataSource,
    private val featureFlagsPreferencesRepository: FeatureFlagsPreferencesRepository,
    private val observeVaultCount: ObserveVaultCount
) : GetUpgradeInfo {
    override fun invoke(): Flow<UpgradeInfo> = observeCurrentUser()
        .distinctUntilChanged()
        .flatMapLatest { user ->
            combineN(
                getUserPlan(user.userId),
                localPlanLimitsDataSource.observePlanLimits(user.userId),
                featureFlagsPreferencesRepository.get<Boolean>(FeatureFlags.IAP_ENABLED),
                flowOf(paymentManager.isUpgradeAvailable()),
                observeMFACount(),
                observeVaultCount(user.userId)
            ) { plan, planLimits, iapEnabled, isUpgradeAvailable, mfaCount, vaultCount ->
                val isPaid = plan is UserPlan.Paid
                UpgradeInfo(
                    isUpgradeAvailable = iapEnabled && isUpgradeAvailable && !isPaid,
                    totalVaults = vaultCount,
                    vaultLimit = planLimits.vaultLimit.takeIf { it >= 0 } ?: Int.MAX_VALUE,
                    totalAlias = 0,
                    aliasLimit = planLimits.aliasLimit.takeIf { it >= 0 } ?: Int.MAX_VALUE,
                    totalTotp = mfaCount,
                    totpLimit = planLimits.totpLimit.takeIf { it >= 0 } ?: Int.MAX_VALUE
                )
            }
        }
}
