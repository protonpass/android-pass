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

package proton.android.pass.biometry

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class BiometryBinderModule {

    @Binds
    abstract fun bindBiometryManager(impl: BiometryManagerImpl): BiometryManager

    @Binds
    abstract fun bindAuthTimeHolder(impl: BiometryAuthTimeHolderImpl): BiometryAuthTimeHolder

    @Binds
    abstract fun bindNeedsBiometricAuth(impl: NeedsBiometricAuthImpl): NeedsBiometricAuth

    @Binds
    abstract fun bindBootCountRetriever(impl: BootCountRetrieverImpl): BootCountRetriever

    @Binds
    abstract fun bindElapsedTimeProvider(impl: ElapsedTimeProviderImpl): ElapsedTimeProvider

    @Binds
    abstract fun bindStoreAuthSuccessful(impl: StoreAuthSuccessfulImpl): StoreAuthSuccessful

    @Binds
    abstract fun bindResetAuthPreferences(impl: ResetAuthPreferencesImpl): ResetAuthPreferences

    @Binds
    abstract fun bindStoreAuthOnStop(impl: ExtendAuthTimeImpl): ExtendAuthTime

    @Binds
    abstract fun bindAuthOverrideState(impl: AuthOverrideStateImpl): AuthOverrideState
}
