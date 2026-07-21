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

package proton.android.pass.data.impl.repositories

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Test
import proton.android.pass.account.fakes.FakeAccountManager
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.data.impl.db.entities.CompromisedPasswordEntity
import proton.android.pass.data.impl.fakes.FakeLocalCompromisedPasswordDataSource
import proton.android.pass.data.impl.fakes.FakeRemoteCompromisedPasswordDataSource
import proton.android.pass.data.impl.remote.PrefixQueryResult
import proton.android.pass.domain.ItemFlag
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.test.domain.ItemTestFactory

internal class CompromisedPasswordRepositoryImplTest {

    private lateinit var instance: CompromisedPasswordRepositoryImpl
    private lateinit var remote: FakeRemoteCompromisedPasswordDataSource
    private lateinit var local: FakeLocalCompromisedPasswordDataSource

    private val userId = UserId(FakeAccountManager.USER_ID)

    @Before
    fun setup() {
        remote = FakeRemoteCompromisedPasswordDataSource()
        local = FakeLocalCompromisedPasswordDataSource()
        instance = CompromisedPasswordRepositoryImpl(
            encryptionContextProvider = FakeEncryptionContextProvider(),
            remoteDataSource = remote,
            localDataSource = local
        )
    }

    @Test
    fun `new item with last_change ok performs plain GET and persists with etag`() = runTest {
        val item = ItemTestFactory.createLogin(
            shareId = ShareId("s1"),
            itemId = ItemId("i1"),
            password = "hunter2"
        )
        val hash = sha1Upper("hunter2")
        val prefix = hash.substring(0, 6)
        val suffix = hash.substring(6)

        remote.lastChangeValue = 1_000L
        remote.suffixResultByKey[prefix to null] =
            PrefixQueryResult.Ok(etag = "abc", suffixes = setOf(suffix))

        instance.refresh(userId, listOf(item))

        assertThat(remote.suffixCalls).containsExactly(
            FakeRemoteCompromisedPasswordDataSource.SuffixCall(prefix, null)
        )
        val snapshot = local.snapshot().single()
        assertThat(snapshot.isCompromised).isTrue()
        assertThat(snapshot.passwordHash).isEqualTo(prefix)
        assertThat(snapshot.lastEtag).isEqualTo("abc")
        assertThat(snapshot.checkedAt).isGreaterThan(0L)
    }

    @Test
    fun `last_change failure still queries items with no row`() = runTest {
        val item = ItemTestFactory.createLogin(
            shareId = ShareId("s1"),
            itemId = ItemId("i1"),
            password = "hunter2"
        )
        val hash = sha1Upper("hunter2")
        val prefix = hash.substring(0, 6)

        remote.lastChangeValue = null
        remote.suffixResultByKey[prefix to null] =
            PrefixQueryResult.Ok(etag = "e", suffixes = emptySet())

        instance.refresh(userId, listOf(item))

        assertThat(remote.suffixCalls).hasSize(1)
        assertThat(local.snapshot()).hasSize(1)
        assertThat(local.snapshot().single().isCompromised).isFalse()
    }

    @Test
    fun `password changed triggers plain GET without etag`() = runTest {
        val item = ItemTestFactory.createLogin(
            shareId = ShareId("s1"),
            itemId = ItemId("i1"),
            password = "new-password"
        )
        val oldPrefix = "ABCDEF"
        val newHash = sha1Upper("new-password")
        val newPrefix = newHash.substring(0, 6)

        local.seed(
            listOf(
                CompromisedPasswordEntity(
                    userId = FakeAccountManager.USER_ID,
                    shareId = "s1",
                    itemId = "i1",
                    isCompromised = true,
                    passwordHash = oldPrefix,
                    checkedAt = 9_000_000L,
                    lastEtag = "old-etag"
                )
            )
        )
        // dbUpdatedAt in ms would be 1_000, which is < checkedAt so freshness alone would skip.
        remote.lastChangeValue = 1L
        remote.suffixResultByKey[newPrefix to null] =
            PrefixQueryResult.Ok(etag = "new-etag", suffixes = emptySet())

        instance.refresh(userId, listOf(item))

        assertThat(remote.suffixCalls).containsExactly(
            FakeRemoteCompromisedPasswordDataSource.SuffixCall(newPrefix, null)
        )
        val row = local.snapshot().single()
        assertThat(row.passwordHash).isEqualTo(newPrefix)
        assertThat(row.lastEtag).isEqualTo("new-etag")
        assertThat(row.isCompromised).isFalse()
    }

