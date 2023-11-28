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

package proton.android.pass.featuresettings.impl.defaultvault

import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.hasText
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
import proton.android.pass.data.fakes.usecases.TestObserveVaultsWithItemCount
import proton.android.pass.data.fakes.usecases.TestSetDefaultVault
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole
import proton.android.pass.domain.Vault
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.featuresettings.impl.SettingsNavigation
import proton.android.pass.test.CallChecker
import proton.android.pass.test.HiltComponentActivity
import proton.android.pass.test.waitUntilExists
import javax.inject.Inject

@HiltAndroidTest
class DefaultVaultBottomSheetTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltComponentActivity>()

    @Inject
    lateinit var observeVaults: TestObserveVaultsWithItemCount

    @Inject
    lateinit var setDefaultVault: TestSetDefaultVault

    @Before
    fun setup() {
        hiltRule.inject()
        setupVaults()
    }

    @Test
    fun onSelectVault() {
        val checker = CallChecker<Unit>()
        composeTestRule.apply {
            setContent {
                PassTheme {
                    SelectDefaultVaultBottomSheet(
                        onNavigate = {
                            if (it == SettingsNavigation.DismissBottomSheet) {
                                checker.call()
                            }
                        }
                    )
                }
            }

            waitUntilExists(hasText(DEFAULT_VAULT_NAME))
            onNodeWithText(DEFAULT_VAULT_NAME).performClick()
            waitUntil { checker.isCalled }
        }

        val memory = setDefaultVault.getMemory()
        assertThat(memory.size).isEqualTo(1)

        val memoryItem = memory.first()
        assertThat(memoryItem.shareId).isEqualTo(ShareId(DEFAULT_VAULT_SHARE_ID))
    }

    @Test
    fun cannotSelectNonWriteableVault() {
        composeTestRule.apply {
            setContent {
                PassTheme {
                    SelectDefaultVaultBottomSheet(
                        onNavigate = {}
                    )
                }
            }

            onNodeWithText(DISABLED_VAULT_NAME).assertHasNoClickAction()
        }
    }

    private fun setupVaults() {
        observeVaults.sendResult(
            Result.success(
                listOf(
                    VaultWithItemCount(
                        vault = Vault(
                            shareId = ShareId(DEFAULT_VAULT_SHARE_ID),
                            name = DEFAULT_VAULT_NAME,
                            role = ShareRole.Admin
                        ),
                        activeItemCount = 1,
                        trashedItemCount = 0
                    ),
                    VaultWithItemCount(
                        vault = Vault(
                            shareId = ShareId("2"),
                            name = DISABLED_VAULT_NAME,
                            role = ShareRole.Read
                        ),
                        activeItemCount = 1,
                        trashedItemCount = 0
                    )
                )
            )
        )
    }

    companion object {
        const val DEFAULT_VAULT_NAME = "Vault 1"
        const val DEFAULT_VAULT_SHARE_ID = "ShareId1"

        const val DISABLED_VAULT_NAME = "Vault 2"
    }


}
