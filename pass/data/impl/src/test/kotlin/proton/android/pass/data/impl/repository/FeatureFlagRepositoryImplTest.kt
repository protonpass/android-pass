/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.data.impl.repository

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Test
import proton.android.pass.account.fakes.TestAccountManager
import proton.android.pass.common.api.some
import proton.android.pass.data.impl.db.entities.ProtonFeatureFlagEntity
import proton.android.pass.data.impl.fakes.TestLocalFeatureFlagDataSource
import proton.android.pass.data.impl.fakes.TestPassDatabase
import proton.android.pass.data.impl.fakes.TestRemoteFeatureFlagDataSource
import proton.android.pass.data.impl.repositories.FeatureFlagRepositoryImpl
import proton.android.pass.data.impl.responses.FeatureFlagToggle
import proton.android.pass.data.impl.responses.FeatureFlagVariant
import proton.android.pass.test.FixedClock
import kotlin.time.Duration.Companion.minutes

class FeatureFlagRepositoryImplTest {

    private lateinit var instance: FeatureFlagRepositoryImpl

    private lateinit var local: TestLocalFeatureFlagDataSource
    private lateinit var remote: TestRemoteFeatureFlagDataSource
    private lateinit var clock: FixedClock


    @Before
    fun setup() {
        local = TestLocalFeatureFlagDataSource()
        remote = TestRemoteFeatureFlagDataSource()
        clock = FixedClock(Instant.fromEpochSeconds(1))

        instance = FeatureFlagRepositoryImpl(
            local = local,
            remote = remote,
            accountManager = TestAccountManager().apply {
                sendPrimaryUserId(UserId(USER_ID))
            },
            clock = clock,
            database = TestPassDatabase()
        )
    }

    @Test
    fun `can check local feature flag`() = runTest {
        val featureName = "feat1"
        local.emitFeatureFlag(localFeatureFlag(featureName).some())

        val result = instance.isFeatureEnabled(featureName).first()
        assertThat(result).isTrue()
    }

    @Test
    fun `checking missing feature flag returns false`() = runTest {
        val result = instance.isFeatureEnabled("notExists").first()
        assertThat(result).isFalse()
    }

    @Test
    fun `get feature flag does not call remote if refresh is false`() = runTest {
        instance.isFeatureEnabled("feature", refresh = false).first()
        assertThat(remote.hasBeenInvoked()).isFalse()
    }

    @Test
    fun `get feature flag calls remote if refresh is true`() = runTest {
        instance.isFeatureEnabled("feature", refresh = true).first()
        assertThat(remote.hasBeenInvoked()).isTrue()
    }

    @Test
    fun `get feature flag value does not call remote if refresh is false`() = runTest {
        instance.getFeatureValue("feature", refresh = false).first()
        assertThat(remote.hasBeenInvoked()).isFalse()
    }

    @Test
    fun `get feature flag value calls remote if refresh is true`() = runTest {
        instance.getFeatureValue("feature", refresh = true).first()
        assertThat(remote.hasBeenInvoked()).isTrue()
    }

    @Test
    fun `refresh stores all entries if local is empty`() = runTest {
        val feature1 = "feat1"
        val feature2 = "feat2"
        local.emitAllFeatureFlags(emptyList())
        remote.setResult(
            listOf(
                remoteFeatureFlag(feature1),
                remoteFeatureFlag(feature2)
            )
        )
        instance.refresh()

        val localStoreMemory = local.getStoreMemory()

        // First for insert, second for update
        val insertMemory = listOf(
            ProtonFeatureFlagEntity(
                userId = USER_ID,
                name = feature1,
                variant = "{\"name\":\"feat1\",\"enabled\":true}",
                createTime = clock.now().epochSeconds,
                updateTime = clock.now().epochSeconds
            ),
            ProtonFeatureFlagEntity(
                userId = USER_ID,
                name = feature2,
                variant = "{\"name\":\"feat2\",\"enabled\":true}",
                createTime = clock.now().epochSeconds,
                updateTime = clock.now().epochSeconds
            )
        )
        val updateMemory = emptyList<ProtonFeatureFlagEntity>()
        assertThat(localStoreMemory).isEqualTo(listOf(insertMemory, updateMemory))
    }

    @Test
    fun `refresh removes all entries from local that are not present in remote`() = runTest {
        val feature1 = "feat1"
        val feature2 = "feat2"
        val feature3 = "feat3"

        local.emitAllFeatureFlags(listOf(localFeatureFlag(feature1), localFeatureFlag(feature2)))
        remote.setResult(
            listOf(
                remoteFeatureFlag(feature2),
                remoteFeatureFlag(feature3)
            )
        )

        val startTime = clock.now().epochSeconds
        val afterOneMinute = clock.instant.plus(1.minutes)
        clock.updateInstant(afterOneMinute)

        instance.refresh()

        val deleteMemory = local.getDeleteMemory()
        assertThat(deleteMemory.size).isEqualTo(1)
        assertThat(deleteMemory[0].size).isEqualTo(1)
        assertThat(deleteMemory[0][0]).isEqualTo(feature1)

        val insertMemory = local.getStoreMemory()
        assertThat(insertMemory.size).isEqualTo(2)

        val inserted = insertMemory[0]
        val updated = insertMemory[1]
        assertThat(inserted.size).isEqualTo(1)
        assertThat(updated.size).isEqualTo(1)

        assertThat(inserted[0].name).isEqualTo(feature3)
        assertThat(inserted[0].createTime).isEqualTo(afterOneMinute.epochSeconds)
        assertThat(inserted[0].updateTime).isEqualTo(afterOneMinute.epochSeconds)

        assertThat(updated[0].name).isEqualTo(feature2)
        assertThat(updated[0].createTime).isEqualTo(startTime)
        assertThat(updated[0].updateTime).isEqualTo(afterOneMinute.epochSeconds)
    }

    private fun localFeatureFlag(name: String) = ProtonFeatureFlagEntity(
        userId = USER_ID,
        name = name,
        variant = "",
        createTime = clock.now().epochSeconds,
        updateTime = clock.now().epochSeconds
    )

    private fun remoteFeatureFlag(name: String) = FeatureFlagToggle(
        name = name,
        variant = FeatureFlagVariant(
            name = name,
            enabled = true,
            payload = null
        )
    )

    companion object {
        private const val USER_ID = "userid123"
    }
}
