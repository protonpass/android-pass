/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.securitycenter.impl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.data.api.usecases.items.ObserveMonitoredItems
import proton.android.pass.securitycenter.api.InsecurePasswordsResult
import proton.android.pass.securitycenter.api.Missing2faResult
import proton.android.pass.securitycenter.api.ObserveSecurityAnalysis
import proton.android.pass.securitycenter.api.ReusedPasswordsResult
import proton.android.pass.securitycenter.api.SecurityAnalysis
import proton.android.pass.securitycenter.api.checkers.BreachedDataChecker
import proton.android.pass.securitycenter.api.passwords.InsecurePasswordChecker
import proton.android.pass.securitycenter.api.passwords.MissingTfaChecker
import proton.android.pass.securitycenter.api.passwords.RepeatedPasswordChecker
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObserveSecurityAnalysisImpl @Inject constructor(
    private val breachedDataChecker: BreachedDataChecker,
    private val repeatedPasswordChecker: RepeatedPasswordChecker,
    private val insecurePasswordChecker: InsecurePasswordChecker,
    private val missing2faChecker: MissingTfaChecker,
    observeMonitoredItems: ObserveMonitoredItems,
    dispatchers: AppDispatchers
) : ObserveSecurityAnalysis {

    private val coroutineScope: CoroutineScope = CoroutineScope(dispatchers.default)

    private val securityAnalysisFlow: SharedFlow<SecurityAnalysis> =
        observeMonitoredItems(includeHiddenVaults = false)
            .flatMapLatest { items ->
                combine(
                    oneShot { breachedDataChecker(items) }.asLoadingResult(),
                    oneShot { repeatedPasswordChecker(items) }.map {
                        ReusedPasswordsResult(it.repeatedPasswordsCount)
                    }.asLoadingResult(),
                    oneShot { insecurePasswordChecker(items) }.map {
                        InsecurePasswordsResult(it.insecurePasswordsCount)
                    }.asLoadingResult(),
                    oneShot { missing2faChecker(items) }.map {
                        Missing2faResult(it.missing2faCount)
                    }.asLoadingResult(),
                    ::SecurityAnalysis
                )
            }
            .onStart {
                emit(
                    SecurityAnalysis(
                        breachedData = LoadingResult.Loading,
                        reusedPasswords = LoadingResult.Loading,
                        insecurePasswords = LoadingResult.Loading,
                        missing2fa = LoadingResult.Loading
                    )
                )
            }
            .shareIn(
                scope = coroutineScope,
                started = SharingStarted.Lazily,
                replay = 1
            )

    override fun invoke(): Flow<SecurityAnalysis> = securityAnalysisFlow.distinctUntilChanged()

}
