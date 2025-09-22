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

package proton.android.pass.features.itemdetail.login

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.components.SingletonComponent
import kotlinx.datetime.Clock
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.clipboard.fakes.TestClipboardManager
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.fakes.TestSavedStateHandleProvider
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.crypto.fakes.context.TestEncryptionContext
import proton.android.pass.data.fakes.usecases.FakeGetItemById
import proton.android.pass.data.fakes.usecases.TestObserveItemById
import proton.android.pass.data.fakes.usecases.TestObserveItems
import proton.android.pass.data.fakes.usecases.TestObserveUserAccessData
import proton.android.pass.data.fakes.usecases.shares.FakeObserveShare
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareFlags
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault
import proton.android.pass.domain.VaultId
import proton.android.pass.features.item.details.detail.ui.ItemDetailsScreen
import proton.android.pass.features.item.details.shared.navigation.ItemDetailsNavDestination
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.test.CallChecker
import proton.android.pass.test.HiltComponentActivity
import proton.android.pass.test.domain.TestItem
import proton.android.pass.test.domain.TestShare
import proton.android.pass.test.domain.TestUserAccessData
import proton.android.pass.test.waitUntilExists
import proton.android.pass.totp.api.TotpManager
import proton.android.pass.totp.fakes.TestObserveTotpFromUri
import java.util.Date
import javax.inject.Inject
import kotlin.test.assertEquals

@HiltAndroidTest
class LoginDetailScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltComponentActivity>()

    @Inject
    lateinit var savedStateHandle: TestSavedStateHandleProvider

    @Inject
    lateinit var observeItemById: TestObserveItemById

    @Inject
    lateinit var getItemById: FakeGetItemById

    @Inject
    lateinit var clipboardManager: TestClipboardManager

    @Inject
    lateinit var observeTotp: TestObserveTotpFromUri

    @Inject
    lateinit var observeShare: FakeObserveShare

    @Inject
    lateinit var observeUserAccessData: TestObserveUserAccessData

    @Before
    fun setup() {
        hiltRule.inject()
        savedStateHandle.get().apply {
            set(CommonNavArgId.ShareId.key, SHARE_ID)
            set(CommonNavArgId.ItemId.key, ITEM_ID)
        }
    }

    @Test
    fun displayLoginContents() {
        val itemTitle = "item title"
        val emailOrUsername = "user@email.com"
        val note = "some note for the item"
        performSetup(title = itemTitle, email = emailOrUsername, note = note)
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    ItemDetailsScreen(
                        onNavigated = {}
                    )
                }
            }

            waitUntilExists(hasText(itemTitle))

            onNode(hasText(itemTitle)).assertExists()
            onNode(hasText(emailOrUsername)).assertExists()
            onNode(hasText(note)).assertExists()
        }
    }

    @Test
    fun revealConcealPassword() {
        val password = "r@nd0mP@ssw0rd"
        val title = performSetup(password = password)
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    ItemDetailsScreen(
                        onNavigated = {}
                    )
                }
            }

            waitUntilExists(hasText(title))

            val concealedContentDescription = activity.getString(R.string.action_reveal)
            val revealedContentDescription = activity.getString(R.string.action_conceal)

            onNodeWithContentDescription(concealedContentDescription).performClick()
            waitUntilExists(hasText(password))

            onNodeWithContentDescription(revealedContentDescription).performClick()
            onNode(hasText(password)).assertDoesNotExist()
        }
    }

    @Test
    fun clickEmailCopiesEmail() {
        val email = "user@email.com"
        performSetup(email = email)
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    ItemDetailsScreen(
                        onNavigated = {}
                    )
                }
            }

            waitUntilExists(hasText(email))

            onNode(hasText(email)).performClick()
            assertEquals(email, clipboardManager.getContents())
        }
    }

    @Test
    fun clickUsernameCopiesUsername() {
        val username = "myusername"
        performSetup(username = username)
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    ItemDetailsScreen(
                        onNavigated = {}
                    )
                }
            }

            waitUntilExists(hasText(username))

            onNode(hasText(username)).performClick()
            assertEquals(username, clipboardManager.getContents())
        }
    }

    @Test
    fun clickPasswordCopiesPassword() {
        val password = "r4Nd0mP@ssw0rd"
        val title = performSetup(password = password)
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    ItemDetailsScreen(
                        onNavigated = {}
                    )
                }
            }

            waitUntilExists(hasText(title))

            val passwordLabel = activity.getString(R.string.password)
            onNode(hasText(passwordLabel)).performClick()
            assertEquals(password, clipboardManager.getContents())
        }

    }

    @Test
    fun clickTotpCopiesTotp() {
        val title = performSetup(primaryTotp = "123")
        val totpCode = "987654"
        setupTotp("987654")
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    ItemDetailsScreen(
                        onNavigated = {}
                    )
                }
            }

            waitUntilExists(hasText(title))

            val label = activity.getString(R.string.item_details_login_section_primary_totp_title)
            onNode(hasText(label)).performClick()
            assertEquals(totpCode, clipboardManager.getContents())
        }
    }

    @Test
    fun navigateToEdit() {
        val title = performSetup()
        val checkerShareId = CallChecker<ShareId>()
        val checkerItemId = CallChecker<ItemId>()
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    ItemDetailsScreen(
                        onNavigated = {
                            if (it is ItemDetailsNavDestination.EditItem) {
                                checkerShareId.call(it.shareId)
                                checkerItemId.call(it.itemId)
                            }
                        }
                    )
                }
            }

            waitUntilExists(hasText(title))

            onNode(hasText(activity.getString(R.string.action_edit))).performClick()
            waitUntil { checkerItemId.isCalled && checkerShareId.isCalled }
            assertEquals(SHARE_ID, checkerShareId.memory?.id)
            assertEquals(ITEM_ID, checkerItemId.memory?.id)
        }
    }

    private fun performSetup(
        title: String = "some title",
        email: String = "user@email.com",
        username: String = "someusername",
        password: String = "password",
        note: String = "a note",
        urls: List<String> = emptyList(),
        primaryTotp: String = "",
        vaultName: String = "vault"
    ): String {
        val item = TestItem.create(
            shareId = ShareId(SHARE_ID),
            itemId = ItemId(ITEM_ID),
            itemContents = ItemContents.Login(
                title = title,
                itemEmail = email,
                itemUsername = username,
                password = HiddenState.Concealed(TestEncryptionContext.encrypt(password)),
                note = note,
                urls = urls,
                packageInfoSet = emptySet(),
                primaryTotp = HiddenState.Revealed(
                    encrypted = TestEncryptionContext.encrypt(primaryTotp),
                    clearText = primaryTotp
                ),
                customFields = emptyList(),
                passkeys = emptyList()
            )
        )
        val share = TestShare.Vault.create(id = SHARE_ID)

        observeItemById.emitValue(Result.success(item))
        getItemById.emit(Result.success(item))
        observeShare.emitValue(share)
        observeUserAccessData.sendValue(TestUserAccessData.random())

        return title
    }

    private fun setupTotp(value: String) {
        observeTotp.sendValue(
            Result.success(
                TotpManager.TotpWrapper(
                    code = value,
                    remainingSeconds = 25,
                    totalSeconds = 30
                )
            )
        )
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object TestModule {
        @Provides
        fun provideClock(): Clock = Clock.System
    }

    companion object {
        private const val SHARE_ID = "shareId"
        private const val ITEM_ID = "itemId"
    }

}

