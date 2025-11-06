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

package proton.android.pass.tracing.noop

import me.proton.core.util.kotlin.CoreLogger
import me.proton.core.util.kotlin.Logger
import org.jetbrains.annotations.NonNls
import timber.log.Timber

// Replication of the TimberLogger available in the me.proton.core.util.android.sentry.TimberLogger
// class in the utilAndroidSentry core module, but we cannot import it on fdroid in order not to
// include the Sentry SDK in the build.
@Suppress("TooManyFunctions")
private object TimberLogger : Logger {

    override fun e(tag: String, message: String) {
        Timber.tag(tag).e(message)
    }

    override fun e(tag: String, e: Throwable) {
        Timber.tag(tag).e(e)
    }

    override fun e(
        tag: String,
        e: Throwable,
        @NonNls message: String
    ) {
        Timber.tag(tag).e(e, message)
    }

    override fun w(tag: String, message: String) {
        Timber.tag(tag).w(message)
    }

    override fun w(tag: String, e: Throwable) {
        Timber.tag(tag).w(e)
    }

    override fun w(
        tag: String,
        e: Throwable,
        @NonNls message: String
    ) {
        Timber.tag(tag).w(e, message)
    }

    override fun i(tag: String, @NonNls message: String) {
        Timber.tag(tag).i(message)
    }

    override fun i(
        tag: String,
        e: Throwable,
        message: String
    ) {
        Timber.tag(tag).i(e, message)
    }

    override fun d(tag: String, message: String) {
        Timber.tag(tag).d(message)
    }

    override fun d(
        tag: String,
        e: Throwable,
        message: String
    ) {
        Timber.tag(tag).d(e, message)
    }

    override fun v(tag: String, message: String) {
        Timber.tag(tag).v(message)
    }

    override fun v(
        tag: String,
        e: Throwable,
        message: String
    ) {
        Timber.tag(tag).v(e, message)
    }
}

fun initSentryLogger(coreLogger: CoreLogger) {
    coreLogger.set(TimberLogger)
}
