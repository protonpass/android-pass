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

package proton.android.pass.data.impl.db.dao.assetlink

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaInstant
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import proton.android.pass.data.impl.db.AppDatabase
import proton.android.pass.data.impl.db.entities.AssetLinkEntity
import proton.android.pass.data.impl.db.entities.IgnoredAssetLinkEntity
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class AssetLinkDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var assetLinkDao: AssetLinkDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        assetLinkDao = db.assetLinkDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun testObserveByPackageName() = runTest {
        val now = getFormattedCurrentTime()
        val packageName = "com.example.package"
        val ignoredWebsite = "ignoredwebsite.com"
        val assetLink1 = AssetLinkEntity("website1.com", packageName, now, "signature1")
        val assetLink2 = AssetLinkEntity("website2.com", packageName, now, "signature2")
        val assetLink3 = AssetLinkEntity(ignoredWebsite, packageName, now, "signature3")
        val assetLinks = listOf(assetLink1, assetLink2, assetLink3)

        assetLinks.forEach { assetLinkDao.insertOrIgnore(it) }
        db.ignoredAssetLinkDao().insertOrIgnore(IgnoredAssetLinkEntity(ignoredWebsite))

        val observedResults = assetLinkDao.observeByPackageName(packageName).first()

        val expectedResults = listOf(assetLink1, assetLink2)

        assertEquals(observedResults.size, expectedResults.size)
        assertContentEquals(expectedResults, observedResults)
    }

    @Test
    fun testObserveByPackageName_withSubdomains() = runTest {
        val now = getFormattedCurrentTime()
        val packageName = "com.example.package"

        val ignoredWebsite = "ignoredwebsite.com"
        val subdomainIgnoredWebsite = "sub.$ignoredWebsite"

        val assetLink1 = AssetLinkEntity("website1.com", packageName, now, "signature1") // Should be included
        val assetLink2 = AssetLinkEntity(subdomainIgnoredWebsite, packageName, now, "signature2") // Should be ignored
        val assetLink3 = AssetLinkEntity(ignoredWebsite, packageName, now, "signature3") // Should be ignored

        val assetLinks = listOf(assetLink1, assetLink2, assetLink3)

        assetLinks.forEach { assetLinkDao.insertOrIgnore(it) }
         db.ignoredAssetLinkDao().insertOrIgnore(IgnoredAssetLinkEntity(ignoredWebsite))

        val observedResults = assetLinkDao.observeByPackageName(packageName).first()

        val expectedResults = listOf(assetLink1)

        assertEquals(expectedResults.size, observedResults.size)
        assertContentEquals(expectedResults, observedResults)
    }
}

fun getFormattedCurrentTime(): Instant {
    val now: Instant = Clock.System.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .withZone(ZoneOffset.UTC)
    return formatter.format(now.toJavaInstant()).toInstant()
}
