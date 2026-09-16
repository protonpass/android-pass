/*
 * Copyright (c) 2024-2026 Proton AG
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

package proton.android.pass.data.impl.usecases.extrapassword

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.usecases.GetPrimaryUserId
import proton.android.pass.data.api.usecases.extrapassword.RemoveExtraPassword
import proton.android.pass.data.impl.remote.RemoteExtraPasswordDataSource
import proton.android.pass.data.impl.repositories.ExtraPasswordRepository
import javax.inject.Inject

class RemoveExtraPasswordImpl @Inject constructor(
    private val remoteExtraPasswordDataSource: RemoteExtraPasswordDataSource,
    private val extraPasswordRepository: ExtraPasswordRepository,
    private val getPrimaryUserId: GetPrimaryUserId
) : RemoveExtraPassword {

    override suspend operator fun invoke(userId: UserId?) {
        val actualUserId = userId ?: getPrimaryUserId()
            ?: throw IllegalStateException("No primary account found")

        remoteExtraPasswordDataSource.removeExtraPassword(actualUserId)
        extraPasswordRepository.removeLocalExtraPasswordForUser(actualUserId)
    }

}
