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

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import me.proton.core.domain.entity.UserId
import me.proton.core.featureflag.domain.entity.FeatureId
import me.proton.core.featureflag.domain.repository.FeatureFlagRepository
import proton.android.pass.account.fakes.FakeFeatureFlagRepository
import me.proton.core.featureflag.domain.entity.FeatureFlag as CoreFeatureFlag

internal class FakePendingFeatureFlagRepository(
    private val loadingValue: CoreFeatureFlag?
) : FeatureFlagRepository by FakeFeatureFlagRepository() {

    private val pendingGet = CompletableDeferred<CoreFeatureFlag?>()

    suspend fun resolve(featureFlag: CoreFeatureFlag?) {
        pendingGet.complete(featureFlag)
    }

    override fun observe(
        userId: UserId?,
        featureId: FeatureId,
        refresh: Boolean
    ): Flow<CoreFeatureFlag?> = flowOf(loadingValue)

    override suspend fun get(
        userId: UserId?,
        featureId: FeatureId,
        refresh: Boolean
    ): CoreFeatureFlag? = if (refresh) pendingGet.await() else null
}
