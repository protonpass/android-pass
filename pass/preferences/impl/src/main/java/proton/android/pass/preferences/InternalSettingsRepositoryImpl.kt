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
import com.google.protobuf.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import me.proton.android.pass.preferences.AppUsage
import me.proton.android.pass.preferences.LastItemAutofill
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.some
import proton.android.pass.common.api.toOption
import proton.android.pass.log.api.PassLogger
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("TooManyFunctions")
@Singleton
class InternalSettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<InternalSettings>,
    private val inMemoryPreferences: InMemoryPreferences
) : InternalSettingsRepository {

    override fun setLastUnlockedTime(time: Long): Result<Unit> = setPreference {
        it.setLastUnlockTime(time)
    }

    override fun getLastUnlockedTime(): Flow<Option<Long>> = getPreference {
        if (it.lastUnlockTime == 0L) {
            None
        } else {
            it.lastUnlockTime.some()
        }
    }

    override fun setBootCount(count: Long): Result<Unit> = setPreference {
        it.setBootCount(count)
    }

    override fun getBootCount(): Flow<Option<Long>> = getPreference {
        if (it.bootCount == 0L) {
            None
        } else {
            it.bootCount.some()
        }
    }

    override fun setDeclinedUpdateVersion(versionDeclined: String): Result<Unit> =
        setPreference { it.setDeclinedUpdateVersion(versionDeclined) }

    override fun getDeclinedUpdateVersion(): Flow<String> = getPreference {
        it.declinedUpdateVersion
    }

    override fun setHomeSortingOption(sortingOption: SortingOptionPreference): Result<Unit> =
        setPreference { it.setHomeSortingOption(sortingOption.value()) }

    override fun getHomeSortingOption(): Flow<SortingOptionPreference> = getPreference {
        SortingOptionPreference.fromValue(it.homeSortingOption)
    }

    override fun setHomeFilterOption(filterOption: FilterOptionPreference): Result<Unit> = setPreference { settings ->
        settings.setHomeFilteringOption(filterOption.value)
    }

    override fun getHomeFilterOption(): Flow<FilterOptionPreference> = getPreference { settings ->
        FilterOptionPreference.fromValue(settings.homeFilteringOption)
    }

    override fun setAutofillSortingOption(sortingOption: SortingOptionPreference): Result<Unit> =
        setPreference { it.setAutofillSortingOption(sortingOption.value()) }

    override fun getAutofillSortingOption(): Flow<SortingOptionPreference> = dataStore.data
        .catch { exception -> handleExceptions(exception) }
        .map { settings -> SortingOptionPreference.fromValue(settings.autofillSortingOption) }

    override fun setAutofillFilterOption(filterOption: FilterOptionPreference): Result<Unit> = runCatching {
        inMemoryPreferences.set(FilterOptionPreference::class.java.name, filterOption.value)
    }

    override fun getAutofillFilterOption(): Flow<FilterOptionPreference> =
        inMemoryPreferences.observe<Int>(FilterOptionPreference::class.java.name)
            .map { it?.let { FilterOptionPreference.fromValue(it) } ?: FilterOptionPreference.All }

    override fun setSelectedVault(userId: UserId, selectedVault: SelectedVaultPreference): Result<Unit> =
        setPreference { it.putSelectedVaultPerUser(userId.id, selectedVault.value()) }

    override fun getSelectedVault(userId: UserId): Flow<SelectedVaultPreference> = combine(
        getPreference { it.selectedVaultPerUserMap },
        getPreference { it.selectedVault }
    ) { selectedVaultPerUser, selectedVault ->
        val vault = if (selectedVaultPerUser.isEmpty() && selectedVault.isNotBlank()) {
            setPreference { it.putSelectedVaultPerUser(userId.id, selectedVault) }
            selectedVault
        } else {
            selectedVaultPerUser[userId.id]
        }
        SelectedVaultPreference.fromValue(vault)
    }

    override fun setPinAttemptsCount(count: Int): Result<Unit> = setPreference {
        it.setPinAttempts(count)
    }

    override fun getPinAttemptsCount(): Flow<Int> = getPreference {
        it.pinAttempts
    }

    override fun getMasterPasswordAttemptsCount(userId: UserId): Flow<Int> = getPreference {
        it.getMasterPasswordAttemptsPerUserOrDefault(userId.id, 0)
    }

    override fun clearMasterPasswordAttemptsCount(): Result<Unit> = setPreference {
        it.clearMasterPasswordAttemptsPerUser()
    }

    override fun setMasterPasswordAttemptsCount(userId: UserId, count: Int): Result<Unit> = setPreference {
        it.putMasterPasswordAttemptsPerUser(userId.id, count)
    }

    override fun setItemCreateCount(count: Int): Result<Unit> = setPreference {
        it.setItemCreateCount(count)
    }

    override fun getItemCreateCount(): Flow<Int> = getPreference { it.itemCreateCount }

    override fun setInAppReviewTriggered(value: Boolean): Result<Unit> = setPreference {
        it.setInAppReviewTriggered(value)
    }

    override fun getInAppReviewTriggered(): Flow<Boolean> = getPreference { it.inAppReviewTriggered }

    override fun setAppUsage(appUsageConfig: AppUsageConfig): Result<Unit> = setPreference {
        it.setAppUsage(
            AppUsage.newBuilder(it.appUsage)
                .setDaysAppUsedInARow(appUsageConfig.timesUsed)
                .setLastAppUsageDate(
                    Timestamp.newBuilder()
                        .setSeconds(appUsageConfig.lastDateUsed.epochSeconds)
                        .build()
                )
                .build()
        )
    }

    override fun getAppUsage(): Flow<AppUsageConfig> = getPreference {
        AppUsageConfig(
            it.appUsage.daysAppUsedInARow,
            Instant.fromEpochSeconds(it.appUsage.lastAppUsageDate.seconds)
        )
    }

    override fun setItemAutofillCount(count: Int): Result<Unit> = setPreference {
        it.setItemAutofillCount(count)
    }

    override fun getItemAutofillCount(): Flow<Int> = getPreference { it.itemAutofillCount }

    override fun setDarkWebAliasMessageVisibility(visibility: IsDarkWebAliasMessageDismissedPreference): Result<Unit> =
        setPreference {
            it.setDarkWebAliasMessageDismissed(visibility.value())
        }

    override fun getDarkWebAliasMessageVisibility(): Flow<IsDarkWebAliasMessageDismissedPreference> = getPreference {
        IsDarkWebAliasMessageDismissedPreference.from(it.darkWebAliasMessageDismissed)
    }

    override fun setLastItemAutofill(lastItemAutofillPreference: LastItemAutofillPreference): Result<Unit> =
        setPreference {
            it.setLastItemAutofill(
                LastItemAutofill.newBuilder()
                    .setItemId(lastItemAutofillPreference.itemId)
                    .setShareId(lastItemAutofillPreference.shareId)
                    .setLastAutofillTimestamp(
                        Timestamp.newBuilder()
                            .setSeconds(lastItemAutofillPreference.lastAutofillTimestamp)
                            .build()
                    )
            )
        }

    override fun getLastItemAutofill(): Flow<Option<LastItemAutofillPreference>> = getPreference {
        it.lastItemAutofill.takeIf { item -> item != LastItemAutofill.getDefaultInstance() }
            .toOption()
            .map { item ->
                LastItemAutofillPreference(
                    lastAutofillTimestamp = item.lastAutofillTimestamp.seconds,
                    shareId = item.shareId,
                    itemId = item.itemId
                )
            }
    }

    override fun setHasShownAliasContactsOnboarding(value: Boolean): Result<Unit> =
        setPreference { it.setHasShownAliasContactsOnboarding(value) }

    override fun hasShownAliasContactsOnboarding(): Flow<Boolean> = getPreference { it.hasShownAliasContactsOnboarding }

    override fun setLastTimeUserHasSeenIAM(value: LastTimeUserHasSeenIAMPreference): Result<Unit> = setPreference {
        it.putLastTimeUserHasSeenIam(
            value.userId.id,
            Timestamp.newBuilder()
                .setSeconds(value.timestamp)
                .build()
        )
    }

    override fun getLastTimeUserHasSeenIAM(userId: UserId): Flow<Option<LastTimeUserHasSeenIAMPreference>> =
        getPreference {
            if (it.lastTimeUserHasSeenIamMap.containsKey(userId.id)) {
                it.lastTimeUserHasSeenIamMap[userId.id]?.let { timestamp ->
                    LastTimeUserHasSeenIAMPreference(userId, timestamp.seconds)
                }.toOption()
            } else {
                None
            }
        }

    override fun setDefaultVaultHasBeenCreated(userId: UserId): Result<Unit> = setPreference {
        it.putIsDefaultVaultHasBeenCreated(userId.id, true)
    }

    override fun hasDefaultVaultBeenCreated(userId: UserId): Flow<Boolean> = getPreference {
        if (it.isDefaultVaultHasBeenCreatedMap.containsKey(userId.id)) {
            it.isDefaultVaultHasBeenCreatedMap[userId.id] ?: false
        } else {
            false
        }
    }

    override fun getPersistentUUID(): Flow<UUID> = getPreference { it.persistentUuid }
        .map { preferenceUuid ->
            if (preferenceUuid.isNullOrBlank()) {
                val uuid = UUID.randomUUID()
                setPreference { it.setPersistentUuid(uuid.toString()) }
                uuid
            } else {
                UUID.fromString(preferenceUuid)
            }
        }

    override fun clearSettings(): Result<Unit> = setPreference { it.clear() }

    override fun setHasShownItemInSharedVaultWarning(value: Boolean): Result<Unit> =
        setPreference { it.setHasShownItemInSharedVaultWarning(value) }

    override fun hasShownItemInSharedVaultWarning(): Flow<Boolean> =
        getPreference { it.hasShownItemInSharedVaultWarning }

    override fun setHasShownReloadAppWarning(value: Boolean): Result<Unit> =
        setPreference { it.setHasShownReloadAppWarning(value) }

    override fun hasShownReloadAppWarning(): Flow<Boolean> = getPreference { it.hasShownReloadAppWarning }

    private fun setPreference(mapper: (InternalSettings.Builder) -> InternalSettings.Builder): Result<Unit> =
        runCatching {
            runBlocking(Dispatchers.IO) {
                dataStore.updateData {
                    mapper(it.toBuilder()).build()
                }
            }
            return@runCatching
        }


    private fun <T> getPreference(mapper: (InternalSettings) -> T): Flow<T> = dataStore.data
        .catch { exception -> handleExceptions(exception) }
        .map { settings -> mapper(settings) }

    private fun FlowCollector<InternalSettings>.handleExceptions(exception: Throwable) {
        if (exception is IOException) {
            PassLogger.e("Cannot read preferences.", exception)
            runBlocking { emit(InternalSettings.getDefaultInstance()) }
        } else {
            throw exception
        }
    }

}
