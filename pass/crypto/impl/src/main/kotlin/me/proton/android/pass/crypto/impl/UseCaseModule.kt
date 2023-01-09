package me.proton.android.pass.crypto.impl

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.android.pass.crypto.api.usecases.CreateItem
import me.proton.android.pass.crypto.api.usecases.CreateVault
import me.proton.android.pass.crypto.api.usecases.OpenItem
import me.proton.android.pass.crypto.api.usecases.OpenKeys
import me.proton.android.pass.crypto.api.usecases.OpenShareContents
import me.proton.android.pass.crypto.api.usecases.ReadKey
import me.proton.android.pass.crypto.api.usecases.UpdateItem
import me.proton.android.pass.crypto.api.usecases.VerifyAcceptanceSignature
import me.proton.android.pass.crypto.api.usecases.VerifyShareContentSignatures
import me.proton.android.pass.crypto.impl.usecases.CreateItemImpl
import me.proton.android.pass.crypto.impl.usecases.CreateVaultImpl
import me.proton.android.pass.crypto.impl.usecases.OpenItemImpl
import me.proton.android.pass.crypto.impl.usecases.OpenKeysImpl
import me.proton.android.pass.crypto.impl.usecases.OpenShareContentsImpl
import me.proton.android.pass.crypto.impl.usecases.ReadKeyImpl
import me.proton.android.pass.crypto.impl.usecases.UpdateItemImpl
import me.proton.android.pass.crypto.impl.usecases.VerifyAcceptanceSignatureImpl
import me.proton.android.pass.crypto.impl.usecases.VerifyShareContentSignaturesImpl

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
