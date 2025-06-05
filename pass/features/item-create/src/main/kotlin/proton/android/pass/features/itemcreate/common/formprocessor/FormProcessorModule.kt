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

package proton.android.pass.features.itemcreate.common.formprocessor

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import proton.android.pass.features.itemcreate.alias.AliasItemFormState
import proton.android.pass.features.itemcreate.common.UICustomFieldContent
import proton.android.pass.features.itemcreate.common.UIExtraSection
import proton.android.pass.features.itemcreate.common.UIHiddenState
import proton.android.pass.features.itemcreate.creditcard.CreditCardItemFormState
import proton.android.pass.features.itemcreate.custom.createupdate.presentation.ItemFormState
import proton.android.pass.features.itemcreate.identity.presentation.IdentityItemFormState
import proton.android.pass.features.itemcreate.login.LoginItemFormState
import proton.android.pass.features.itemcreate.note.NoteItemFormState
import proton.android.pass.totp.api.TotpManager

@Module
@InstallIn(SingletonComponent::class)
abstract class FormProcessorModuleBinds {

    @Binds
    abstract fun bindCustomItemFormProcessor(
        impl: CustomItemFormProcessor
    ): FormProcessor<CustomItemFormProcessor.Input, ItemFormState>

    @Binds
    abstract fun bindLoginItemFormProcessor(
        impl: LoginItemFormProcessor
    ): FormProcessor<LoginItemFormProcessor.Input, LoginItemFormState>

    @Binds
    abstract fun bindCreditCardItemFormProcessor(
        impl: CreditCardItemFormProcessor
    ): FormProcessor<CreditCardItemFormProcessor.Input, CreditCardItemFormState>

    @Binds
    abstract fun bindNoteItemFormProcessor(
        impl: NoteItemFormProcessor
    ): FormProcessor<NoteItemFormProcessor.Input, NoteItemFormState>

    @Binds
    abstract fun bindIdentityItemFormProcessor(
        impl: IdentityItemFormProcessor
    ): FormProcessor<IdentityItemFormProcessor.Input, IdentityItemFormState>

    @Binds
    abstract fun bindAliasItemFormProcessor(
        impl: AliasItemFormProcessor
    ): FormProcessor<AliasItemFormProcessor.Input, AliasItemFormState>

    @Binds
    abstract fun bindPrimaryTotpFormProcessor(
        impl: PrimaryTotpFormProcessor
    ): FormProcessor<PrimaryTotpFormProcessor.Input, UIHiddenState>
}

@Module
@InstallIn(SingletonComponent::class)
object FormProcessorModule {

    @Provides
    fun provideUISectionContentFormProcessor(
        custommFieldProcessor: FormProcessor<UICustomFieldContentFormProcessor.Input, List<UICustomFieldContent>>
    ): FormProcessor<UISectionContentFormProcessor.Input, List<UIExtraSection>> =
        UISectionContentFormProcessor(custommFieldProcessor)

    @Provides
    fun provideUICustomFieldContentFormProcessor(
        totpManager: TotpManager
    ): FormProcessor<UICustomFieldContentFormProcessor.Input, List<UICustomFieldContent>> =
        UICustomFieldContentFormProcessor(totpManager)
}
