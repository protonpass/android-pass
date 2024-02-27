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

package proton.android.pass.commonpresentation.impl.items.details.handlers

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.commonpresentation.api.items.details.handlers.ItemDetailsHandlerObserver
import proton.android.pass.commonui.api.toItemContents
import proton.android.pass.commonuimodels.api.items.ItemDetailState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.GetVaultById
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemContents
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import javax.inject.Inject

class LoginItemDetailsHandlerObserverImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val getVaultById: GetVaultById,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val encryptionContextProvider: EncryptionContextProvider,
) : ItemDetailsHandlerObserver {

    private val itemDetailsFlow = MutableStateFlow<ItemDetailState.Login?>(null)

    override fun observe(item: Item): Flow<ItemDetailState> = itemDetailsFlow
        .onStart {
            combine(
                getVaultById(accountManager.getPrimaryUserId().first(), item.shareId),
                userPreferencesRepository.getUseFaviconsPreference(),
            ) { vault, useFaviconsPreference ->
                encryptionContextProvider.withEncryptionContext {
                    item.toItemContents(this@withEncryptionContext)
                }.let { itemContents ->
                    ItemDetailState.Login(
                        contents = itemContents as ItemContents.Login,
                        isPinned = item.isPinned,
                        vault = vault,
                        canLoadExternalImages = useFaviconsPreference.value(),
                    )
                }
            }
                .first()
                .let { initialItemDetailState -> itemDetailsFlow.update { initialItemDetailState } }
        }
        .flatMapLatest { itemDetailsFlow.filterNotNull() }

    override fun updateHiddenState(hiddenState: HiddenState) {
        itemDetailsFlow.update { itemDetailsState ->
            itemDetailsState?.copy(
                contents = itemDetailsState.contents.copy(
                    password = hiddenState,
                )
            )
        }
    }

}
