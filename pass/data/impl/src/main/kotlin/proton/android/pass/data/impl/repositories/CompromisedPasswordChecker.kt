/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.data.impl.repositories

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.repositories.CompromisedPasswordRepository
import proton.android.pass.data.impl.db.entities.ItemEntity
import proton.android.pass.data.impl.extensions.toDomain
import proton.android.pass.domain.ItemFlag
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject
import javax.inject.Singleton

interface CompromisedPasswordChecker {
    fun onItemsUpserted(entities: List<ItemEntity>)
}

@Singleton
class CompromisedPasswordCheckerImpl @Inject constructor(
    private val encryptionContextProvider: EncryptionContextProvider,
    private val repository: CompromisedPasswordRepository,
    appDispatchers: AppDispatchers
) : CompromisedPasswordChecker {

    private val scope = CoroutineScope(appDispatchers.io + SupervisorJob())

    private val pending = Channel<List<ItemEntity>>(Channel.UNLIMITED)

    init {
        scope.launch {
            for (batch in pending) {
                process(batch)
            }
        }
    }

    override fun onItemsUpserted(entities: List<ItemEntity>) {
        val logins = entities.filter { entity ->
            entity.itemType == ItemCategory.Login.value &&
                entity.state == ItemState.Active.value &&
                entity.flags and ItemFlag.SkipHealthCheck.value == 0
        }
        if (logins.isEmpty()) return
        pending.trySend(logins)
    }

    private suspend fun process(entities: List<ItemEntity>) {
        entities.groupBy { it.userId }.forEach { (userId, userEntities) ->
            runCatching {
                val items = encryptionContextProvider.withEncryptionContextSuspendable {
                    val context = this
                    userEntities.map { entity -> entity.toDomain(context) }
                }
                repository.checkNow(UserId(userId), items)
            }.onFailure { error ->
                PassLogger.w(TAG, "Failed to check compromised passwords after upsert")
                PassLogger.w(TAG, error)
            }
        }
    }

    companion object {
        private const val TAG = "CompromisedPasswordChecker"
    }
}
