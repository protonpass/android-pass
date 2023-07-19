/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.crypto.impl

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import proton.android.pass.crypto.api.usecases.AcceptInvite
import proton.android.pass.crypto.api.usecases.CreateItem
import proton.android.pass.crypto.api.usecases.CreateVault
import proton.android.pass.crypto.api.usecases.MigrateItem
import proton.android.pass.crypto.api.usecases.OpenItem
import proton.android.pass.crypto.api.usecases.OpenItemKey
import proton.android.pass.crypto.api.usecases.EncryptInviteKeys
import proton.android.pass.crypto.api.usecases.UpdateItem
import proton.android.pass.crypto.api.usecases.UpdateVault
import proton.android.pass.crypto.impl.usecases.AcceptInviteImpl
import proton.android.pass.crypto.impl.usecases.CreateItemImpl
import proton.android.pass.crypto.impl.usecases.CreateVaultImpl
import proton.android.pass.crypto.impl.usecases.EncryptInviteKeysImpl
import proton.android.pass.crypto.impl.usecases.MigrateItemImpl
import proton.android.pass.crypto.impl.usecases.OpenItemImpl
import proton.android.pass.crypto.impl.usecases.OpenItemKeyImpl
import proton.android.pass.crypto.impl.usecases.UpdateItemImpl
import proton.android.pass.crypto.impl.usecases.UpdateVaultImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class UseCaseModule {

    @Binds
    abstract fun bindCreateItem(impl: CreateItemImpl): CreateItem

    @Binds
    abstract fun bindCreateVault(impl: CreateVaultImpl): CreateVault

    @Binds
    abstract fun bindOpenItem(impl: OpenItemImpl): OpenItem

    @Binds
    abstract fun bindUpdateItem(impl: UpdateItemImpl): UpdateItem

    @Binds
    abstract fun bindOpenItemKey(impl: OpenItemKeyImpl): OpenItemKey

    @Binds
    abstract fun bindUpdateVault(impl: UpdateVaultImpl): UpdateVault

    @Binds
    abstract fun bindMigrateItem(impl: MigrateItemImpl): MigrateItem

    @Binds
    abstract fun bindShareVault(impl: EncryptInviteKeysImpl): EncryptInviteKeys

    @Binds
    abstract fun bindAcceptInvite(impl: AcceptInviteImpl): AcceptInvite

}
