/*
 * Copyright (c) 2026 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package proton.android.pass.account.fakes.payment

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.domain.entity.AppStore
import me.proton.core.payment.presentation.entity.SecureEndpoint
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FakePaymentsProvideModule {
    @[Provides Singleton]
    fun provideFakeSecureEndpoint() = SecureEndpoint("foo")

    @[Provides Singleton]
    fun provideAppStore() = AppStore.GooglePlay
}
