package me.proton.android.pass.di

import android.content.Context
import androidx.work.WorkManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import me.proton.android.pass.log.PassKeyLogger
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.auth.domain.ClientSecret
import me.proton.core.domain.entity.Product
import me.proton.core.presentation.app.AppLifecycleObserver
import me.proton.core.presentation.app.AppLifecycleProvider
import me.proton.core.util.kotlin.Logger

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {

    @Provides
    @Singleton
    // Used in child modules
    fun provideLogger(): Logger = PassKeyLogger

    @Provides
    @Singleton
    fun provideProduct(): Product =
        Product.Drive

    @Provides
    @ClientSecret
    fun provideClientSecret(): String = ""

    @Provides
    @Singleton
    fun provideRequiredAccountType(): AccountType =
        AccountType.Internal

    @Provides
    @Singleton
    fun provideAppLifecycleObserver(): AppLifecycleObserver = AppLifecycleObserver()

    @Provides
    @Singleton
    fun provideWorkManager(
        @ApplicationContext context: Context,
    ): WorkManager = WorkManager.getInstance(context)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ApplicationBindsModule {
    @Binds
    abstract fun provideAppLifecycleStateProvider(observer: AppLifecycleObserver): AppLifecycleProvider
}
