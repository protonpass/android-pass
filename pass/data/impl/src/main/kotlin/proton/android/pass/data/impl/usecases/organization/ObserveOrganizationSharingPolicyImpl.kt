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

package proton.android.pass.data.impl.usecases.organization

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.data.api.usecases.organization.ObserveOrganizationSettings
import proton.android.pass.data.api.usecases.organization.ObserveOrganizationSharingPolicy
import proton.android.pass.domain.OrganizationSettings
import proton.android.pass.domain.organizations.OrganizationSharingPolicy
import javax.inject.Inject

class ObserveOrganizationSharingPolicyImpl @Inject constructor(
    private val observeOrganizationSettings: ObserveOrganizationSettings
) : ObserveOrganizationSharingPolicy {

    override fun invoke(): Flow<OrganizationSharingPolicy> = observeOrganizationSettings()
        .mapLatest { organizationSettingsOption ->
            when (organizationSettingsOption) {
                is None -> OrganizationSharingPolicy.Default
                is Some -> {
                    when (val organizationSettings = organizationSettingsOption.value) {
                        OrganizationSettings.NotAnOrganization -> OrganizationSharingPolicy.Default
                        is OrganizationSettings.Organization -> organizationSettings.sharingPolicy
                    }
                }
            }
        }

}
