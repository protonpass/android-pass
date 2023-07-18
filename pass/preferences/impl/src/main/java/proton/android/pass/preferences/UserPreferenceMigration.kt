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

package proton.android.pass.preferences

import androidx.datastore.core.DataMigration
import me.proton.android.pass.preferences.AppLockTypePrefProto
import me.proton.android.pass.preferences.BooleanPrefProto

object UserPreferenceMigration {
    val MIGRATION_1: DataMigration<UserPreferences> = object : DataMigration<UserPreferences> {
        override suspend fun shouldMigrate(currentData: UserPreferences): Boolean =
            currentData.biometricLock == BooleanPrefProto.BOOLEAN_PREFERENCE_TRUE &&
                currentData.appLockType == AppLockTypePrefProto.APP_LOCK_TYPE_UNSPECIFIED

        override suspend fun migrate(currentData: UserPreferences): UserPreferences =
            currentData.toBuilder()
                .setAppLockType(AppLockTypePrefProto.APP_LOCK_TYPE_BIOMETRICS)
                .build()

        override suspend fun cleanUp() = Unit
    }
}
