package proton.android.pass.preferences

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.flowWithLifecycle
import androidx.startup.Initializer
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.onAccountState
import me.proton.core.featureflag.domain.FeatureFlagManager
import me.proton.core.featureflag.domain.entity.FeatureId
import proton.android.pass.commonui.api.PassAppLifecycleProvider

class FeatureFlagsPrefetchInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        val entryPoint: FeatureFlagsPrefetchInitializerEntryPoint =
            EntryPointAccessors.fromApplication(
                context.applicationContext,
                FeatureFlagsPrefetchInitializerEntryPoint::class.java
            )
        val featureFlagManager: FeatureFlagManager = entryPoint.featureFlagManager()
        val accountManager: AccountManager = entryPoint.accountManager()
        val passAppLifecycleProvider: PassAppLifecycleProvider = entryPoint.passAppLifecycleProvider()

        accountManager.onAccountState(AccountState.Ready, initialState = true)
            .flowWithLifecycle(passAppLifecycleProvider.lifecycle, Lifecycle.State.CREATED)
            .onEach { account ->
                val featureFlags: Set<FeatureId> = FeatureFlag.values()
                    .mapNotNull { it.key }
                    .map { FeatureId(it) }
                    .toSet()
                featureFlagManager.prefetch(account.userId, featureFlags)
            }
            .launchIn(passAppLifecycleProvider.lifecycle.coroutineScope)
    }

    override fun dependencies(): List<Class<out Initializer<*>?>> = emptyList()

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface FeatureFlagsPrefetchInitializerEntryPoint {
        fun passAppLifecycleProvider(): PassAppLifecycleProvider
        fun accountManager(): AccountManager
        fun featureFlagManager(): FeatureFlagManager
    }
}
