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

package proton.android.pass.features.itemdetail.login

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.features.itemdetail.ItemDetailNavScope
import proton.android.pass.securitycenter.api.passwords.DuplicatedPasswordReport
import proton.android.pass.securitycenter.api.passwords.InsecurePasswordsReport
import proton.android.pass.securitycenter.api.passwords.Missing2faReport

private const val REUSED_PASSWORD_DISPLAY_MODE_THRESHOLD = 5

@Stable
internal data class LoginMonitorState(
    internal val isExcludedFromMonitor: Boolean,
    private val navigationScope: ItemDetailNavScope,
    private val insecurePasswordsReport: InsecurePasswordsReport,
    private val duplicatedPasswordsReport: DuplicatedPasswordReport,
    private val missing2faReport: Missing2faReport,
    private val encryptionContextProvider: EncryptionContextProvider
) {

    internal enum class ReusedPasswordDisplayMode {
        Compact,
        Expanded
    }

    internal val shouldDisplayMonitoring: Boolean = when (navigationScope) {
        ItemDetailNavScope.Default -> false

        ItemDetailNavScope.MonitorExcluded,
        ItemDetailNavScope.MonitorReport,
        ItemDetailNavScope.MonitorWeakPassword,
        ItemDetailNavScope.MonitorMissing2fa,
        ItemDetailNavScope.MonitorReusedPassword -> true

    }

    internal val isPasswordInsecure: Boolean by lazy {
        insecurePasswordsReport.hasInsecurePasswords
    }

    internal val isPasswordReused: Boolean by lazy {
        duplicatedPasswordsReport.hasDuplications
    }

    internal val isMissingTwoFa: Boolean by lazy {
        missing2faReport.isMissingTwoFa
    }

    internal val reusedPasswordDisplayMode: ReusedPasswordDisplayMode by lazy {
        if (duplicatedPasswordsReport.duplicationCount > REUSED_PASSWORD_DISPLAY_MODE_THRESHOLD) {
            ReusedPasswordDisplayMode.Compact
        } else {
            ReusedPasswordDisplayMode.Expanded
        }
    }

    internal val reusedPasswordCount: Int by lazy {
        duplicatedPasswordsReport.duplicationCount
    }

    internal val reusedPasswordItems: ImmutableList<ItemUiModel> by lazy {
        duplicatedPasswordsReport.duplications
            .map { item ->
                encryptionContextProvider.withEncryptionContext {
                    item.toUiModel(this@withEncryptionContext)
                }
            }
            .toPersistentList()
    }

}
