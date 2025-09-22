/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.vault.bottomsheet.options

import androidx.annotation.StringRes
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.fakes.TestSavedStateHandleProvider
import proton.android.pass.data.api.usecases.capabilities.VaultAccessData
import proton.android.pass.data.fakes.usecases.FakeObserveEncryptedItems
import proton.android.pass.data.fakes.usecases.TestCanManageVaultAccess
import proton.android.pass.data.fakes.usecases.TestCanMigrateVault
import proton.android.pass.data.fakes.usecases.TestObserveVaults
import proton.android.pass.domain.ShareFlags
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault
import proton.android.pass.domain.VaultId
import proton.android.pass.features.vault.R
import proton.android.pass.features.vault.VaultNavigation
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.test.CallChecker
import proton.android.pass.test.HiltComponentActivity
import java.util.Date
import javax.inject.Inject

@HiltAndroidTest
class VaultOptionsBottomSheetTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltComponentActivity>()

    @Inject
    lateinit var observeVaults: TestObserveVaults

    @Inject
    lateinit var vaultAccess: TestCanManageVaultAccess

    @Inject
    lateinit var migrateVault: TestCanMigrateVault

    @Inject
    lateinit var savedStateHandle: TestSavedStateHandleProvider

    @Inject
    lateinit var observeEncryptedItems: FakeObserveEncryptedItems

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
        setVault(owned = true, shared = false)
        migrateVault.setResult(true)
        observeEncryptedItems.emitValue(emptyList())
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
        val vaultToDelete = Vault(
            userId = UserId(""),
            shareId = ShareId(SHARE_ID),
            vaultId = VaultId("vault-id"),
            name = "Test vault",
            isOwned = true,
            members = 1,
            shared = false,
            createTime = Date(),
            shareFlags = ShareFlags(0)
        )
        val anotherVault = Vault(
            userId = UserId(""),
            shareId = ShareId("OtherShare"),
            vaultId = VaultId("vault-id-2"),
            name = "another vault",
            isOwned = true,
            members = 1,
            shared = false,
            createTime = Date(),
            shareFlags = ShareFlags(0)
        )
        observeVaults.sendResult(Result.success(listOf(vaultToDelete, anotherVault)))
        runTest(R.string.bottomsheet_delete_vault) { event, checker ->
            if (event is VaultNavigation.VaultRemove) {
                checker.call(event.shareId)
            }
        }
    }

    @Test
    fun canClickViewMembers() {
        setVault(owned = false, shared = true)
        setVaultAccess(canManage = false, canViewMembers = true)
        runTest(R.string.bottomsheet_view_members) { event, checker ->
            if (event is VaultNavigation.VaultAccess) {
                checker.call(event.shareId)
            }
        }
    }

    @Test
    fun canClickManageAccess() {
        setVault(owned = true, shared = true)
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

    private fun setVault(owned: Boolean = true, shared: Boolean = false) {
        val vault = Vault(
            userId = UserId(""),
            shareId = ShareId(SHARE_ID),
            vaultId = VaultId("vault-id"),
            name = "Test vault",
            isOwned = owned,
            members = if (shared) 2 else 1,
            shared = shared,
            createTime = Date(),
            shareFlags = ShareFlags(0)
        )
        observeVaults.sendResult(Result.success(listOf(vault)))
    }

    private fun setVaultAccess(
        canManage: Boolean,
        canViewMembers: Boolean
    ) {
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
