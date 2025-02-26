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

package proton.android.pass.data.api.repositories

import kotlinx.coroutines.flow.Flow
import proton.android.pass.common.api.Option

const val DRAFT_PASSWORD_KEY = "draftpassword"
const val DRAFT_NEW_CUSTOM_FIELD_KEY = "newCustomField"
const val DRAFT_NEW_CUSTOM_SECTION_KEY = "newCustomSection"
const val DRAFT_IDENTITY_EXTRA_SECTION_KEY = "identityExtraSection"
const val DRAFT_IDENTITY_CUSTOM_FIELD_KEY = "identityCustomField"
const val DRAFT_EDIT_CUSTOM_FIELD_TITLE_KEY = "customFieldTitle"
const val DRAFT_EDIT_CUSTOM_SECTION_TITLE_KEY = "customSectionTitle"
const val DRAFT_REMOVE_CUSTOM_FIELD_KEY = "removeCustomField"
const val DRAFT_REMOVE_CUSTOM_SECTION_KEY = "removeCustomSection"

interface DraftRepository {
    fun save(key: String, value: Any)
    fun <T> get(key: String): Flow<Option<T>>
    fun <T> delete(key: String): Option<T>
}
