/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.data.impl.usecases.breach

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getAccounts
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveAllShares
import proton.android.pass.data.api.usecases.ObserveItems
import proton.android.pass.data.api.usecases.breach.SendUserMonitorCredentialsReport
import proton.android.pass.data.impl.remote.RemoteOrganizationReportDataSource
import proton.android.pass.data.impl.util.runConcurrently
import proton.android.pass.domain.ItemFlag
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.Share
import proton.android.pass.domain.ShareSelection
import proton.android.pass.securitycenter.api.passwords.InsecurePasswordChecker
import proton.android.pass.securitycenter.api.passwords.MissingTfaChecker
import proton.android.pass.securitycenter.api.passwords.RepeatedPasswordChecker
import javax.inject.Inject

class SendUserMonitorCredentialsReportImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val getUserPlan: GetUserPlan,
    private val remoteOrganizationReportDataSource: RemoteOrganizationReportDataSource,
    private val repeatedPasswordChecker: RepeatedPasswordChecker,
    private val insecurePasswordChecker: InsecurePasswordChecker,
    private val missing2faChecker: MissingTfaChecker,
    private val observeItems: ObserveItems,
    private val observeAllShares: ObserveAllShares,
    private val dispatchers: AppDispatchers
) : SendUserMonitorCredentialsReport {
    override suspend fun invoke() {
        val accounts = accountManager.getAccounts(AccountState.Ready).firstOrNull().orEmpty()
        val reports = accounts
            .map { account ->
                account.userId to getUserPlan(account.userId).first()
            }
            .filter { (_, plan) -> plan.isBusinessPlan }
            .associate { (userId, _) ->
                val shares = observeAllShares(userId, includeHidden = false)
                    .map { it.filter(Share::isOwner).map(Share::id) }
                    .firstOrNull()
                    .orEmpty()
                val monitoredItems = observeItems(
                    userId = userId,
                    selection = ShareSelection.Shares(shares),
                    filter = ItemTypeFilter.Logins,
                    itemState = ItemState.Active,
                    itemFlags = mapOf(ItemFlag.SkipHealthCheck to false),
                    includeHidden = false
                ).first()
                val excludedItems = observeItems(
                    userId = userId,
                    selection = ShareSelection.Shares(shares),
                    filter = ItemTypeFilter.Logins,
                    itemState = ItemState.Active,
                    itemFlags = mapOf(ItemFlag.SkipHealthCheck to true),
                    includeHidden = false
                ).first()
                val report = withContext(dispatchers.default) {
                    Report(
                        reusedPasswords = repeatedPasswordChecker(monitoredItems).repeatedPasswordsCount,
                        inactive2FA = missing2faChecker(monitoredItems).missing2faCount,
                        excludedItems = excludedItems.count(),
                        weakPasswords = insecurePasswordChecker(monitoredItems).insecurePasswordsCount
                    )
                }
                userId to report
            }
        runConcurrently(
            items = reports.entries,
            block = { (userId, report) ->
                remoteOrganizationReportDataSource.request(
                    userId = userId,
                    reusedPasswords = report.reusedPasswords,
                    inactive2FA = report.inactive2FA,
                    excludedItems = report.excludedItems,
                    weakPasswords = report.weakPasswords
                )
            }
        )
    }
}

private data class Report(
    val reusedPasswords: Int,
    val inactive2FA: Int,
    val excludedItems: Int,
    val weakPasswords: Int
)
