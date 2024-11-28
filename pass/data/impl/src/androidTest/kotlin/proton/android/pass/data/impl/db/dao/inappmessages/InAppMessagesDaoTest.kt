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

package proton.android.pass.data.impl.db.dao.inappmessages

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import proton.android.pass.data.impl.db.AppDatabase
import proton.android.pass.data.impl.db.entities.InAppMessageEntity
import kotlin.time.Duration.Companion.days

@RunWith(AndroidJUnit4::class)
class AssetLinkDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var inAppMessagesDao: InAppMessagesDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        db.query("PRAGMA foreign_keys=OFF;", null)
        inAppMessagesDao = db.inAppMessagesDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun shouldReturnOnlyUnread() = runTest {
        val now = Clock.System.now()
        val userId = "userId"
        val message1 = createEntity("1", userId, 0, now.minus(1.days).epochSeconds)
        val message2 = createEntity("2", userId, 1, now.minus(1.days).epochSeconds)
        inAppMessagesDao.insertOrIgnore(message1, message2)
        val messages = inAppMessagesDao.observeDeliverableUserMessages(userId, now.epochSeconds).first()
        assertEquals(1, messages.size)
        assertEquals("1", messages[0].id)
    }

    @Test
    fun shouldReturnOnlyInTimeRange() = runTest {
        val now = Clock.System.now()
        val userId = "userId"
        val message1 = createEntity("1", userId, 0, now.minus(1.days).epochSeconds, now.plus(1.days).epochSeconds)
        val message2 = createEntity("2", userId, 0, now.minus(2.days).epochSeconds, now.minus(1.days).epochSeconds)
        inAppMessagesDao.insertOrIgnore(message1, message2)
        val messages = inAppMessagesDao.observeDeliverableUserMessages(userId, now.epochSeconds).first()
        assertEquals(1, messages.size)
        assertEquals("1", messages[0].id)
    }

    @Test
    fun shouldReturnInPriorityAndStartTimeOrder() = runTest {
        val now = Clock.System.now()
        val userId = "userId"
        val message1 = createEntity("1", userId, 0, now.minus(2.days).epochSeconds, priority = 1)
        val message2 = createEntity("2", userId, 0, now.minus(1.days).epochSeconds, priority = 1)
        val message3 = createEntity("3", userId, 0, now.minus(1.days).epochSeconds, priority = 2)
        val message4 = createEntity("4", userId, 0, now.minus(3.days).epochSeconds, priority = 2)
        inAppMessagesDao.insertOrIgnore(message1, message2, message3, message4)
        val messages = inAppMessagesDao.observeDeliverableUserMessages(userId, now.epochSeconds).first()
        assertEquals(4, messages.size)
        assertEquals("4", messages[0].id)
        assertEquals("3", messages[1].id)
        assertEquals("1", messages[2].id)
        assertEquals("2", messages[3].id)
    }

    @Test
    fun shouldReturnOnlyForUser() = runTest {
        val now = Clock.System.now()
        val userId = "userId"
        val message1 = createEntity("1", userId, 0, now.minus(1.days).epochSeconds)
        val message2 = createEntity("2", "otherUserId", 0, now.minus(1.days).epochSeconds)
        inAppMessagesDao.insertOrIgnore(message1, message2)
        val messages = inAppMessagesDao.observeDeliverableUserMessages(userId, now.epochSeconds).first()
        assertEquals(1, messages.size)
        assertEquals("1", messages[0].id)
    }

    private fun createEntity(
        id: String,
        userId: String,
        state: Int,
        start: Long,
        end: Long? = null,
        priority: Int = 1
    ) = InAppMessageEntity(
        id = id,
        key = "key",
        mode = 0,
        priority = priority,
        title = "title",
        message = "message",
        imageUrl = "imageUrl",
        ctaText = "ctaText",
        ctaRoute = "ctaRoute",
        ctaType = "ctaType",
        state = state,
        rangeStart = start,
        rangeEnd = end,
        userId = userId
    )
}
