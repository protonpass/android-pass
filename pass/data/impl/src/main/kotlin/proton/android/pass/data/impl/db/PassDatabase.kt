/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.data.impl.db

import me.proton.core.data.room.db.Database
import proton.android.pass.data.impl.db.dao.GroupInviteDao
import proton.android.pass.data.impl.db.dao.GroupInviteKeyDao
import proton.android.pass.data.impl.db.dao.ItemsDao
import proton.android.pass.data.impl.db.dao.LiveTelemetryDao
import proton.android.pass.data.impl.db.dao.PassDataMigrationDao
import proton.android.pass.data.impl.db.dao.PassEventsDao
import proton.android.pass.data.impl.db.dao.PassOrganizationSettingsDao
import proton.android.pass.data.impl.db.dao.PasswordHistoryDao
import proton.android.pass.data.impl.db.dao.PlanDao
import proton.android.pass.data.impl.db.dao.SearchEntryDao
import proton.android.pass.data.impl.db.dao.ShareKeysDao
import proton.android.pass.data.impl.db.dao.SharesDao
import proton.android.pass.data.impl.db.dao.TelemetryDao
import proton.android.pass.data.impl.db.dao.UserAccessDataDao
import proton.android.pass.data.impl.db.dao.UserEventsDao
import proton.android.pass.data.impl.db.dao.UserInviteDao
import proton.android.pass.data.impl.db.dao.UserInviteKeyDao
import proton.android.pass.data.impl.db.dao.assetlink.AssetLinkDao
import proton.android.pass.data.impl.db.dao.assetlink.IgnoredAssetLinkDao
import proton.android.pass.data.impl.db.dao.attachment.AttachmentDao
import proton.android.pass.data.impl.db.dao.attachment.ChunkDao
import proton.android.pass.data.impl.db.dao.inappmessages.InAppMessagesDao
import proton.android.pass.data.impl.db.dao.securelinks.SecureLinksDao
import proton.android.pass.log.api.PassLogger

@Suppress("ComplexInterface", "TooManyFunctions")
interface PassDatabase : Database {

    fun sharesDao(): SharesDao

    fun itemsDao(): ItemsDao

    fun shareKeysDao(): ShareKeysDao

    fun passEventsDao(): PassEventsDao

    fun userEventsDao(): UserEventsDao

    fun telemetryEventsDao(): TelemetryDao

    fun searchEntryDao(): SearchEntryDao

    fun planDao(): PlanDao

    fun dataMigrationDao(): PassDataMigrationDao

    fun userInviteDao(): UserInviteDao

    fun userInviteKeyDao(): UserInviteKeyDao

    fun groupInviteDao(): GroupInviteDao

    fun groupInviteKeyDao(): GroupInviteKeyDao

    fun userAccessDataDao(): UserAccessDataDao

    fun organizationSettingsDao(): PassOrganizationSettingsDao

    fun liveTelemetryDao(): LiveTelemetryDao

    fun secureLinksDao(): SecureLinksDao

    fun assetLinkDao(): AssetLinkDao

    fun ignoredAssetLinkDao(): IgnoredAssetLinkDao

    fun inAppMessagesDao(): InAppMessagesDao

    fun attachmentDao(): AttachmentDao

    fun chunkDao(): ChunkDao

    fun passwordHistoryDao(): PasswordHistoryDao

    suspend fun <R> inTransaction(name: String, block: suspend () -> R): R {
        PassLogger.i(TAG, "$name transaction started")
        return inTransaction { block() }
            .also { PassLogger.i(TAG, "$name transaction finished") }
    }

    private companion object {

        private const val TAG = "PassDatabase"

    }

}
