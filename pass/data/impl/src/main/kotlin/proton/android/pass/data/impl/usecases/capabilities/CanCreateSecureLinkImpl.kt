/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.data.impl.usecases.capabilities

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.capabilities.CanCreateSecureLink
import proton.android.pass.data.api.usecases.organization.ObserveOrganizationSettings
import proton.android.pass.domain.OrganizationSettings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CanCreateSecureLinkImpl @Inject constructor(
    private val observeOrganizationSettings: ObserveOrganizationSettings,
    private val getUserPlan: GetUserPlan
) : CanCreateSecureLink {

    override fun invoke(): Flow<Boolean> = combine(
        observeOrganizationSettings(),
        getUserPlan()
    ) { organizationSettingsOption, plan ->
        when (organizationSettingsOption) {
            is None -> false
            is Some -> when (val settings = organizationSettingsOption.value) {
                OrganizationSettings.NotAnOrganization -> plan.isPaidPlan
                is OrganizationSettings.Organization -> settings.sharingPolicy.canShareSecureLinks
            }
        }
    }
}
