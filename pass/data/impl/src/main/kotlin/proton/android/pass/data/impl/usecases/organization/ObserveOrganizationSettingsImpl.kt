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

package proton.android.pass.data.impl.usecases.organization

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.OrganizationSettingsRepository
import proton.android.pass.data.api.usecases.organization.ObserveOrganizationSettings
import proton.android.pass.domain.OrganizationSettings
import javax.inject.Inject

class ObserveOrganizationSettingsImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val repository: OrganizationSettingsRepository
) : ObserveOrganizationSettings {

    override fun invoke(): Flow<OrganizationSettings> = accountManager.getPrimaryUserId()
        .filterNotNull()
        .flatMapLatest(::observeSettings)
        .filterNotNull()

    private fun observeSettings(userId: UserId): Flow<OrganizationSettings?> = repository.observe(userId)
        .onEach {
            if (it == null) {
                runCatching { repository.refresh(userId) }
            }
        }

}
