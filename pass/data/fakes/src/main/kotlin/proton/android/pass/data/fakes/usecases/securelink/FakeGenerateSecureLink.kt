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

package proton.android.pass.data.fakes.usecases.securelink

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.usecases.securelink.GenerateSecureLink
import proton.android.pass.data.api.usecases.securelink.SecureLinkOptions
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.securelinks.SecureLinkId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeGenerateSecureLink @Inject constructor() : GenerateSecureLink {

    private var secureLinkId = SecureLinkId(id = "")

    fun setSecureLinkId(newSecureLinkId: String) {
        secureLinkId = SecureLinkId(id = newSecureLinkId)
    }

    override suspend fun invoke(
        userId: UserId?,
        shareId: ShareId,
        itemId: ItemId,
        options: SecureLinkOptions
    ): SecureLinkId = secureLinkId

    private companion object {

        const val DEFAULT_URL = "https://default.local"

    }

}
