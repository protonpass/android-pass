package me.proton.android.pass.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.android.pass.BuildConfig
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.key.data.db.KeySaltDatabase
import me.proton.core.key.data.db.PublicAddressDatabase
import me.proton.core.key.data.repository.KeySaltRepositoryImpl
import me.proton.core.key.data.repository.PrivateKeyRepositoryImpl
import me.proton.core.key.data.repository.PublicAddressRepositoryImpl
import me.proton.core.key.domain.repository.KeySaltRepository
import me.proton.core.key.domain.repository.PrivateKeyRepository
import me.proton.core.key.domain.repository.PublicAddressRepository
import me.proton.core.network.data.ApiProvider
import me.proton.core.user.data.DefaultDomainHost
import me.proton.core.user.data.UserAddressKeySecretProvider
import me.proton.core.user.data.UserAddressManagerImpl
import me.proton.core.user.data.UserManagerImpl
import me.proton.core.user.data.db.AddressDatabase
import me.proton.core.user.data.db.UserDatabase
import me.proton.core.user.data.repository.DomainRepositoryImpl
import me.proton.core.user.data.repository.UserAddressRepositoryImpl
import me.proton.core.user.data.repository.UserRepositoryImpl
import me.proton.core.user.domain.UserAddressManager
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.Domain
import me.proton.core.user.domain.repository.DomainRepository
import me.proton.core.user.domain.repository.PassphraseRepository
import me.proton.core.user.domain.repository.UserAddressRepository
import me.proton.core.user.domain.repository.UserRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserManagerModule {

    @Provides
    @DefaultDomainHost
    fun provideDefaultDomainHost() = BuildConfig.HOST

    @Provides
    @Singleton
    fun provideUserRepositoryImpl(
        db: UserDatabase,
        provider: ApiProvider,
        context: CryptoContext
    ): UserRepositoryImpl = UserRepositoryImpl(db, provider, context)

    @Provides
    @Singleton
    fun provideUserAddressRepository(
        db: AddressDatabase,
        provider: ApiProvider,
        userRepository: UserRepository,
        userAddressKeySecretProvider: UserAddressKeySecretProvider
    ): UserAddressRepository =
        UserAddressRepositoryImpl(db, provider, userRepository, userAddressKeySecretProvider)

    @Provides
    @Singleton
    fun provideUserAddressKeyPassphraseProvider(
        userRepository: UserRepository,
        passphraseRepository: PassphraseRepository,
        cryptoContext: CryptoContext
    ): UserAddressKeySecretProvider =
        UserAddressKeySecretProvider(userRepository, passphraseRepository, cryptoContext)

    @Provides
    @Singleton
    fun provideDomainRepository(
        @DefaultDomainHost defaultDomain : Domain,
        provider: ApiProvider
    ): DomainRepository = DomainRepositoryImpl(defaultDomain, provider)

    @Provides
    @Singleton
    fun provideKeySaltRepository(
        db: KeySaltDatabase,
        provider: ApiProvider
    ): KeySaltRepository = KeySaltRepositoryImpl(db, provider)

    @Provides
    @Singleton
    fun providePrivateKeyRepository(
        provider: ApiProvider
    ): PrivateKeyRepository = PrivateKeyRepositoryImpl(provider)

    @Provides
    @Singleton
    fun providePublicAddressKeyRepository(
        db: PublicAddressDatabase,
        provider: ApiProvider
    ): PublicAddressRepository = PublicAddressRepositoryImpl(db, provider)

    @Provides
    @Singleton
    fun provideUserManager(
        userRepository: UserRepository,
        userAddressRepository: UserAddressRepository,
        passphraseRepository: PassphraseRepository,
        keySaltRepository: KeySaltRepository,
        privateKeyRepository: PrivateKeyRepository,
        userAddressKeySecretProvider: UserAddressKeySecretProvider,
        cryptoContext: CryptoContext
    ): UserManager = UserManagerImpl(
        userRepository,
        userAddressRepository,
        passphraseRepository,
        keySaltRepository,
        privateKeyRepository,
        userAddressKeySecretProvider,
        cryptoContext
    )

    @Provides
    @Singleton
    fun provideUserAddressManager(
        userRepository: UserRepository,
        userAddressRepository: UserAddressRepository,
        privateKeyRepository: PrivateKeyRepository,
        userAddressKeySecretProvider: UserAddressKeySecretProvider,
        cryptoContext: CryptoContext
    ): UserAddressManager = UserAddressManagerImpl(
        userRepository,
        userAddressRepository,
        privateKeyRepository,
        userAddressKeySecretProvider,
        cryptoContext
    )
}

@Module
@InstallIn(SingletonComponent::class)
abstract class UserManagerBindsModule {

    @Binds
    abstract fun provideUserRepository(userRepositoryImpl: UserRepositoryImpl): UserRepository

    @Binds
    abstract fun providePassphraseRepository(userRepositoryImpl: UserRepositoryImpl): PassphraseRepository
}
