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

package proton.android.pass.features.itemcreate.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomFieldDraftRepositoryImpl @Inject constructor() : CustomFieldDraftRepository {

    private val events: MutableSharedFlow<DraftFormEvent> = MutableSharedFlow()

    override fun observeAllEvents(): Flow<DraftFormEvent> = events

    override fun observeCustomFieldEvents(): Flow<DraftFormFieldEvent> = events
        .filterIsInstance<DraftFormFieldEvent>()

    override suspend fun emit(event: DraftFormEvent) {
        events.emit(event)
    }
}
