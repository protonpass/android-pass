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

package proton.android.pass.autofill

import java.util.concurrent.atomic.AtomicInteger

/**
 * SuggestionCounter is a thread-safe counter used to generate unique integers at runtime.
 *
 * This counter is specifically used to assign a unique `requestCode` when creating
 * a `PendingIntent` within the AutofillService.
 *
 * Why is this necessary?
 * -----------------------
 * In Android, if you create a `PendingIntent` with the same parameters (including the same `requestCode`),
 * the system may reuse an existing one instead of generating a new instance. In the context of Autofill,
 * this can lead to the same `IntentSender` being reused across different suggestions,
 * which may cause a `SendIntentException` during authentication if the intent is no longer valid.
 *
 * To prevent unintended reuse and ensure each `PendingIntent` is unique,
 * it's important to use a different `requestCode` each time.
 * This class provides a simple, efficient, and safe way to generate those unique values.
 */
object SuggestionCounter {
    private val count = AtomicInteger(0)

    fun next(): Int = count.incrementAndGet()
}
