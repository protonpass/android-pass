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

package proton.android.pass.log.fakes

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import proton.android.pass.log.api.LogFileManager
import proton.android.pass.log.api.PrivacySanitizer
import proton.android.pass.log.api.ShareLogsUseCase
import proton.android.pass.log.impl.LogFileMaxSize
import proton.android.pass.log.impl.LogRotationLines
import proton.android.pass.log.impl.LogsModule
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [LogsModule::class]
)
abstract class FakesLogModule {

    @Binds
    @Singleton
    abstract fun bindLogFileManager(impl: FakeLogFileManager): LogFileManager

    @Binds
    @Singleton
    abstract fun bindPrivacySanitizer(impl: FakePrivacySanitizer): PrivacySanitizer

    @Binds
    @Singleton
    abstract fun bindShareLogsUseCase(impl: FakeShareLogsUseCase): ShareLogsUseCase

    companion object {

        private const val LOG_FILE_MAX_SIZE: Long = 4 * 1024 * 1024
        private const val LOG_ROTATION_LINES: Int = 500

        @Provides
        @Singleton
        @LogFileMaxSize
        fun provideLogFileMaxSize(): Long = LOG_FILE_MAX_SIZE

        @Provides
        @Singleton
        @LogRotationLines
        fun provideLogRotationLines(): Int = LOG_ROTATION_LINES
    }
}
