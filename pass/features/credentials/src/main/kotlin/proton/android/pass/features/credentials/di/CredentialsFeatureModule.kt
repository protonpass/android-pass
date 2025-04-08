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

package proton.android.pass.features.credentials.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.scopes.ServiceScoped
import proton.android.pass.features.credentials.shared.passkeys.create.PasskeyCredentialsCreator
import proton.android.pass.features.credentials.shared.passkeys.create.PasskeyCredentialsCreatorImpl
import proton.android.pass.features.credentials.shared.passkeys.search.PasskeyCredentialsSearcher
import proton.android.pass.features.credentials.shared.passkeys.search.PasskeyCredentialsSearcherImpl
import proton.android.pass.features.credentials.shared.passwords.create.PasswordCredentialsCreator
import proton.android.pass.features.credentials.shared.passwords.create.PasswordCredentialsCreatorImpl
import proton.android.pass.features.credentials.shared.passwords.search.PasswordCredentialsSearcher
import proton.android.pass.features.credentials.shared.passwords.search.PasswordCredentialsSearcherImpl

@[Module InstallIn(ServiceComponent::class)]
internal abstract class CredentialsFeatureModule {

    @[Binds ServiceScoped]
    internal abstract fun bindPasswordCredentialsCreator(
        impl: PasswordCredentialsCreatorImpl
    ): PasswordCredentialsCreator

    @[Binds ServiceScoped]
    internal abstract fun bindPasswordCredentialsSearcher(
        impl: PasswordCredentialsSearcherImpl
    ): PasswordCredentialsSearcher

    @[Binds ServiceScoped]
    internal abstract fun bindPasskeyCredentialsCreatorImpl(
        impl: PasskeyCredentialsCreatorImpl
    ): PasskeyCredentialsCreator

    @[Binds ServiceScoped]
    internal abstract fun bindPasskeyCredentialsSearcherImpl(
        impl: PasskeyCredentialsSearcherImpl
    ): PasskeyCredentialsSearcher

}
