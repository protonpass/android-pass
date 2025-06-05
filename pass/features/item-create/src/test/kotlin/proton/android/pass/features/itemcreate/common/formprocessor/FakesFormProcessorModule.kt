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
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.crypto.common.keystore.EncryptedString
import proton.android.pass.features.itemcreate.creditcard.CreditCardItemFormState
import proton.android.pass.features.itemcreate.custom.createupdate.presentation.ItemFormState
import proton.android.pass.features.itemcreate.identity.presentation.IdentityItemFormState
import proton.android.pass.features.itemcreate.login.LoginItemFormState
import proton.android.pass.features.itemcreate.note.NoteItemFormState
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FakesFormProcessorModule {

    @Binds
    abstract fun bindCustomItemFormProcessor(
        impl: FakeCustomItemFormProcessor
    ): FormProcessor<CustomItemFormProcessor.Input, ItemFormState>

    @Binds
    abstract fun bindLoginItemFormProcessor(
        impl: FakeLoginItemFormProcessor
    ): FormProcessor<LoginItemFormProcessor.Input, LoginItemFormState>

    @Binds
    abstract fun bindCreditCardItemFormProcessor(
        impl: FakeCreditCardItemFormProcessor
    ): FormProcessor<CreditCardItemFormProcessor.Input, CreditCardItemFormState>

    @Binds
    abstract fun bindNoteItemFormProcessor(
        impl: FakeNoteItemFormProcessor
    ): FormProcessor<NoteItemFormProcessor.Input, NoteItemFormState>

    @Binds
    abstract fun bindIdentityItemFormProcessor(
        impl: FakeIdentityItemFormProcessor
    ): FormProcessor<IdentityItemFormProcessor.Input, IdentityItemFormState>
}

@Singleton
class FakeCustomItemFormProcessor @Inject constructor() :
    FormProcessor<CustomItemFormProcessor.Input, ItemFormState> {
    private var result: FormProcessingResult<ItemFormState>? = null

    fun setResult(result: FormProcessingResult<ItemFormState>) {
        this.result = result
    }
    override suspend fun process(
        input: CustomItemFormProcessor.Input,
        decrypt: (EncryptedString) -> String,
        encrypt: (String) -> EncryptedString
    ): FormProcessingResult<ItemFormState> = result ?: FormProcessingResult.Success(input.formState)
}

@Singleton
class FakeLoginItemFormProcessor @Inject constructor() :
    FormProcessor<LoginItemFormProcessor.Input, LoginItemFormState> {
    private var result: FormProcessingResult<LoginItemFormState>? = null

    fun setResult(result: FormProcessingResult<LoginItemFormState>) {
        this.result = result
    }
    override suspend fun process(
        input: LoginItemFormProcessor.Input,
        decrypt: (EncryptedString) -> String,
        encrypt: (String) -> EncryptedString
    ): FormProcessingResult<LoginItemFormState> = result ?: FormProcessingResult.Success(input.formState)
}

@Singleton
class FakeCreditCardItemFormProcessor @Inject constructor() :
    FormProcessor<CreditCardItemFormProcessor.Input, CreditCardItemFormState> {
    private var result: FormProcessingResult<CreditCardItemFormState>? = null

    fun setResult(result: FormProcessingResult<CreditCardItemFormState>) {
        this.result = result
    }

    override suspend fun process(
        input: CreditCardItemFormProcessor.Input,
        decrypt: (EncryptedString) -> String,
        encrypt: (String) -> EncryptedString
    ): FormProcessingResult<CreditCardItemFormState> = result ?: FormProcessingResult.Success(input.formState)
}

@Singleton
class FakeNoteItemFormProcessor @Inject constructor() :
    FormProcessor<NoteItemFormProcessor.Input, NoteItemFormState> {
    private var result: FormProcessingResult<NoteItemFormState>? = null

    fun setResult(result: FormProcessingResult<NoteItemFormState>) {
        this.result = result
    }

    override suspend fun process(
        input: NoteItemFormProcessor.Input,
        decrypt: (EncryptedString) -> String,
        encrypt: (String) -> EncryptedString
    ): FormProcessingResult<NoteItemFormState> = result ?: FormProcessingResult.Success(input.formState)
}

@Singleton
class FakeIdentityItemFormProcessor @Inject constructor() :
    FormProcessor<IdentityItemFormProcessor.Input, IdentityItemFormState> {
    private var result: FormProcessingResult<IdentityItemFormState>? = null

    fun setResult(result: FormProcessingResult<IdentityItemFormState>) {
        this.result = result
    }
    override suspend fun process(
        input: IdentityItemFormProcessor.Input,
        decrypt: (EncryptedString) -> String,
        encrypt: (String) -> EncryptedString
    ): FormProcessingResult<IdentityItemFormState> = result ?: FormProcessingResult.Success(input.formState)
}