    @Test
    fun `unchanged prefix with stale db is skipped`() = runTest {
        val item = ItemTestFactory.createLogin(
            shareId = ShareId("s1"),
            itemId = ItemId("i1"),
            password = "hunter2"
        )
        val hash = sha1Upper("hunter2")
        val prefix = hash.substring(0, 6)

        local.seed(
            listOf(
                CompromisedPasswordEntity(
                    userId = FakeAccountManager.USER_ID,
                    shareId = "s1",
                    itemId = "i1",
                    isCompromised = false,
                    passwordHash = prefix,
                    checkedAt = 10_000L,
                    lastEtag = "etag-1"
                )
            )
        )
        remote.lastChangeValue = 5L // 5 seconds = 5_000 ms, <= 10_000

        instance.refresh(userId, listOf(item))

        assertThat(remote.suffixCalls).isEmpty()
        assertThat(local.snapshot().single().checkedAt).isEqualTo(10_000L)
    }

    @Test
    fun `304 response keeps isCompromised and etag but bumps checkedAt`() = runTest {
        val item = ItemTestFactory.createLogin(
            shareId = ShareId("s1"),
            itemId = ItemId("i1"),
            password = "hunter2"
        )
        val hash = sha1Upper("hunter2")
        val prefix = hash.substring(0, 6)

        local.seed(
            listOf(
                CompromisedPasswordEntity(
                    userId = FakeAccountManager.USER_ID,
                    shareId = "s1",
                    itemId = "i1",
                    isCompromised = true,
                    passwordHash = prefix,
                    checkedAt = 1_000L,
                    lastEtag = "stored-etag"
                )
            )
        )
        remote.lastChangeValue = 10L // 10_000 ms > 1_000
        remote.suffixResultByKey[prefix to "stored-etag"] = PrefixQueryResult.NotModified

        instance.refresh(userId, listOf(item))

        assertThat(remote.suffixCalls).containsExactly(
            FakeRemoteCompromisedPasswordDataSource.SuffixCall(prefix, "stored-etag")
        )
        val row = local.snapshot().single()
        assertThat(row.isCompromised).isTrue()
        assertThat(row.lastEtag).isEqualTo("stored-etag")
        assertThat(row.checkedAt).isGreaterThan(1_000L)
    }

    @Test
    fun `200 with updated suffixes flips isCompromised and updates etag`() = runTest {
        val item = ItemTestFactory.createLogin(
            shareId = ShareId("s1"),
            itemId = ItemId("i1"),
            password = "hunter2"
        )
        val hash = sha1Upper("hunter2")
        val prefix = hash.substring(0, 6)
        val suffix = hash.substring(6)

        local.seed(
            listOf(
                CompromisedPasswordEntity(
                    userId = FakeAccountManager.USER_ID,
                    shareId = "s1",
                    itemId = "i1",
                    isCompromised = false,
                    passwordHash = prefix,
                    checkedAt = 1_000L,
                    lastEtag = "old-etag"
                )
            )
        )
        remote.lastChangeValue = 10L
        remote.suffixResultByKey[prefix to "old-etag"] =
            PrefixQueryResult.Ok(etag = "new-etag", suffixes = setOf(suffix))

        instance.refresh(userId, listOf(item))

        val row = local.snapshot().single()
        assertThat(row.isCompromised).isTrue()
        assertThat(row.lastEtag).isEqualTo("new-etag")
    }

