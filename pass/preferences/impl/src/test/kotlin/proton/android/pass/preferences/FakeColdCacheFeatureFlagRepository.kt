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

import me.proton.core.domain.entity.UserId
import me.proton.core.featureflag.domain.entity.FeatureId
import me.proton.core.featureflag.domain.repository.FeatureFlagRepository
import proton.android.pass.account.fakes.FakeFeatureFlagRepository
import me.proton.core.featureflag.domain.entity.FeatureFlag as CoreFeatureFlag

internal class FakeColdCacheFeatureFlagRepository(
    private val remoteFlag: CoreFeatureFlag?,
    startFetched: Boolean = false,
    private val getAllError: Throwable? = null
) : FeatureFlagRepository by FakeFeatureFlagRepository() {

    var getAllInvocations = 0
        private set

    private var isFetched = startFetched

    override fun getValue(userId: UserId?, featureId: FeatureId): Boolean? = if (isFetched) remoteFlag?.value else null

    override suspend fun getAll(userId: UserId?): List<CoreFeatureFlag> {
        getAllInvocations++
        getAllError?.let { throw it }
        isFetched = true
        return listOfNotNull(remoteFlag)
    }
}
