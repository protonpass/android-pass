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

package proton.android.pass.data.fakes.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.data.api.repositories.FeatureFlagRepository
import javax.inject.Inject

class TestFeatureFlagRepository @Inject constructor() : FeatureFlagRepository {

    private val enabledFeatures = mutableMapOf<String, Boolean>()
    private val featureValues = mutableMapOf<String, Option<String>>()

    fun setFeatureEnabled(featureName: String, enabled: Boolean) {
        enabledFeatures[featureName] = enabled
    }

    fun setFeatureValue(featureName: String, value: Option<String>) {
        featureValues[featureName] = value
    }

    override fun isFeatureEnabled(featureName: String, refresh: Boolean): Flow<Boolean> {
        val value = enabledFeatures[featureName] ?: false
        return flowOf(value)
    }

    override fun getFeatureValue(featureName: String, refresh: Boolean): Flow<Option<String>> {
        val value = featureValues[featureName] ?: None
        return flowOf(value)
    }

    override suspend fun refresh(userId: UserId?) {

    }
}
