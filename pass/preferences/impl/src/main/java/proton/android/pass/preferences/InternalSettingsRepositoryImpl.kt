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
    override suspend fun setLastUnlockedTime(time: Long): Result<Unit> = runCatching {
        dataStore.updateData {
            it.toBuilder()
                .setLastUnlockTime(time)
                .build()
        }
        return@runCatching
    }

    override fun getLastUnlockedTime(): Flow<Option<Long>> = dataStore.data
        .catch { exception -> handleExceptions(exception) }
        .map { settings ->
            if (settings.lastUnlockTime == 0L) {
                None
            } else {
                settings.lastUnlockTime.some()
            }
        }

    override suspend fun setBootCount(count: Long): Result<Unit> = runCatching {
        dataStore.updateData {
            it.toBuilder()
                .setBootCount(count)
                .build()
        }
        return@runCatching
    }

    override fun getBootCount(): Flow<Option<Long>> = dataStore.data
        .catch { exception -> handleExceptions(exception) }
        .map { settings ->
            if (settings.bootCount == 0L) {
                None
            } else {
                settings.bootCount.some()
            }
        }

    override suspend fun setDeclinedUpdateVersion(versionDeclined: String): Result<Unit> =
        runCatching {
            dataStore.updateData {
                it.toBuilder()
                    .setDeclinedUpdateVersion(versionDeclined)
                    .build()
            }
            return@runCatching
        }

    override fun getDeclinedUpdateVersion(): Flow<String> = dataStore.data
        .catch { exception -> handleExceptions(exception) }
        .map { settings -> settings.declinedUpdateVersion }

    override suspend fun setHomeSortingOption(sortingOption: SortingOptionPreference): Result<Unit> =
        runCatching {
            dataStore.updateData {
                it.toBuilder()
                    .setHomeSortingOption(sortingOption.value())
                    .build()
            }
            return@runCatching
        }

    override fun getHomeSortingOption(): Flow<SortingOptionPreference> = dataStore.data
        .catch { exception -> handleExceptions(exception) }
        .map { settings -> SortingOptionPreference.fromValue(settings.homeSortingOption) }

    override suspend fun setAutofillSortingOption(sortingOption: SortingOptionPreference): Result<Unit> =
        runCatching {
            dataStore.updateData {
                it.toBuilder()
                    .setAutofillSortingOption(sortingOption.value())
                    .build()
            }
            return@runCatching
        }

    override fun getAutofillSortingOption(): Flow<SortingOptionPreference> = dataStore.data
        .catch { exception -> handleExceptions(exception) }
        .map { settings -> SortingOptionPreference.fromValue(settings.autofillSortingOption) }

    override suspend fun setSelectedVault(selectedVault: SelectedVaultPreference): Result<Unit> =
        runCatching {
            dataStore.updateData {
                it.toBuilder()
                    .setSelectedVault(selectedVault.value())
                    .build()
            }
            return@runCatching
        }

    override fun getSelectedVault(): Flow<SelectedVaultPreference> = dataStore.data
        .catch { exception -> handleExceptions(exception) }
        .map { settings -> SelectedVaultPreference.fromValue(settings.selectedVault) }

    override suspend fun setPinAttemptsCount(count: Int): Result<Unit> = runCatching {
        dataStore.updateData {
            it.toBuilder()
                .setPinAttempts(count)
                .build()
        }
        return@runCatching
    }

    override fun getPinAttemptsCount(): Flow<Int> = dataStore.data
        .catch { exception -> handleExceptions(exception) }
        .map { settings -> settings.pinAttempts }

    override suspend fun clearSettings(): Result<Unit> = runCatching {
        dataStore.updateData {
            it.toBuilder()
                .clear()
                .build()
        }
        return@runCatching
    }

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
