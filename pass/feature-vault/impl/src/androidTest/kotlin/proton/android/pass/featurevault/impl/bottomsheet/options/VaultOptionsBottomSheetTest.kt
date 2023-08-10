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

package proton.android.pass.featurevault.impl.bottomsheet.options

import androidx.annotation.StringRes
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.fakes.TestSavedStateHandleProvider
import proton.android.pass.data.api.usecases.capabilities.VaultAccessData
import proton.android.pass.data.fakes.usecases.TestCanManageVaultAccess
import proton.android.pass.data.fakes.usecases.TestCanMigrateVault
import proton.android.pass.data.fakes.usecases.TestGetVaultById
import proton.android.pass.feature.vault.impl.R
import proton.android.pass.featurevault.impl.VaultNavigation
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.test.CallChecker
import proton.android.pass.test.HiltComponentActivity
import proton.pass.domain.ShareId
import proton.pass.domain.Vault
import javax.inject.Inject

@HiltAndroidTest
class VaultOptionsBottomSheetTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltComponentActivity>()

    @Inject
    lateinit var getVaultById: TestGetVaultById

    @Inject
    lateinit var vaultAccess: TestCanManageVaultAccess

    @Inject
    lateinit var migrateVault: TestCanMigrateVault

    @Inject
    lateinit var savedStateHandle: TestSavedStateHandleProvider

    @Before
    fun setup() {
        hiltRule.inject()
        savedStateHandle.get()[CommonNavArgId.ShareId.key] = SHARE_ID
    }

    @Test
    fun canClickEdit() {
        setVault(owned = true, shared = false)
        runTest(R.string.bottomsheet_edit) { event, checker ->
            if (event is VaultNavigation.VaultEdit) {
                checker.call(event.shareId)
            }
        }
    }

    @Test
    fun canClickMigrate() {
        setVault(owned = true, shared = false, primary = true)
        migrateVault.setResult(true)
        runTest(R.string.bottomsheet_migrate) { event, checker ->
            if (event is VaultNavigation.VaultMigrate) {
                checker.call(event.shareId)
            }
        }
    }

    @Test
    fun canClickShare() {
        setVault(owned = true, shared = false)
        runTest(R.string.bottomsheet_share_vault) { event, checker ->
            if (event is VaultNavigation.VaultShare) {
                checker.call(event.shareId)
            }
        }
    }

    @Test
    fun canClickDelete() {
        setVault(owned = true, shared = false, primary = false)
        runTest(R.string.bottomsheet_delete_vault) { event, checker ->
            if (event is VaultNavigation.VaultRemove) {
                checker.call(event.shareId)
            }
        }
    }

    @Test
    fun canClickViewMembers() {
        setVault(owned = false, shared = true, primary = false)
        setVaultAccess(canManage = false, canViewMembers = true)
        runTest(R.string.bottomsheet_view_members) { event, checker ->
            if (event is VaultNavigation.VaultAccess) {
                checker.call(event.shareId)
            }
        }
    }

    @Test
    fun canClickManageAccess() {
        setVault(owned = true, shared = true, primary = false)
        setVaultAccess(canManage = true, canViewMembers = false)
        runTest(R.string.bottomsheet_manage_access) { event, checker ->
            if (event is VaultNavigation.VaultAccess) {
                checker.call(event.shareId)
            }
        }
    }

    private fun runTest(
        @StringRes text: Int,
        callback: (VaultNavigation, CallChecker<ShareId>) -> Unit
    ) {
        val checker = CallChecker<ShareId>()
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    VaultOptionsBottomSheet(
                        onNavigate = { event -> callback(event, checker) }
                    )
                }
            }

            val viewText = activity.getString(text)
            onNodeWithText(viewText).assertExists().performClick()

            waitUntil { checker.isCalled }
        }

        assertThat(checker.memory).isEqualTo(ShareId(SHARE_ID))
    }

    private fun setVault(
        primary: Boolean = true,
        owned: Boolean = true,
        shared: Boolean = false
    ) {
        val vault = Vault(
            shareId = ShareId(SHARE_ID),
            name = "Test vault",
            isPrimary = primary,
            isOwned = owned,
            members = if (shared) 2 else 1,
            shared = shared
        )
        getVaultById.emitValue(vault)
    }

    private fun setVaultAccess(canManage: Boolean, canViewMembers: Boolean) {
        vaultAccess.setResult(
            VaultAccessData(
                canManageAccess = canManage,
                canViewMembers = canViewMembers
            )
        )
    }

    companion object {
        private const val SHARE_ID = "VaultOptionsBottomSheetTest-ShareID"
    }
}
