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

package proton.android.pass.securitycenter.fakes

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import proton.android.pass.securitycenter.api.sentinel.DisableSentinel
import proton.android.pass.securitycenter.api.sentinel.EnableSentinel
import proton.android.pass.securitycenter.fakes.sentinel.FakeDisableSentinel
import proton.android.pass.securitycenter.fakes.sentinel.FakeEnableSentinel

@[Module InstallIn(SingletonComponent::class)]
internal abstract class FakeSecurityCenterModule {

    @Binds
    internal abstract fun bindDisableSentinel(impl: FakeDisableSentinel): DisableSentinel

    @Binds
    internal abstract fun bindEnableSentinel(impl: FakeEnableSentinel): EnableSentinel

}
