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

package proton.android.pass.log.impl

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import proton.android.pass.log.api.LogFileManager
import proton.android.pass.log.api.PrivacySanitizer
import proton.android.pass.log.api.ShareLogsUseCase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LogsModule {

    @Binds
    @Singleton
    abstract fun bindLogFileManager(impl: LogFileManagerImpl): LogFileManager

    @Binds
    @Singleton
    abstract fun bindPrivacySanitizer(impl: PrivacySanitizerImpl): PrivacySanitizer

    @Binds
    @Singleton
    abstract fun bindShareLogsUseCase(impl: ShareLogsUseCaseImpl): ShareLogsUseCase

    companion object {
        @Provides
        @Singleton
        @LogFileMaxSize
        fun provideLogFileMaxSize(): Long = FileLoggingTree.DEFAULT_MAX_FILE_SIZE

        @Provides
        @Singleton
        @LogRotationLines
        fun provideLogRotationLines(): Int = FileLoggingTree.DEFAULT_ROTATION_LINES
    }
}
