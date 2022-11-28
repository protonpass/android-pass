package me.proton.android.pass.clipboard.impl

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.android.pass.clipboard.api.ClipboardManager

@Module
@InstallIn(SingletonComponent::class)
abstract class ClipboardModule {
    @Binds
    abstract fun bindClipboardManager(impl: ClipboardManagerImpl): ClipboardManager
}
