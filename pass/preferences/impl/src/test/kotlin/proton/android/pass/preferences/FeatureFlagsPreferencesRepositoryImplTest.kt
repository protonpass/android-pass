/*
 * Copyright (c) 2026 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.preferences

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.featureflag.domain.entity.FeatureId
import org.junit.Test
import proton.android.pass.account.fakes.FakeAccountManager
import proton.android.pass.account.fakes.FakeFeatureFlagRepository
import me.proton.core.featureflag.domain.entity.FeatureFlag as CoreFeatureFlag

class FeatureFlagsPreferencesRepositoryImplTest {

    private val userId = UserId("test-user-id")

    private val remoteFlag = FeatureFlag.PASS_AUTOFILL_HEALTH
    private val remoteFlagKey = requireNotNull(remoteFlag.key)

    @Test
    fun `awaitResolved returns the local override regardless of the remote value`() = runTest {
        val (repository, featureFlagRepository) = buildRepository()
        featureFlagRepository.setFeatureFlag(
            FeatureId(remoteFlagKey),
            CoreFeatureFlag.default(remoteFlagKey, defaultValue = false).copy(value = false)
        )
        repository.set(remoteFlag, true)

        val resolved = repository.awaitResolved(remoteFlag, userId)

        assertThat(resolved).isTrue()
    }

    @Test
    fun `awaitResolved returns the remote value when there is no override`() = runTest {
        val (repository, featureFlagRepository) = buildRepository()
        featureFlagRepository.setFeatureFlag(
            FeatureId(remoteFlagKey),
            CoreFeatureFlag.default(remoteFlagKey, defaultValue = false).copy(value = true)
        )

        val resolved = repository.awaitResolved(remoteFlag, userId)

        assertThat(resolved).isTrue()
    }

    @Test
    fun `awaitResolved falls back to the static default when the remote flag is unresolved`() = runTest {
        val (repository, _) = buildRepository()

        val resolved = repository.awaitResolved(remoteFlag, userId)

        assertThat(resolved).isEqualTo(remoteFlag.isEnabledDefault)
    }

    @Test
    fun `get keeps observing and self-corrects once the remote value resolves`() = runTest {
        val (repository, featureFlagRepository) = buildRepository()
        val correctedValue = !remoteFlag.isEnabledDefault

        repository.get<Boolean>(remoteFlag, userId).test {
            assertThat(awaitItem()).isEqualTo(remoteFlag.isEnabledDefault)

            featureFlagRepository.setFeatureFlag(
                FeatureId(remoteFlagKey),
                CoreFeatureFlag.default(remoteFlagKey, defaultValue = remoteFlag.isEnabledDefault)
                    .copy(value = correctedValue)
            )

            assertThat(awaitItem()).isEqualTo(correctedValue)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `awaitResolved forces a refresh and waits for it, instead of defaulting on a cache miss`() = runTest {
        val pendingFeatureFlagRepository = FakePendingFeatureFlagRepository(loadingValue = null)
        val repository = FeatureFlagsPreferencesRepositoryImpl(
            accountManager = FakeAccountManager(),
            featureFlagManager = pendingFeatureFlagRepository,
            dataStore = FakeFeatureFlagsPreferencesDataStore()
        )

        var resolved: Boolean? = null
        val job = launch { resolved = repository.awaitResolved(remoteFlag, userId) }
        advanceUntilIdle()

        assertThat(resolved).isNull()

        pendingFeatureFlagRepository.resolve(
            CoreFeatureFlag.default(remoteFlagKey, defaultValue = false).copy(value = true)
        )
        advanceUntilIdle()
        job.join()

        assertThat(resolved).isTrue()
    }

    @Test
    fun `awaitResolved ignores the stale value exposed by observe and waits for get instead`() = runTest {
        val staleValue = CoreFeatureFlag.default(remoteFlagKey, defaultValue = false).copy(value = false)
        val pendingFeatureFlagRepository = FakePendingFeatureFlagRepository(loadingValue = staleValue)
        val repository = FeatureFlagsPreferencesRepositoryImpl(
            accountManager = FakeAccountManager(),
            featureFlagManager = pendingFeatureFlagRepository,
            dataStore = FakeFeatureFlagsPreferencesDataStore()
        )

        val job = launch {
            pendingFeatureFlagRepository.resolve(
                CoreFeatureFlag.default(remoteFlagKey, defaultValue = false).copy(value = true)
            )
        }
        val resolved = repository.awaitResolved(remoteFlag, userId)
        job.join()

        assertThat(resolved).isTrue()
    }

    private fun buildRepository(): Pair<FeatureFlagsPreferencesRepositoryImpl, FakeFeatureFlagRepository> {
        val featureFlagRepository = FakeFeatureFlagRepository()
        val repository = FeatureFlagsPreferencesRepositoryImpl(
            accountManager = FakeAccountManager(),
            featureFlagManager = featureFlagRepository,
            dataStore = FakeFeatureFlagsPreferencesDataStore()
        )
        return repository to featureFlagRepository
    }
}
