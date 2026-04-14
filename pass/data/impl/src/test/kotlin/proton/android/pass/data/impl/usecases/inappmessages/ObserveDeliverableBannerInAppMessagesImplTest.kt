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

package proton.android.pass.data.impl.usecases.inappmessages

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import me.proton.core.domain.entity.UserId
import org.junit.Before
import proton.android.pass.data.fakes.repositories.FakeInAppMessagesRepository
import proton.android.pass.data.fakes.usecases.FakeObserveCurrentUser
import proton.android.pass.domain.inappmessages.InAppMessageStatus
import proton.android.pass.test.domain.InAppMessageTestFactory
import proton.android.pass.test.domain.UserTestFactory
import kotlin.test.Test

internal class ObserveDeliverableBannerInAppMessagesImplTest {

    private lateinit var instance: ObserveDeliverableBannerInAppMessagesImpl
    private lateinit var observeCurrentUser: FakeObserveCurrentUser
    private lateinit var inAppMessagesRepository: FakeInAppMessagesRepository
    private lateinit var clock: Clock
    private lateinit var userId: UserId

    @Before
    fun setup() {
        userId = UserId("test-user")
        observeCurrentUser = FakeObserveCurrentUser().apply {
            sendUser(UserTestFactory.create())
        }
        inAppMessagesRepository = FakeInAppMessagesRepository()
        clock = Clock.System
        instance = ObserveDeliverableBannerInAppMessagesImpl(
            observeCurrentUser = observeCurrentUser,
            inAppMessagesRepository = inAppMessagesRepository,
            clock = clock
        )
    }

    @Test
    fun `test no unread messages returns empty list`() = runTest {
        instance(userId).test {
            assertThat(awaitItem()).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test unread banner messages are returned immediately without throttle`() = runTest {
        val message = InAppMessageTestFactory.createBanner(
            state = InAppMessageStatus.Unread,
            range = InAppMessageTestFactory.createInAppMessageRange()
        )
        inAppMessagesRepository.addMessage(userId, message)
        instance(userId).test {
            assertThat(awaitItem()).containsExactly(message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test multiple banner messages are all returned`() = runTest {
        val message1 = InAppMessageTestFactory.createBanner(
            state = InAppMessageStatus.Unread,
            range = InAppMessageTestFactory.createInAppMessageRange()
        )
        val message2 = InAppMessageTestFactory.createBanner(
            state = InAppMessageStatus.Unread,
            range = InAppMessageTestFactory.createInAppMessageRange()
        )
        inAppMessagesRepository.addMessage(userId, message1)
        inAppMessagesRepository.addMessage(userId, message2)
        instance(userId).test {
            val items = awaitItem()
            assertThat(items).hasSize(2)
            assertThat(items).containsAtLeast(message1, message2)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
