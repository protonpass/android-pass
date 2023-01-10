package proton.android.pass.crypto.impl

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import proton.android.pass.crypto.api.usecases.CreateItem
import proton.android.pass.crypto.api.usecases.CreateVault
import proton.android.pass.crypto.api.usecases.OpenItem
import proton.android.pass.crypto.api.usecases.OpenKeys
import proton.android.pass.crypto.api.usecases.OpenShareContents
import proton.android.pass.crypto.api.usecases.ReadKey
import proton.android.pass.crypto.api.usecases.UpdateItem
import proton.android.pass.crypto.api.usecases.VerifyAcceptanceSignature
import proton.android.pass.crypto.api.usecases.VerifyShareContentSignatures
import proton.android.pass.crypto.impl.usecases.CreateItemImpl
import proton.android.pass.crypto.impl.usecases.CreateVaultImpl
import proton.android.pass.crypto.impl.usecases.OpenItemImpl
import proton.android.pass.crypto.impl.usecases.OpenKeysImpl
import proton.android.pass.crypto.impl.usecases.OpenShareContentsImpl
import proton.android.pass.crypto.impl.usecases.ReadKeyImpl
import proton.android.pass.crypto.impl.usecases.UpdateItemImpl
import proton.android.pass.crypto.impl.usecases.VerifyAcceptanceSignatureImpl
import proton.android.pass.crypto.impl.usecases.VerifyShareContentSignaturesImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class UseCaseModule {

    @Binds
    abstract fun bindCreateItem(impl: CreateItemImpl): CreateItem

    @Binds
    abstract fun bindCreateVault(impl: CreateVaultImpl): CreateVault

    @Binds
    abstract fun bindOpenItem(impl: OpenItemImpl): OpenItem

    @Binds
    abstract fun bindOpenShareContents(impl: OpenShareContentsImpl): OpenShareContents

    @Binds
    abstract fun bindOpenKeys(impl: OpenKeysImpl): OpenKeys

    @Binds
    abstract fun bindUpdateItem(impl: UpdateItemImpl): UpdateItem

    @Binds
    abstract fun bindReadKey(impl: ReadKeyImpl): ReadKey

    @Binds
    abstract fun bindVerifyAcceptanceSignature(impl: VerifyAcceptanceSignatureImpl): VerifyAcceptanceSignature

    @Binds
    abstract fun bindVerifyShareContentSignatures(impl: VerifyShareContentSignaturesImpl): VerifyShareContentSignatures
}
