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

package proton.android.pass.commonrust.impl

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import proton.android.pass.commonrust.PassphraseGenerator
import proton.android.pass.commonrust.RandomPasswordGenerator
import proton.android.pass.commonrust.api.AliasPrefixValidator
import proton.android.pass.commonrust.api.CommonLibraryVersionChecker
import proton.android.pass.commonrust.api.DomainManager
import proton.android.pass.commonrust.api.EmailValidator
import proton.android.pass.commonrust.api.FileTypeDetector
import proton.android.pass.commonrust.api.NewUserInviteSignatureBodyCreator
import proton.android.pass.commonrust.api.PasswordScorer
import proton.android.pass.commonrust.api.UsableShareFilter
import proton.android.pass.commonrust.api.WifiNetworkQRGenerator
import proton.android.pass.commonrust.api.passwords.PasswordGenerator
import proton.android.pass.commonrust.api.passwords.strengths.PasswordStrengthCalculator
import proton.android.pass.commonrust.impl.passwords.PasswordGeneratorImpl
import proton.android.pass.commonrust.impl.passwords.strengths.RustPasswordStrengthCalculator
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CommonRustModule {

    @Binds
    abstract fun bindAliasPrefixValidator(impl: AliasPrefixValidatorImpl): AliasPrefixValidator

    @Binds
    abstract fun bindEmailValidator(impl: EmailValidatorImpl): EmailValidator

    @Binds
    abstract fun bindCommonLibraryVersionChecker(impl: CommonLibraryVersionCheckerImpl): CommonLibraryVersionChecker

    @Binds
    abstract fun bindNewUserInviteSignatureBodyCreator(
        impl: NewUserInviteSignatureBodyCreatorImpl
    ): NewUserInviteSignatureBodyCreator

    @[Binds Singleton]
    abstract fun bindPasswordGenerator(impl: PasswordGeneratorImpl): PasswordGenerator

    @Binds
    abstract fun bindPasswordScorer(impl: PasswordScorerImpl): PasswordScorer

    @[Binds Singleton]
    abstract fun bindPasswordStrengthCalculator(impl: RustPasswordStrengthCalculator): PasswordStrengthCalculator

    @[Binds Singleton]
    abstract fun bindDomainManager(impl: DomainManagerImpl): DomainManager

    @[Binds Singleton]
    abstract fun bindFileTypeDetector(impl: FileTypeDetectorImpl): FileTypeDetector

    @[Binds Singleton]
    abstract fun bindWifiNetworkQRGenerator(impl: WifiNetworkQRGeneratorImpl): WifiNetworkQRGenerator

    @[Binds Singleton]
    abstract fun bindUsableShareFilter(impl: UsableShareFilterImpl): UsableShareFilter

    companion object {

        @[Provides Singleton]
        fun provideRandomPasswordGenerator() = RandomPasswordGenerator()

        @[Provides Singleton]
        fun providePassphraseGenerator() = PassphraseGenerator()

    }

}
