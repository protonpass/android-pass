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

package proton.android.pass.features.sl.sync.domains.select.presentation

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.domain.simplelogin.SimpleLoginAliasDomain

@Stable
internal data class SimpleLoginSyncDomainSelectState(
    internal val canSelectPremiumDomains: Boolean,
    internal val event: SimpleLoginSyncDomainSelectEvent,
    private val simpleLoginAliasDomains: List<SimpleLoginAliasDomain>
) {

    internal val aliasDomains: ImmutableList<SimpleLoginAliasDomain> = buildList {
        SimpleLoginAliasDomain(
            domain = "",
            isCustom = false,
            isPremium = true,
            isVerified = true,
            isDefault = simpleLoginAliasDomains
                .any { simpleLoginAliasDomain -> simpleLoginAliasDomain.isDefault }
                .not()
        ).also(::add)

        addAll(simpleLoginAliasDomains)
    }.toPersistentList()

    internal companion object {

        internal val Initial = SimpleLoginSyncDomainSelectState(
            canSelectPremiumDomains = false,
            event = SimpleLoginSyncDomainSelectEvent.Idle,
            simpleLoginAliasDomains = emptyList()
        )

    }

}
