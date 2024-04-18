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

package proton.android.pass.securitycenter.impl

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import proton.android.pass.commonrust.TwofaDomainChecker
import proton.android.pass.securitycenter.api.ObserveSecurityAnalysis
import proton.android.pass.securitycenter.api.checkers.BreachedDataChecker
import proton.android.pass.securitycenter.api.helpers.Supports2fa
import proton.android.pass.securitycenter.api.passwords.DuplicatedPasswordChecker
import proton.android.pass.securitycenter.api.passwords.InsecurePasswordChecker
import proton.android.pass.securitycenter.api.passwords.MissingTfaChecker
import proton.android.pass.securitycenter.api.passwords.RepeatedPasswordChecker
import proton.android.pass.securitycenter.api.sentinel.DisableSentinel
import proton.android.pass.securitycenter.api.sentinel.EnableSentinel
import proton.android.pass.securitycenter.api.sentinel.ObserveIsSentinelEnabled
import proton.android.pass.securitycenter.impl.checkers.BreachedDataCheckerImpl
import proton.android.pass.securitycenter.impl.checkers.DuplicatedPasswordCheckerImpl
import proton.android.pass.securitycenter.impl.checkers.InsecurePasswordCheckerImpl
import proton.android.pass.securitycenter.impl.checkers.MissingTfaCheckerImpl
import proton.android.pass.securitycenter.impl.checkers.RepeatedPasswordCheckerImpl
import proton.android.pass.securitycenter.impl.helpers.Supports2faImpl
import proton.android.pass.securitycenter.impl.sentinel.DisableSentinelImpl
import proton.android.pass.securitycenter.impl.sentinel.EnableSentinelImpl
import proton.android.pass.securitycenter.impl.sentinel.ObserveIsSentinelEnabledImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class SecurityCenterBindModule {

    @Binds
    abstract fun bindObserveSecurityAnalysis(impl: ObserveSecurityAnalysisImpl): ObserveSecurityAnalysis

    @Binds
    abstract fun bindRepeatedPasswordChecker(impl: RepeatedPasswordCheckerImpl): RepeatedPasswordChecker

    @Binds
    abstract fun bindBreachedDataChecker(impl: BreachedDataCheckerImpl): BreachedDataChecker

    @Binds
    abstract fun bindInsecurePasswordChecker(impl: InsecurePasswordCheckerImpl): InsecurePasswordChecker

    @Binds
    abstract fun bindMissingTfaChecker(impl: MissingTfaCheckerImpl): MissingTfaChecker

    @Binds
    abstract fun bindSupports2fa(impl: Supports2faImpl): Supports2fa

    @[Binds Singleton]
    abstract fun bindObserveIsSentinelEnabled(impl: ObserveIsSentinelEnabledImpl): ObserveIsSentinelEnabled

    @[Binds Singleton]
    abstract fun bindEnableSentinel(impl: EnableSentinelImpl): EnableSentinel

    @[Binds Singleton]
    abstract fun bindDisableSentinel(impl: DisableSentinelImpl): DisableSentinel

    @[Binds Singleton]
    abstract fun bindDuplicatedPasswordChecker(impl: DuplicatedPasswordCheckerImpl): DuplicatedPasswordChecker

}

@Module
@InstallIn(SingletonComponent::class)
internal object SecurityCenterProvideModule {

    @Provides
    @Singleton
    fun provideMissing2Fa(): TwofaDomainChecker = TwofaDomainChecker()

}
