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

package proton.android.pass.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.some
import proton.android.pass.log.api.PassLogger
import java.io.IOException
import javax.inject.Inject

class InternalSettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<InternalSettings>
) : InternalSettingsRepository {

    override suspend fun setLastUnlockedTime(time: Long): Result<Unit> = setPreference {
        it.setLastUnlockTime(time)
    }

    override fun getLastUnlockedTime(): Flow<Option<Long>> = getPreference {
        if (it.lastUnlockTime == 0L) {
            None
        } else {
            it.lastUnlockTime.some()
        }
    }

    override suspend fun setBootCount(count: Long): Result<Unit> = setPreference {
        it.setBootCount(count)
    }

    override fun getBootCount(): Flow<Option<Long>> = getPreference {
        if (it.bootCount == 0L) {
            None
        } else {
            it.bootCount.some()
        }
    }

    override suspend fun setDeclinedUpdateVersion(
        versionDeclined: String
    ): Result<Unit> = setPreference { it.setDeclinedUpdateVersion(versionDeclined) }

    override fun getDeclinedUpdateVersion(): Flow<String> = getPreference {
        it.declinedUpdateVersion
    }

    override suspend fun setHomeSortingOption(
        sortingOption: SortingOptionPreference
    ): Result<Unit> = setPreference { it.setHomeSortingOption(sortingOption.value()) }

    override fun getHomeSortingOption(): Flow<SortingOptionPreference> = getPreference {
        SortingOptionPreference.fromValue(it.homeSortingOption)
    }

    override suspend fun setAutofillSortingOption(
        sortingOption: SortingOptionPreference
    ): Result<Unit> = setPreference { it.setAutofillSortingOption(sortingOption.value()) }

    override fun getAutofillSortingOption(): Flow<SortingOptionPreference> = dataStore.data
        .catch { exception -> handleExceptions(exception) }
        .map { settings -> SortingOptionPreference.fromValue(settings.autofillSortingOption) }

    override suspend fun setSelectedVault(
        selectedVault: SelectedVaultPreference
    ): Result<Unit> = setPreference { it.setSelectedVault(selectedVault.value()) }

    override fun getSelectedVault(): Flow<SelectedVaultPreference> = getPreference {
        SelectedVaultPreference.fromValue(it.selectedVault)
    }

    override suspend fun setPinAttemptsCount(count: Int): Result<Unit> = setPreference {
        it.setPinAttempts(count)
    }

    override fun getPinAttemptsCount(): Flow<Int> = getPreference { it.pinAttempts }

    override fun getMasterPasswordAttemptsCount(): Flow<Int> = getPreference {
        it.masterPasswordAttempts
    }

    override suspend fun setMasterPasswordAttemptsCount(count: Int): Result<Unit> = setPreference {
        it.setMasterPasswordAttempts(count)
    }

    override suspend fun clearSettings(): Result<Unit> = setPreference { it.clear() }

    private suspend fun setPreference(
        mapper: (InternalSettings.Builder) -> InternalSettings.Builder
    ): Result<Unit> = runCatching {
        dataStore.updateData {
            mapper(it.toBuilder()).build()
        }
        return@runCatching
    }

    private fun <T> getPreference(mapper: (InternalSettings) -> T): Flow<T> = dataStore.data
        .catch { exception -> handleExceptions(exception) }
        .map { settings -> mapper(settings) }

    private suspend fun FlowCollector<InternalSettings>.handleExceptions(
        exception: Throwable
    ) {
        if (exception is IOException) {
            PassLogger.e("Cannot read preferences.", exception)
            emit(InternalSettings.getDefaultInstance())
        } else {
            throw exception
        }
    }

}
