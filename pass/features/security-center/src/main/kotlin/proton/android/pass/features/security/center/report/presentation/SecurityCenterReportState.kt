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

package proton.android.pass.features.security.center.report.presentation

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.domain.ShareIcon
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault
import proton.android.pass.domain.breach.BreachEmail
import proton.android.pass.domain.breach.BreachEmailId
import proton.android.pass.domain.breach.BreachEmailReport

@Stable
internal sealed interface SecurityCenterReportEvent {

    @Stable
    data object Idle : SecurityCenterReportEvent

    @Stable
    data object OnEmailBreachesResolved : SecurityCenterReportEvent

    @Stable
    data object OnResolveEmailBreaches : SecurityCenterReportEvent

    @Stable
    data object OnResolveEmailBreachesCancelled : SecurityCenterReportEvent

    @Stable
    data object OnResolveEmailBreachesConfirmed : SecurityCenterReportEvent

}

@Stable
internal data class SecurityCenterReportState(
    internal val canLoadExternalImages: Boolean,
    private val breachEmailResult: LoadingResult<BreachEmailReport>,
    private val breachEmailsResult: LoadingResult<List<BreachEmail>>,
    private val usedInLoginItemsResult: LoadingResult<List<ItemUiModel>>,
    internal val breachEmailId: BreachEmailId?,
    internal val event: SecurityCenterReportEvent,
    private val groupedVaults: Map<ShareId, Vault>
) {

    internal val isBreachExcludedFromMonitoring: Boolean = when (breachEmailResult) {
        is LoadingResult.Error,
        LoadingResult.Loading -> false

        is LoadingResult.Success -> breachEmailResult.data.isMonitoringDisabled
    }

    internal val breachCount: Int = when (breachEmailResult) {
        is LoadingResult.Error,
        LoadingResult.Loading -> 0

        is LoadingResult.Success -> breachEmailResult.data.breachCount
    }

    internal val breachEmail: String = when (breachEmailResult) {
        is LoadingResult.Error,
        LoadingResult.Loading -> ""

        is LoadingResult.Success -> breachEmailResult.data.email
    }

    private val breachEmails: List<BreachEmail> = when (breachEmailsResult) {
        is LoadingResult.Error,
        LoadingResult.Loading -> emptyList()

        is LoadingResult.Success -> breachEmailsResult.data
    }

    internal val resolvedBreachEmails: ImmutableList<BreachEmail> = breachEmails
        .filter { breachEmail -> breachEmail.isResolved }
        .toPersistentList()

    internal val hasResolvedBreaches: Boolean = resolvedBreachEmails.isNotEmpty()

    internal val unresolvedBreachEmails: ImmutableList<BreachEmail> = breachEmails
        .filter { breachEmail -> !breachEmail.isResolved }
        .toPersistentList()

    internal val unresolvedBreachesEmailId: BreachEmailId? = unresolvedBreachEmails.firstOrNull()?.emailId

    internal val hasUnresolvedBreaches: Boolean = unresolvedBreachEmails.isNotEmpty()

    internal val usedInLoginItems: ImmutableList<ItemUiModel> = when (usedInLoginItemsResult) {
        is LoadingResult.Error,
        LoadingResult.Loading -> persistentListOf()

        is LoadingResult.Success -> usedInLoginItemsResult.data.toImmutableList()
    }

    internal val hasBeenUsedInLoginItems: Boolean = usedInLoginItems.isNotEmpty()

    internal val isContentLoading: Boolean = usedInLoginItemsResult is LoadingResult.Loading ||
        breachEmailsResult is LoadingResult.Loading

    internal fun getShareIcon(shareId: ShareId): ShareIcon? = groupedVaults[shareId]?.icon

    internal companion object {

        internal val Initial = SecurityCenterReportState(
            canLoadExternalImages = false,
            breachEmailResult = LoadingResult.Loading,
            breachEmailsResult = LoadingResult.Loading,
            usedInLoginItemsResult = LoadingResult.Loading,
            breachEmailId = null,
            event = SecurityCenterReportEvent.Idle,
            groupedVaults = emptyMap()
        )

    }

}

