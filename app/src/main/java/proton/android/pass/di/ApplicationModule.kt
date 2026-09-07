/*
 * Copyright (c) 2023-2026 Proton AG
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

package proton.android.pass.di

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.datetime.Clock
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.compose.theme.AppTheme
import me.proton.core.configuration.EnvironmentConfiguration
import me.proton.core.domain.entity.AppStore
import me.proton.core.domain.entity.Product
import okhttp3.OkHttpClient
import proton.android.pass.PassAppConfig
import java.util.concurrent.TimeUnit
import proton.android.pass.R
import proton.android.pass.appconfig.api.AppConfig
import proton.android.pass.appconfig.api.BuildFlavor
import proton.android.pass.autofill.AppIcon
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.data.api.AssetLinkOkHttpClient
import proton.android.pass.data.api.PublicOkhttpClient
import proton.android.pass.data.api.WebauthnFetchOkHttpClient
import proton.android.pass.notifications.api.MainActivityAnnotation
import proton.android.pass.ui.MainActivity
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {

    private const val CONNECT_TIMEOUT = 15L
    private const val READ_WRITE_TIMEOUT = 30L
    private const val CALL_TIMEOUT = 60L

    @Provides
    @Singleton
    fun provideProduct(): Product = Product.Pass

    @Provides
    @Singleton
    fun provideAppStore(appConfig: AppConfig) = when (appConfig.flavor) {
        is BuildFlavor.Fdroid -> AppStore.FDroid
        is BuildFlavor.Alpha -> AppStore.GooglePlay
        is BuildFlavor.Dev -> AppStore.GooglePlay
        is BuildFlavor.Play -> AppStore.GooglePlay
        is BuildFlavor.Quest -> AppStore.FDroid
        is BuildFlavor.Nogms -> AppStore.FDroid
    }

    @Provides
    @Singleton
    fun provideRequiredAccountType(): AccountType = AccountType.External

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager = WorkManager.getInstance(context)

    @Provides
    @Singleton
    fun provideAppConfig(environmentConfig: EnvironmentConfiguration): AppConfig = PassAppConfig(environmentConfig)

    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.System

    @Provides
    @MainActivityAnnotation
    fun provideMainActivityClass(): Class<*> = MainActivity::class.java

    @Provides
    @AppIcon
    fun provideAppIcon(): Int = R.mipmap.ic_launcher

    @Provides
    fun provideAppTheme() = AppTheme { content ->
        PassTheme { content() }
    }

    @PublicOkhttpClient
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .retryOnConnectionFailure(false)
        .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(READ_WRITE_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(READ_WRITE_TIMEOUT, TimeUnit.SECONDS)
        .callTimeout(CALL_TIMEOUT, TimeUnit.SECONDS)
        .build()

    // Digital Asset Links spec requires the statement to be fetched from the exact declared
    // URL; a redirect invalidates it, so this client fails closed on any 3xx.
    @AssetLinkOkHttpClient
    @Provides
    @Singleton
    fun provideAssetLinkOkHttpClient(@PublicOkhttpClient base: OkHttpClient): OkHttpClient = base.newBuilder()
        .followRedirects(false)
        .followSslRedirects(false)
        .build()

    // The webauthn related-origins fetcher is designed to follow redirects: the passkey
    // library validates the redirect target's domain via `final_url`. What it can't validate
    // is scheme, so this client only blocks an https -> http downgrade, not the redirect itself.
    @WebauthnFetchOkHttpClient
    @Provides
    @Singleton
    fun provideWebauthnFetchOkHttpClient(@PublicOkhttpClient base: OkHttpClient): OkHttpClient = base.newBuilder()
        .followSslRedirects(false)
        .build()

    @[Provides Singleton]
    fun provideNotificationManagerCompat(@ApplicationContext context: Context): NotificationManagerCompat =
        NotificationManagerCompat.from(context)
}