    @Test
    fun `batch issues last_change once and caches prefix responses`() = runTest {
        val newItem = ItemTestFactory.createLogin(
            shareId = ShareId("s1"),
            itemId = ItemId("new"),
            password = "alpha"
        )
        val skipItem = ItemTestFactory.createLogin(
            shareId = ShareId("s1"),
            itemId = ItemId("skip"),
            password = "beta"
        )
        val condItem = ItemTestFactory.createLogin(
            shareId = ShareId("s1"),
            itemId = ItemId("cond"),
            password = "alpha" // same prefix as newItem
        )

        val alphaHash = sha1Upper("alpha")
        val alphaPrefix = alphaHash.substring(0, 6)
        val betaHash = sha1Upper("beta")
        val betaPrefix = betaHash.substring(0, 6)

        local.seed(
            listOf(
                CompromisedPasswordEntity(
                    userId = FakeAccountManager.USER_ID,
                    shareId = "s1",
                    itemId = "skip",
                    isCompromised = false,
                    passwordHash = betaPrefix,
                    checkedAt = 10_000L,
                    lastEtag = "beta-etag"
                ),
                CompromisedPasswordEntity(
                    userId = FakeAccountManager.USER_ID,
                    shareId = "s1",
                    itemId = "cond",
                    isCompromised = false,
                    passwordHash = alphaPrefix,
                    checkedAt = 1_000L,
                    lastEtag = null // null etag -> PLAIN_GET with etag=null
                )
            )
        )
        remote.lastChangeValue = 5L // 5_000 ms > cond.checkedAt, <= skip.checkedAt
        remote.suffixResultByKey[alphaPrefix to null] =
            PrefixQueryResult.Ok(etag = "alpha-etag", suffixes = emptySet())

        instance.refresh(userId, listOf(newItem, skipItem, condItem))

        assertThat(remote.lastChangeCallCount).isEqualTo(1)
        // newItem and condItem share prefix alpha with etag=null → exactly one call (cached).
        // skipItem skipped.
        assertThat(remote.suffixCalls).containsExactly(
            FakeRemoteCompromisedPasswordDataSource.SuffixCall(alphaPrefix, null)
        )
    }

    @Test
    fun `error on one prefix does not affect other prefix's persistence`() = runTest {
        val okItem = ItemTestFactory.createLogin(
            shareId = ShareId("s1"),
            itemId = ItemId("ok"),
            password = "alpha"
        )
        val errItem = ItemTestFactory.createLogin(
            shareId = ShareId("s1"),
            itemId = ItemId("err"),
            password = "beta"
        )
        val alphaHash = sha1Upper("alpha")
        val alphaPrefix = alphaHash.substring(0, 6)
        val betaHash = sha1Upper("beta")
        val betaPrefix = betaHash.substring(0, 6)

        remote.lastChangeValue = 0L
        remote.suffixResultByKey[alphaPrefix to null] =
            PrefixQueryResult.Ok(etag = "a", suffixes = emptySet())
        remote.suffixResultByKey[betaPrefix to null] = PrefixQueryResult.Error

        instance.refresh(userId, listOf(okItem, errItem))

        val rows = local.snapshot()
        assertThat(rows).hasSize(1)
        assertThat(rows.single().itemId).isEqualTo("ok")
        assertThat(rows.single().lastEtag).isEqualTo("a")
    }

    @Test
    fun `item with SkipCompromisedPasswordCheck flag is not queried`() = runTest {
        val item = ItemTestFactory.createLogin(
            shareId = ShareId("s1"),
            itemId = ItemId("i1"),
            password = "hunter2",
            flags = ItemFlag.SkipCompromisedPasswordCheck.value
        )

        remote.lastChangeValue = 1_000L

        instance.refresh(userId, listOf(item))

        assertThat(remote.suffixCalls).isEmpty()
        assertThat(local.snapshot()).isEmpty()
    }

    @Test
    fun `item with SkipHealthCheck flag is also not queried for compromised`() = runTest {
        val item = ItemTestFactory.createLogin(
            shareId = ShareId("s1"),
            itemId = ItemId("i1"),
            password = "hunter2",
            flags = ItemFlag.SkipHealthCheck.value
        )

        remote.lastChangeValue = 1_000L

        instance.refresh(userId, listOf(item))

        assertThat(remote.suffixCalls).isEmpty()
    }

    private fun sha1Upper(s: String): String {
        val md = java.security.MessageDigest.getInstance("SHA-1")
        return md.digest(s.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02X".format(it) }
    }
}
