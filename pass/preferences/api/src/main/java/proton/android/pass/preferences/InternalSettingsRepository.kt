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
import proton.android.pass.common.api.Option

@Suppress("TooManyFunctions", "ComplexInterface")
interface InternalSettingsRepository {

    fun setLastUnlockedTime(time: Long): Result<Unit>
    fun getLastUnlockedTime(): Flow<Option<Long>>

    fun setBootCount(count: Long): Result<Unit>
    fun getBootCount(): Flow<Option<Long>>

    fun setDeclinedUpdateVersion(versionDeclined: String): Result<Unit>
    fun getDeclinedUpdateVersion(): Flow<String>

    fun setHomeSortingOption(sortingOption: SortingOptionPreference): Result<Unit>
    fun getHomeSortingOption(): Flow<SortingOptionPreference>

    fun setHomeFilterOption(filterOption: FilterOptionPreference): Result<Unit>
    fun getHomeFilterOption(): Flow<FilterOptionPreference>

    fun setAutofillSortingOption(sortingOption: SortingOptionPreference): Result<Unit>
    fun getAutofillSortingOption(): Flow<SortingOptionPreference>

    fun setAutofillFilterOption(filterOption: FilterOptionPreference): Result<Unit>
    fun getAutofillFilterOption(): Flow<FilterOptionPreference>

    fun setSelectedVault(selectedVault: SelectedVaultPreference): Result<Unit>
    fun getSelectedVault(): Flow<SelectedVaultPreference>

    fun setPinAttemptsCount(count: Int): Result<Unit>
    fun getPinAttemptsCount(): Flow<Int>

    fun setMasterPasswordAttemptsCount(count: Int): Result<Unit>
    fun getMasterPasswordAttemptsCount(): Flow<Int>

    fun setItemCreateCount(count: Int): Result<Unit>
    fun getItemCreateCount(): Flow<Int>

    fun setInAppReviewTriggered(value: Boolean): Result<Unit>
    fun getInAppReviewTriggered(): Flow<Boolean>

    fun setAppUsage(appUsageConfig: AppUsageConfig): Result<Unit>
    fun getAppUsage(): Flow<AppUsageConfig>

    fun setItemAutofillCount(count: Int): Result<Unit>
    fun getItemAutofillCount(): Flow<Int>

    fun setDarkWebAliasMessageVisibility(visibility: IsDarkWebAliasMessageDismissedPreference): Result<Unit>
    fun getDarkWebAliasMessageVisibility(): Flow<IsDarkWebAliasMessageDismissedPreference>

    fun setLastItemAutofill(lastItemAutofillPreference: LastItemAutofillPreference): Result<Unit>
    fun getLastItemAutofill(): Flow<Option<LastItemAutofillPreference>>

    fun clearSettings(): Result<Unit>
}
