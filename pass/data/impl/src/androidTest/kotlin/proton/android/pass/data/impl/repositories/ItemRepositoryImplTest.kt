/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.data.impl.repositories

import androidx.room.Room
import androidx.room.migration.AutoMigrationSpec
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import me.proton.core.account.data.entity.AccountEntity
import me.proton.core.account.data.entity.SessionEntity
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.SessionState
import me.proton.core.domain.entity.Product
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.SessionId
import me.proton.core.user.data.entity.AddressEntity
import me.proton.core.user.data.entity.UserEntity
import me.proton.core.user.domain.entity.AddressId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import proton.android.pass.account.fakes.FakeAccountManager
import proton.android.pass.account.fakes.FakeUserAddressRepository
import proton.android.pass.crypto.api.usecases.OpenItemOutput
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.crypto.fakes.usecases.FakeCreateItem
import proton.android.pass.crypto.fakes.usecases.FakeMigrateItem
import proton.android.pass.crypto.fakes.usecases.FakeOpenItem
import proton.android.pass.crypto.fakes.usecases.FakeUpdateItem
import proton.android.pass.data.api.repositories.ItemRevision
import proton.android.pass.data.fakes.crypto.FakeGetShareAndItemKey
import proton.android.pass.data.impl.db.AppDatabase
import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.db.entities.ShareEntity
import proton.android.pass.data.impl.local.LocalItemDataSourceImpl
import proton.android.pass.data.impl.local.LocalShareDataSource
import proton.android.pass.data.impl.local.LocalShareDataSourceImpl
import proton.android.pass.data.impl.repositories.fakes.FakeRemoteItemDataSource
import proton.android.pass.data.impl.repositories.fakes.FakeShareKeyRepository
import proton.android.pass.data.impl.repositories.fakes.FakeShareRepository
import proton.android.pass.domain.ShareId
import proton.android.pass.test.domain.ItemTestFactory
import proton.android.pass.test.domain.ShareTestFactory
import proton.android.pass.test.domain.ShareTestFactoryKey
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class ItemRepositoryImplTest {

    // List of autoMigrations
    private val autoMigrations: List<AutoMigrationSpec> = emptyList()

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        autoMigrations,
        FrameworkSQLiteOpenHelperFactory()
    )

    private lateinit var database: PassDatabase
    private lateinit var localShareDataSource: LocalShareDataSource
    private lateinit var shareRepository: FakeShareRepository
    private lateinit var instance: ItemRepositoryImpl

    @Before
    fun setup() {
        val userAddressRepository = TestUserAddressRepository()
        val userAddress = userAddressRepository.generateAddress(
            displayName = USER_ID.id,
            userId = USER_ID,
            addressId = ADDRESS_ID
        )

        shareRepository = FakeShareRepository().apply {
            setGetAddressForShareIdResult(Result.success(userAddress))
        }

        database = runBlocking { setupDatabase() }
        localShareDataSource = LocalShareDataSourceImpl(
            database = database,
            usableShareFilter = { list, _ -> list.map { ShareId(it.shareId) } }
        )

        instance = ItemRepositoryImpl(
            database = database,
            accountManager = FakeAccountManager(),
            userAddressRepository = userAddressRepository.apply {
                setAddress(userAddress)
            },
            shareRepository = shareRepository,
            createItem = FakeCreateItem(),
            updateItem = FakeUpdateItem(),
            localItemDataSource = LocalItemDataSourceImpl(database, localShareDataSource),
            remoteItemDataSource = FakeRemoteItemDataSource(),
            shareKeyRepository = TestShareKeyRepository().apply {
                emitGetShareKeys(listOf(ShareKeyTestFactory.createPrivate()))
            },
            openItem = FakeOpenItem().apply {
                setOutput(
                    value = OpenItemOutput(
                        item = ItemTestFactory.create(),
                        itemKey = FakeEncryptionContextProvider().withEncryptionContext {
                            encrypt(byteArrayOf(1, 3, 4))
                        }
                    )
                )
            },
            migrateItem = FakeMigrateItem(),
            encryptionContextProvider = FakeEncryptionContextProvider(),
            getShareAndItemKey = FakeGetShareAndItemKey()
        )
    }

    @Test
    fun setShareItems() = runTest {
        val contents = listOf(
            shareItems("share1", 1, database),
            shareItems("share2", 2, database),
            shareItems("share3", 300, database),
            shareItems("share4", 400, database),
            shareItems("share5", 500, database),
            shareItems("share6", 1000, database),
            shareItems("share7", 2000, database),
        ).toMap()

        instance.setShareItems(USER_ID, contents) {}

        // Assert all items are stored
        contents.forEach { (shareId, items) ->
            val storedItems = database.itemsDao().countItems(USER_ID.id, listOf(shareId.id))
            assertEquals(items.size, storedItems)
        }
    }

    private suspend fun setupDatabase(): PassDatabase {
        // Create earliest version of the database.
        helper.createDatabase(AppDatabase.DB_NAME, 1)
            .apply { close() }

        val builder = Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            AppDatabase::class.java,
            AppDatabase.DB_NAME
        ).addMigrations(*AppDatabase.migrations.toTypedArray())

        autoMigrations.forEach { builder.addAutoMigrationSpec(it) }

        val database = builder.build()

        // Ensure we are on the latest version
        helper.runMigrationsAndValidate(
            AppDatabase.DB_NAME,
            AppDatabase.VERSION,
            true,
            *AppDatabase.migrations.toTypedArray()
        )

        database.inTransaction {
            database.accountDao().insertOrUpdate(
                AccountEntity(
                    userId = USER_ID,
                    username = USER_ID.id,
                    email = "test@email.test",
                    state = AccountState.Ready,
                    sessionId = null,
                    sessionState = SessionState.Authenticated
                )
            )

            database.sessionDao().insertOrUpdate(
                SessionEntity(
                    userId = USER_ID,
                    sessionId = SessionId("123"),
                    accessToken = "accessToken",
                    refreshToken = "refreshToken",
                    scopes = emptyList(),
                    product = Product.Pass
                )
            )
        }

        database.userDao().insertOrUpdate(
            UserEntity(
                userId = USER_ID,
                email = "test@email.test",
                name = null,
                displayName = null,
                currency = "eur",
                credit = 0,
                createdAtUtc = 1,
                usedSpace = 0,
                maxSpace = 0,
                maxUpload = 0,
                type = null,
                role = null,
                isPrivate = false,
                subscribed = 0,
                services = 0,
                delinquent = null,
                recovery = null,
                passphrase = null,
                maxBaseSpace = null,
                maxDriveSpace = null,
                usedBaseSpace = null,
                usedDriveSpace = null,
                flags = null
            )
        )

        database.addressDao().insertOrUpdate(
            AddressEntity(
                userId = USER_ID,
                addressId = ADDRESS_ID,
                email = "test-email",
                displayName = null,
                signature = null,
                domainId = null,
                canSend = true,
                canReceive = true,
                enabled = true,
                type = null,
                order = 1,
                signedKeyList = null,
            )
        )

        return database
    }

    private suspend fun shareItems(
        shareId: String,
        itemCount: Int,
        database: PassDatabase
    ): Pair<ShareId, List<ItemRevision>> {
        database.sharesDao().insertOrUpdate(
            ShareEntity(
                id = shareId,
                userId = USER_ID.id,
                addressId = ADDRESS_ID.id,
                vaultId = shareId,
                groupId = null,
                groupEmail = null,
                targetType = 1,
                targetId = shareId,
                permission = 1,
                content = null,
                contentKeyRotation = 1,
                contentFormatVersion = 1,
                expirationTime = null,
                createTime = 1,
                encryptedContent = null,
                isActive = true,
                shareRoleId = "123",
                owner = true,
                targetMembers = 0,
                shared = false,
                targetMaxMembers = 10,
                pendingInvites = 0,
                newUserInvitesReady = 0,
                canAutofill = true,
                flags = 0
            )
        )
        val id = ShareId(shareId)
        shareRepository.setGetByIdResult(id, Result.success(ShareTestFactory.Vault.create(id = shareId)))

        val items = mutableListOf<ItemRevision>()
        for (i in 0 until itemCount) {
            items.add(
                ItemRevision(
                    itemId = "item$i",
                    revision = 1,
                    contentFormatVersion = 4,
                    keyRotation = 1,
                    content = "content",
                    itemKey = "key",
                    state = 1,
                    aliasEmail = null,
                    createTime = 123,
                    modifyTime = 123,
                    lastUseTime = null,
                    revisionTime = 1,
                    isPinned = true,
                    pinTime = null,
                    flags = 1,
                    shareCount = 0
                )
            )
        }
        return id to items
    }

    companion object {
        private val USER_ID = UserId("ItemRepositoryImplTest-test-userid")
        private val ADDRESS_ID = AddressId("ItemRepositoryImplTest-test-addressid")
    }
}
