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

package proton.android.pass.data.impl.local

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import proton.android.pass.domain.breach.BreachCustomEmail
import proton.android.pass.domain.breach.BreachEmail
import proton.android.pass.domain.breach.BreachEmailId
import javax.inject.Inject

interface LocalBreachesDataSource {

    suspend fun getCustomEmail(customEmailId: BreachEmailId.Custom): BreachCustomEmail

    fun observeCustomEmail(customEmailId: BreachEmailId.Custom): Flow<BreachCustomEmail>

    suspend fun upsertCustomEmail(customEmail: BreachCustomEmail)

    fun observeCustomEmails(): Flow<List<BreachCustomEmail>>

    suspend fun upsertCustomEmails(customEmails: List<BreachCustomEmail>)

    fun observeCustomEmailBreaches(): Flow<List<BreachEmail>>

    suspend fun upsertCustomEmailBreaches(customEmailBreaches: List<BreachEmail>)

}

class LocalBreachesDataSourceImpl @Inject constructor() : LocalBreachesDataSource {

    private val customEmailsFlow = MutableSharedFlow<Map<BreachEmailId.Custom, BreachCustomEmail>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val customEmailBreachesFlow = MutableSharedFlow<Map<BreachEmailId, BreachEmail>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val customEmailsCache = mutableMapOf<BreachEmailId.Custom, BreachCustomEmail>()

    private val customEmailBreachesCache = mutableMapOf<BreachEmailId, BreachEmail>()

    override suspend fun getCustomEmail(
        customEmailId: BreachEmailId.Custom
    ): BreachCustomEmail = customEmailsCache[customEmailId]
        ?: throw IllegalArgumentException("There's no custom email with id: ${customEmailId.id.id}")

    override fun observeCustomEmail(
        customEmailId: BreachEmailId.Custom
    ): Flow<BreachCustomEmail> = customEmailsFlow
        .map { customEmailsMap -> customEmailsMap[customEmailId] }
        .filterNotNull()

    override suspend fun upsertCustomEmail(customEmail: BreachCustomEmail) {
        customEmailsCache
            .put(customEmail.id, customEmail)
            .also { emitCustomEmailsChanges() }
    }

    override fun observeCustomEmails(): Flow<List<BreachCustomEmail>> = customEmailsFlow
        .map { customEmailsMap -> customEmailsMap.values.toList() }

    override suspend fun upsertCustomEmails(customEmails: List<BreachCustomEmail>) {
        customEmails
            .forEach { customEmail -> customEmailsCache[customEmail.id] = customEmail }
            .also { emitCustomEmailsChanges() }
    }

    override fun observeCustomEmailBreaches(): Flow<List<BreachEmail>> = customEmailBreachesFlow
        .map { customEmailBreachesMap -> customEmailBreachesMap.values.toList() }

    override suspend fun upsertCustomEmailBreaches(customEmailBreaches: List<BreachEmail>) {
        customEmailBreaches
            .forEach { customEmailBreach ->
                customEmailBreachesCache[customEmailBreach.emailId] = customEmailBreach
            }
            .also { emitCustomEmailBreachesChanges() }
    }

    private fun emitCustomEmailsChanges() {
        customEmailsFlow.tryEmit(customEmailsCache)
    }

    private fun emitCustomEmailBreachesChanges() {
        customEmailBreachesFlow.tryEmit(customEmailBreachesCache)
    }

}
