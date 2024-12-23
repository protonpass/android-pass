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

package proton.android.pass.data.impl.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import proton.android.pass.data.impl.repositories.DraftAttachmentRepositoryImpl
import java.net.URI
import kotlin.test.Test

class DraftAttachmentRepositoryImplTest {

    private lateinit var repository: DraftAttachmentRepositoryImpl

    @Before
    fun setUp() {
        repository = DraftAttachmentRepositoryImpl()
    }

    @Test
    fun `observeAll emits added URIs`() = runTest {
        val uri1 = URI("https://example.com")
        val uri2 = URI("https://another-example.com")

        repository.observeAll().test {
            assertThat(awaitItem()).isEmpty() // Initial state

            repository.add(uri1)
            assertThat(awaitItem()).containsExactly(uri1)

            repository.add(uri2)
            assertThat(awaitItem()).containsExactly(uri1, uri2)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeNew emits only newly added URIs`() = runTest {
        val uri1 = URI("https://example.com")
        val uri2 = URI("https://another-example.com")
        val uri3 = URI("https://new-uri.com")

        repository.observeNew().test {
            repository.add(uri1)
            assertThat(awaitItem()).containsExactly(uri1)

            repository.add(uri2)
            assertThat(awaitItem()).containsExactly(uri2)

            repository.add(uri3)
            assertThat(awaitItem()).containsExactly(uri3)

            repository.add(uri2) // Adding duplicate
            expectNoEvents()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `remove successfully removes a URI and updates state`() = runTest {
        val uri1 = URI("https://example.com")
        val uri2 = URI("https://another-example.com")

        repository.add(uri1)
        repository.add(uri2)

        repository.observeAll().test {
            assertThat(awaitItem()).containsExactly(uri1, uri2)

            val removed = repository.remove(uri1)
            assertThat(removed).isTrue()
            assertThat(awaitItem()).containsExactly(uri2)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `remove returns false if URI is not present`() = runTest {
        val uri = URI("https://example.com")

        repository.observeAll().test {
            assertThat(awaitItem()).isEmpty() // Initial state

            val removed = repository.remove(uri)
            assertThat(removed).isFalse()

            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clear empties all URIs and updates state`() = runTest {
        val uri1 = URI("https://example.com")
        val uri2 = URI("https://another-example.com")

        repository.add(uri1)
        repository.add(uri2)

        repository.observeAll().test {
            assertThat(awaitItem()).containsExactly(uri1, uri2)

            val cleared = repository.clear()
            assertThat(cleared).isTrue()
            assertThat(awaitItem()).isEmpty()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clear returns false if repository is already empty`() = runTest {
        repository.observeAll().test {
            assertThat(awaitItem()).isEmpty() // Initial state

            val cleared = repository.clear()
            assertThat(cleared).isFalse()

            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `contains emits true when URI is present and false when removed`() = runTest {
        val uri = URI("https://example.com")

        repository.contains(uri).test {
            assertThat(awaitItem()).isFalse() // Initial state

            repository.add(uri)
            assertThat(awaitItem()).isTrue()

            repository.remove(uri)
            assertThat(awaitItem()).isFalse()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeNew does not emit duplicates`() = runTest {
        val uri = URI("https://example.com")

        repository.observeNew().test {
            repository.add(uri)
            assertThat(awaitItem()).containsExactly(uri)

            repository.add(uri) // Adding duplicate
            expectNoEvents()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeNew re-emits an item if removed and re-added`() = runTest {
        val uri = URI("https://example.com")
        val otherUri = URI("https://another-example.com")
        repository.observeNew().test {
            repository.add(uri)
            assertThat(awaitItem()).containsExactly(uri)
            repository.add(otherUri)
            assertThat(awaitItem()).containsExactly(otherUri)

            repository.remove(uri)
            expectNoEvents()

            repository.add(uri)
            assertThat(awaitItem()).containsExactly(uri)

            cancelAndIgnoreRemainingEvents()
        }
    }

}
