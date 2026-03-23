/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.data.impl.work

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import proton.android.pass.account.fakes.FakeAccountManager
import proton.android.pass.common.api.None
import proton.android.pass.crypto.fakes.context.FakeEncryptionContext
import proton.android.pass.data.fakes.repositories.FakeAssetLinkRepository
import proton.android.pass.data.fakes.usecases.FakeObserveItems
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemFlags
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareType

@RunWith(AndroidJUnit4::class)
class PeriodicAssetLinkWorkerTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private lateinit var fakeAccountManager: FakeAccountManager
    private lateinit var fakeObserveItems: FakeObserveItems
    private lateinit var fakeAssetLinkRepository: FakeAssetLinkRepository
    private lateinit var fakeUpdateAssetLink: FakeUpdateAssetLink

    @Before
    fun setup() {
        fakeAccountManager = FakeAccountManager()
        fakeObserveItems = FakeObserveItems()
        fakeAssetLinkRepository = FakeAssetLinkRepository()
        fakeUpdateAssetLink = FakeUpdateAssetLink()
    }

    @Test
    fun returnsSuccessOnNormalExecution() = runTest {
        fakeObserveItems.emitValue(emptyList())

        val result = buildWorker().doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
    }

    @Test
    fun passesWebsitesFromLoginItemsToUpdateAssetLink() = runTest {
        val items = listOf(
            loginItem(itemId = "item-1", websites = listOf("example.com", "proton.me")),
            loginItem(itemId = "item-2", websites = listOf("proton.me", "other.com"))
        )
        fakeObserveItems.emitValue(items)

        buildWorker().doWork()

        assertThat(fakeUpdateAssetLink.invocations).hasSize(1)
        assertThat(fakeUpdateAssetLink.invocations.first())
            .containsExactly("example.com", "proton.me", "other.com")
    }

    @Test
    fun deduplicatesWebsitesAcrossItems() = runTest {
        val items = listOf(
            loginItem(itemId = "item-1", websites = listOf("example.com")),
            loginItem(itemId = "item-2", websites = listOf("example.com"))
        )
        fakeObserveItems.emitValue(items)

        buildWorker().doWork()

        assertThat(fakeUpdateAssetLink.invocations.first()).containsExactly("example.com")
    }

    @Test
    fun purgesOldAssetLinksBeforeUpdating() = runTest {
        fakeObserveItems.emitValue(emptyList())

        buildWorker().doWork()

        assertThat(fakeAssetLinkRepository.purgeOlderThanInvocations).hasSize(1)
    }

    @Test
    fun returnsFailureWhenUpdateAssetLinkThrows() = runTest {
        fakeObserveItems.emitValue(listOf(loginItem(websites = listOf("example.com"))))
        fakeUpdateAssetLink.setResult(Result.failure(RuntimeException("error")))

        val result = buildWorker().doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.failure())
    }

    @Test
    fun doesNotCallUpdateAssetLinkWhenNoWebsites() = runTest {
        fakeObserveItems.emitValue(listOf(loginItem(websites = emptyList())))

        buildWorker().doWork()

        assertThat(fakeUpdateAssetLink.invocations).isEmpty()
    }

    private fun buildWorker(): PeriodicAssetLinkWorker =
        TestListenableWorkerBuilder<PeriodicAssetLinkWorker>(context)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ): ListenableWorker = PeriodicAssetLinkWorker(
                    appContext = appContext,
                    workerParameters = workerParameters,
                    accountManager = fakeAccountManager,
                    observeItems = fakeObserveItems,
                    assetLinkRepository = fakeAssetLinkRepository,
                    updateAssetLink = fakeUpdateAssetLink,
                    clock = Clock.System
                )
            })
            .build()

    companion object {
        private fun loginItem(
            itemId: String = "item",
            websites: List<String> = emptyList()
        ): Item {
            val now = Instant.fromEpochSeconds(0)
            return Item(
                id = ItemId(itemId),
                userId = UserId("user-id"),
                itemUuid = "",
                revision = 1,
                shareId = ShareId("share-id"),
                itemType = ItemType.Login(
                    itemEmail = "",
                    itemUsername = "",
                    password = FakeEncryptionContext.encrypt("") as EncryptedString,
                    websites = websites,
                    packageInfoSet = emptySet(),
                    primaryTotp = FakeEncryptionContext.encrypt("") as EncryptedString,
                    customFields = emptyList(),
                    passkeys = emptyList()
                ),
                title = FakeEncryptionContext.encrypt("") as EncryptedString,
                note = FakeEncryptionContext.encrypt("") as EncryptedString,
                content = FakeEncryptionContext.encrypt(byteArrayOf()) as EncryptedByteArray,
                state = ItemState.Active.value,
                packageInfoSet = emptySet(),
                createTime = now,
                modificationTime = now,
                lastAutofillTime = None,
                isPinned = false,
                pinTime = None,
                itemFlags = ItemFlags(0),
                shareCount = 0,
                shareType = ShareType.Vault
            )
        }
    }
}
