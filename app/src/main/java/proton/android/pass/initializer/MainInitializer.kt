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

/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton Technologies AG and Proton Mail.
 *
 * Proton Mail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Mail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Mail. If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.initializer

import android.content.Context
import androidx.startup.AppInitializer
import androidx.startup.Initializer
import me.proton.core.auth.presentation.MissingScopeInitializer
import me.proton.core.crypto.validator.presentation.init.CryptoValidatorInitializer
import me.proton.core.humanverification.presentation.HumanVerificationInitializer
import me.proton.core.network.presentation.init.UnAuthSessionFetcherInitializer
import me.proton.core.plan.presentation.UnredeemedPurchaseInitializer
import proton.android.pass.data.impl.migration.DataMigrationInitializer
import proton.android.pass.data.impl.sync.SyncInitializer
import proton.android.pass.log.impl.LoggerInitializer
import proton.android.pass.preferences.FeatureFlagsPrefetchInitializer
import proton.android.pass.telemetry.impl.startup.TelemetryInitializer
import proton.android.pass.tracing.impl.SentryInitializer

class MainInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        // No-op needed
    }

    override fun dependencies() = listOf(
        AccountStateHandlerInitializer::class.java,
        CryptoValidatorInitializer::class.java,
        EventManagerInitializer::class.java,
        HumanVerificationInitializer::class.java,
        LoggerInitializer::class.java,
        MissingScopeInitializer::class.java,
        SentryInitializer::class.java,
        StrictModeInitializer::class.java,
        SyncInitializer::class.java,
        TelemetryInitializer::class.java,
        DataMigrationInitializer::class.java,
        UnAuthSessionFetcherInitializer::class.java,
        UnredeemedPurchaseInitializer::class.java,
        FeatureFlagsPrefetchInitializer::class.java,
        AccountListenerInitializer::class.java
    )

    companion object {

        fun init(appContext: Context) {
            with(AppInitializer.getInstance(appContext)) {
                // WorkManager need to be initialized before any other dependant initializer.
                initializeComponent(WorkManagerInitializer::class.java)
                initializeComponent(MainInitializer::class.java)
            }
        }
    }
}
