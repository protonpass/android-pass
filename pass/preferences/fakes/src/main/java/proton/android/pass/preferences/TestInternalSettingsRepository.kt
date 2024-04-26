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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("TooManyFunctions")
@Singleton
class TestInternalSettingsRepository @Inject constructor() : InternalSettingsRepository {

    private val bootCountFlow = MutableStateFlow<Option<Long>>(None)
    private val lastUnlockedTimeFlow = MutableStateFlow<Option<Long>>(None)
    private val declinedUpdateVersionFlow = MutableStateFlow("")
    private val homeSortingFlow = MutableStateFlow(SortingOptionPreference.MostRecent)
    private val autofillSortingFlow = MutableStateFlow(SortingOptionPreference.MostRecent)
    private val selectedVaultFlow =
        MutableStateFlow<SelectedVaultPreference>(SelectedVaultPreference.AllVaults)
    private val pinAttemptsCountFlow = MutableStateFlow(0)
    private val masterPasswordAttemptsCountFlow = MutableStateFlow(0)
    private val itemCreateCountFlow = MutableStateFlow(0)
    private val itemAutofillCountFlow = MutableStateFlow(0)
    private val appUsageFlow =
        MutableStateFlow(AppUsageConfig(timesUsed = 0, lastDateUsed = Clock.System.now()))
    private val inAppReviewTriggeredFlow = MutableStateFlow(false)
    private val homeFilterOptionFlow = MutableStateFlow(FilterOptionPreference.All)
    private val autofillFilterOptionFlow = MutableStateFlow(FilterOptionPreference.All)
    private val isDarkWebAliasMessageFlow =
        MutableStateFlow(IsDarkWebAliasMessageDismissedPreference.Show)

    override fun setLastUnlockedTime(time: Long): Result<Unit> {
        lastUnlockedTimeFlow.update { Some(time) }
        return Result.success(Unit)
    }

    override fun getLastUnlockedTime(): Flow<Option<Long>> = lastUnlockedTimeFlow

    override fun setBootCount(count: Long): Result<Unit> {
        bootCountFlow.update { Some(count) }
        return Result.success(Unit)
    }

    override fun getBootCount(): Flow<Option<Long>> = bootCountFlow

    override fun setDeclinedUpdateVersion(versionDeclined: String): Result<Unit> {
        declinedUpdateVersionFlow.update { versionDeclined }
        return Result.success(Unit)
    }

    override fun getDeclinedUpdateVersion(): Flow<String> = declinedUpdateVersionFlow

    override fun setHomeSortingOption(sortingOption: SortingOptionPreference): Result<Unit> {
        homeSortingFlow.update { sortingOption }
        return Result.success(Unit)
    }

    override fun getHomeSortingOption(): Flow<SortingOptionPreference> = homeSortingFlow

    override fun setHomeFilterOption(filterOption: FilterOptionPreference): Result<Unit> {
        homeFilterOptionFlow.update { filterOption }
        return Result.success(Unit)
    }

    override fun getHomeFilterOption(): Flow<FilterOptionPreference> = homeFilterOptionFlow

    override fun setAutofillSortingOption(sortingOption: SortingOptionPreference): Result<Unit> {
        autofillSortingFlow.update { sortingOption }
        return Result.success(Unit)
    }

    override fun getAutofillSortingOption(): Flow<SortingOptionPreference> = autofillSortingFlow

    override fun setAutofillFilterOption(filterOption: FilterOptionPreference): Result<Unit> {
        autofillFilterOptionFlow.update { filterOption }
        return Result.success(Unit)
    }

    override fun getAutofillFilterOption(): Flow<FilterOptionPreference> = autofillFilterOptionFlow

    override fun setSelectedVault(selectedVault: SelectedVaultPreference): Result<Unit> {
        selectedVaultFlow.update { selectedVault }
        return Result.success(Unit)
    }

    override fun getSelectedVault(): Flow<SelectedVaultPreference> = selectedVaultFlow

    override fun setPinAttemptsCount(count: Int): Result<Unit> {
        pinAttemptsCountFlow.update { count }
        return Result.success(Unit)
    }

    override fun getPinAttemptsCount(): Flow<Int> = pinAttemptsCountFlow

    override fun setMasterPasswordAttemptsCount(count: Int): Result<Unit> {
        masterPasswordAttemptsCountFlow.update { count }
        return Result.success(Unit)
    }

    override fun getMasterPasswordAttemptsCount(): Flow<Int> = masterPasswordAttemptsCountFlow

    override fun setItemCreateCount(count: Int): Result<Unit> {
        itemCreateCountFlow.update { count }
        return Result.success(Unit)
    }

    override fun getItemCreateCount(): Flow<Int> = itemCreateCountFlow

    override fun setInAppReviewTriggered(value: Boolean): Result<Unit> {
        inAppReviewTriggeredFlow.update { value }
        return Result.success(Unit)
    }

    override fun getInAppReviewTriggered(): Flow<Boolean> = inAppReviewTriggeredFlow

    override fun setAppUsage(appUsageConfig: AppUsageConfig): Result<Unit> {
        appUsageFlow.update { appUsageConfig }
        return Result.success(Unit)
    }

    override fun getAppUsage(): Flow<AppUsageConfig> = appUsageFlow

    override fun setItemAutofillCount(count: Int): Result<Unit> {
        itemAutofillCountFlow.update { count }
        return Result.success(Unit)
    }

    override fun getItemAutofillCount(): Flow<Int> = itemAutofillCountFlow

    override fun setDarkWebAliasMessageVisibility(visibility: IsDarkWebAliasMessageDismissedPreference): Result<Unit> {
        isDarkWebAliasMessageFlow.update { visibility }
        return Result.success(Unit)
    }

    override fun getDarkWebAliasMessageVisibility(): Flow<IsDarkWebAliasMessageDismissedPreference> =
        isDarkWebAliasMessageFlow

    override fun clearSettings(): Result<Unit> = Result.success(Unit)
}
