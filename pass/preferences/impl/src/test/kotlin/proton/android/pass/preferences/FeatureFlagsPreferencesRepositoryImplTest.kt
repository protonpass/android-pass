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
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.featureflag.domain.entity.FeatureId
import org.junit.Test
import proton.android.pass.account.fakes.FakeAccountManager
import proton.android.pass.account.fakes.FakeFeatureFlagRepository
import proton.android.pass.common.fakes.FakeAppDispatchers
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
    fun `awaitResolved refreshes once on a cold cache and returns the freshly fetched value`() = runTest {
        val featureFlagRepository = FakeColdCacheFeatureFlagRepository(
            remoteFlag = CoreFeatureFlag.default(remoteFlagKey, defaultValue = false).copy(value = true)
        )
        val repository = FeatureFlagsPreferencesRepositoryImpl(
            accountManager = FakeAccountManager(),
            featureFlagManager = featureFlagRepository,
            dataStore = FakeFeatureFlagsPreferencesDataStore(),
            appDispatchers = FakeAppDispatchers()
        )

        val resolved = repository.awaitResolved(remoteFlag, userId)

        assertThat(resolved).isTrue()
        assertThat(featureFlagRepository.getAllInvocations).isEqualTo(1)
    }

    @Test
    fun `awaitResolved does not retry a successful fetch for a flag that is genuinely absent`() = runTest {
        val featureFlagRepository = FakeColdCacheFeatureFlagRepository(remoteFlag = null)
        val repository = FeatureFlagsPreferencesRepositoryImpl(
            accountManager = FakeAccountManager(),
            featureFlagManager = featureFlagRepository,
            dataStore = FakeFeatureFlagsPreferencesDataStore(),
            appDispatchers = FakeAppDispatchers()
        )

        val firstResolved = repository.awaitResolved(remoteFlag, userId)
        val secondResolved = repository.awaitResolved(remoteFlag, userId)

        assertThat(firstResolved).isEqualTo(remoteFlag.isEnabledDefault)
        assertThat(secondResolved).isEqualTo(remoteFlag.isEnabledDefault)
        assertThat(featureFlagRepository.getAllInvocations).isEqualTo(1)
    }

    @Test
    fun `awaitResolved falls back to the static default, without throwing, when the refresh fails`() = runTest {
        val featureFlagRepository = FakeColdCacheFeatureFlagRepository(
            remoteFlag = CoreFeatureFlag.default(remoteFlagKey, defaultValue = false).copy(value = true),
            getAllError = IllegalStateException("network error")
        )
        val repository = FeatureFlagsPreferencesRepositoryImpl(
            accountManager = FakeAccountManager(),
            featureFlagManager = featureFlagRepository,
            dataStore = FakeFeatureFlagsPreferencesDataStore(),
            appDispatchers = FakeAppDispatchers()
        )

        val resolved = repository.awaitResolved(remoteFlag, userId)

        assertThat(resolved).isEqualTo(remoteFlag.isEnabledDefault)
    }

    @Test
    fun `awaitResolved does not refresh when the value is already cached`() = runTest {
        val featureFlagRepository = FakeColdCacheFeatureFlagRepository(
            remoteFlag = CoreFeatureFlag.default(remoteFlagKey, defaultValue = false).copy(value = true),
            startFetched = true
        )
        val repository = FeatureFlagsPreferencesRepositoryImpl(
            accountManager = FakeAccountManager(),
            featureFlagManager = featureFlagRepository,
            dataStore = FakeFeatureFlagsPreferencesDataStore(),
            appDispatchers = FakeAppDispatchers()
        )

        val resolved = repository.awaitResolved(remoteFlag, userId)

        assertThat(resolved).isTrue()
        assertThat(featureFlagRepository.getAllInvocations).isEqualTo(0)
    }

    private fun buildRepository(): Pair<FeatureFlagsPreferencesRepositoryImpl, FakeFeatureFlagRepository> {
        val featureFlagRepository = FakeFeatureFlagRepository()
        val repository = FeatureFlagsPreferencesRepositoryImpl(
            accountManager = FakeAccountManager(),
            featureFlagManager = featureFlagRepository,
            dataStore = FakeFeatureFlagsPreferencesDataStore(),
            appDispatchers = FakeAppDispatchers()
        )
        return repository to featureFlagRepository
    }
}
