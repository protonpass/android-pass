/*
 * Copyright (c) 2024-2025 Proton AG
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

package proton.android.pass.features.item.details.detail.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.multibindings.IntoMap
import proton.android.pass.commonpresentation.api.items.details.handlers.ItemDetailsHandler
import proton.android.pass.commonpresentation.api.items.details.handlers.ItemDetailsHandlerObserver
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.features.item.details.detail.presentation.handlers.AliasItemDetailsHandlerObserverImpl
import proton.android.pass.features.item.details.detail.presentation.handlers.CreditCardItemDetailsHandlerObserverImpl
import proton.android.pass.features.item.details.detail.presentation.handlers.CustomItemDetailsHandlerObserverImpl
import proton.android.pass.features.item.details.detail.presentation.handlers.IdentityItemDetailsHandlerObserverImpl
import proton.android.pass.features.item.details.detail.presentation.handlers.ItemDetailsHandlerImpl
import proton.android.pass.features.item.details.detail.presentation.handlers.LoginItemDetailsHandlerObserverImpl
import proton.android.pass.features.item.details.detail.presentation.handlers.NoteItemDetailsHandlerObserverImpl
import proton.android.pass.features.item.details.detail.presentation.handlers.SSHKeyDetailsHandlerObserverImpl
import proton.android.pass.features.item.details.detail.presentation.handlers.WifiNetworkDetailsHandlerObserverImpl

@[Module InstallIn(ViewModelComponent::class)]
internal abstract class CommonPresentationItemsModule {

    @[Binds ViewModelScoped]
    internal abstract fun bindItemDetailsHandler(impl: ItemDetailsHandlerImpl): ItemDetailsHandler

    @[Binds ViewModelScoped IntoMap ItemDetailsHandlerObserverKey(ItemCategory.Alias)]
    internal abstract fun bindAliasItemDetailsHandlerObserver(
        impl: AliasItemDetailsHandlerObserverImpl
    ): ItemDetailsHandlerObserver<*>

    @[Binds ViewModelScoped IntoMap ItemDetailsHandlerObserverKey(ItemCategory.CreditCard)]
    internal abstract fun bindCreditCardItemDetailsHandlerObserver(
        impl: CreditCardItemDetailsHandlerObserverImpl
    ): ItemDetailsHandlerObserver<*>

    @[Binds ViewModelScoped IntoMap ItemDetailsHandlerObserverKey(ItemCategory.Login)]
    internal abstract fun bindLoginItemDetailsHandlerObserver(
        impl: LoginItemDetailsHandlerObserverImpl
    ): ItemDetailsHandlerObserver<*>

    @[Binds ViewModelScoped IntoMap ItemDetailsHandlerObserverKey(ItemCategory.Note)]
    internal abstract fun bindNoteItemDetailsHandlerObserver(
        impl: NoteItemDetailsHandlerObserverImpl
    ): ItemDetailsHandlerObserver<*>

    @[Binds ViewModelScoped IntoMap ItemDetailsHandlerObserverKey(ItemCategory.Identity)]
    internal abstract fun bindIdentityItemDetailsHandlerObserver(
        impl: IdentityItemDetailsHandlerObserverImpl
    ): ItemDetailsHandlerObserver<*>

    @[Binds ViewModelScoped IntoMap ItemDetailsHandlerObserverKey(ItemCategory.Custom)]
    internal abstract fun bindCustomItemDetailsHandlerObserver(
        impl: CustomItemDetailsHandlerObserverImpl
    ): ItemDetailsHandlerObserver<*>

    @[Binds ViewModelScoped IntoMap ItemDetailsHandlerObserverKey(ItemCategory.WifiNetwork)]
    internal abstract fun bindWifiItemDetailsHandlerObserver(
        impl: WifiNetworkDetailsHandlerObserverImpl
    ): ItemDetailsHandlerObserver<*>

    @[Binds ViewModelScoped IntoMap ItemDetailsHandlerObserverKey(ItemCategory.SSHKey)]
    internal abstract fun bindSSHKeyItemDetailsHandlerObserver(
        impl: SSHKeyDetailsHandlerObserverImpl
    ): ItemDetailsHandlerObserver<*>

}
