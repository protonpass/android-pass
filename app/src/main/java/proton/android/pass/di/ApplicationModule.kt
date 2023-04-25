package proton.android.pass.di

import android.content.Context
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.datetime.Clock
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.domain.entity.AppStore
import me.proton.core.domain.entity.Product
import proton.android.pass.PassAppConfig
import proton.android.pass.appconfig.api.AppConfig
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {

    @Provides
    @Singleton
    fun provideProduct(): Product =
        Product.Pass

    @Provides
    @Singleton
    fun provideAppStore() =
        AppStore.GooglePlay

    @Provides
    @Singleton
    fun provideRequiredAccountType(): AccountType =
        AccountType.External

    @Provides
    @Singleton
    fun provideWorkManager(
        @ApplicationContext context: Context
    ): WorkManager = WorkManager.getInstance(context)

    @Provides
    @Singleton
    fun provideAppConfig(): AppConfig = PassAppConfig()

    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.System
}
