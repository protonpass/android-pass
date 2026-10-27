/*
 * Copyright (c) 2023-2026 Proton AG
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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import proton.android.pass.domain.UserId
import proton.android.pass.preferences.FeatureFlag.AUTOFILL_DEBUG_MODE
import proton.android.pass.preferences.FeatureFlag.RENAME_ADMIN_TO_MANAGER
import proton.android.pass.preferences.FeatureFlag.ENABLE_PAGINATION
import proton.android.pass.preferences.FeatureFlag.PASS_EXPLORE_TAB
import proton.android.pass.preferences.FeatureFlag.PASS_PASSWORD_CHECKS
import proton.android.pass.preferences.FeatureFlag.PASS_MONITOR_PER_CHECK_EXCLUSION
import proton.android.pass.preferences.FeatureFlag.PASS_OFFLINE_ATTACHMENTS
import proton.android.pass.preferences.FeatureFlag.PASS_FOLDER_NAVIGATION
import proton.android.pass.preferences.FeatureFlag.PASS_FORCE_SYNC_FOLDERS
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeFeatureFlagsPreferenceRepository @Inject constructor() :
    FeatureFlagsPreferencesRepository {

    private val state: MutableStateFlow<MutableMap<FeatureFlag, Any?>> =
        MutableStateFlow(mutableMapOf())

    val awaitResolvedInvocations: MutableList<FeatureFlag> = mutableListOf()
    val refreshRemoteInvocations: MutableList<UserId> = mutableListOf()

    @Suppress("UNCHECKED_CAST")
    override fun <T> get(featureFlag: FeatureFlag): Flow<T> = state.map {
        when (featureFlag) {
            AUTOFILL_DEBUG_MODE -> it.getOrDefault(AUTOFILL_DEBUG_MODE, false) as T
            RENAME_ADMIN_TO_MANAGER -> it.getOrDefault(RENAME_ADMIN_TO_MANAGER, false) as T
            ENABLE_PAGINATION -> it.getOrDefault(ENABLE_PAGINATION, false) as T
            PASS_EXPLORE_TAB -> it.getOrDefault(PASS_EXPLORE_TAB, false) as T
            PASS_PASSWORD_CHECKS -> it.getOrDefault(PASS_PASSWORD_CHECKS, false) as T
            PASS_MONITOR_PER_CHECK_EXCLUSION ->
                it.getOrDefault(PASS_MONITOR_PER_CHECK_EXCLUSION, false) as T
            PASS_OFFLINE_ATTACHMENTS -> it.getOrDefault(PASS_OFFLINE_ATTACHMENTS, false) as T
            PASS_FORCE_SYNC_FOLDERS -> it.getOrDefault(PASS_FORCE_SYNC_FOLDERS, false) as T
            PASS_FOLDER_NAVIGATION -> it.getOrDefault(PASS_FOLDER_NAVIGATION, false) as T
        }
    }

    override fun <T> get(featureFlag: FeatureFlag, userId: UserId): Flow<T> = get(featureFlag)

    override suspend fun awaitResolved(featureFlag: FeatureFlag, userId: UserId): Boolean {
        awaitResolvedInvocations.add(featureFlag)
        return state.value[featureFlag] as? Boolean ?: featureFlag.isEnabledDefault
    }

    override suspend fun refreshRemote(userId: UserId) {
        refreshRemoteInvocations.add(userId)
    }

    override fun <T> set(featureFlag: FeatureFlag, value: T?): Result<Unit> {
        state.update {
            it[featureFlag] = value
            it
        }
        return Result.success(Unit)
    }

    override fun observeForAllUsers(featureFlag: FeatureFlag): Flow<Boolean> = flowOf(false)

}
