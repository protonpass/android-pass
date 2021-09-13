package me.proton.android.pass.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.network.data.ApiProvider
import me.proton.core.usersettings.data.db.OrganizationDatabase
import me.proton.core.usersettings.data.db.UserSettingsDatabase
import me.proton.core.usersettings.data.repository.OrganizationRepositoryImpl
import me.proton.core.usersettings.data.repository.UserSettingsRepositoryImpl
import me.proton.core.usersettings.domain.repository.OrganizationRepository
import me.proton.core.usersettings.domain.repository.UserSettingsRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserSettingsModule {

    @Provides
    @Singleton
    fun provideUserSettingsRepository(
        db: UserSettingsDatabase,
        apiProvider: ApiProvider
    ): UserSettingsRepository =
        UserSettingsRepositoryImpl(db, apiProvider)

    @Provides
    @Singleton
    fun provideOrganizationRepository(
        db: OrganizationDatabase,
        apiProvider: ApiProvider
    ): OrganizationRepository =
        OrganizationRepositoryImpl(db, apiProvider)

}
