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

package proton.android.pass.features.itemcreate.alias

import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.espresso.Espresso
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.account.fakes.TestAccountManager
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.fakes.TestSavedStateHandleProvider
import proton.android.pass.data.api.usecases.UpgradeInfo
import proton.android.pass.data.fakes.usecases.TestCanPerformPaidAction
import proton.android.pass.data.fakes.usecases.TestCreateAlias
import proton.android.pass.data.fakes.usecases.TestObserveAliasOptions
import proton.android.pass.data.fakes.usecases.TestObserveCurrentUser
import proton.android.pass.data.fakes.usecases.TestObserveItems
import proton.android.pass.data.fakes.usecases.TestObserveUpgradeInfo
import proton.android.pass.data.fakes.usecases.TestObserveUserAccessData
import proton.android.pass.data.fakes.usecases.TestObserveVaultsWithItemCount
import proton.android.pass.domain.AliasMailbox
import proton.android.pass.domain.Plan
import proton.android.pass.domain.PlanLimit
import proton.android.pass.domain.PlanType
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault
import proton.android.pass.domain.VaultId
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.domain.entity.NewAlias
import proton.android.pass.features.itemcreate.R
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.test.CallChecker
import proton.android.pass.test.HiltComponentActivity
import proton.android.pass.test.domain.TestUser
import proton.android.pass.test.waitUntilExists
import proton.android.pass.test.writeTextAndWait
import java.util.Date
import javax.inject.Inject
import proton.android.pass.composecomponents.impl.R as CompR

@HiltAndroidTest
class CreateAliasScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltComponentActivity>()

    @Inject
    lateinit var createAlias: TestCreateAlias

    @Inject
    lateinit var accountManager: TestAccountManager

    @Inject
    lateinit var observeCurrentUser: TestObserveCurrentUser

    @Inject
    lateinit var savedStateHandle: TestSavedStateHandleProvider

    @Inject
    lateinit var observeVaults: TestObserveVaultsWithItemCount

    @Inject
    lateinit var canPerformPaidAction: TestCanPerformPaidAction

    @Inject
    lateinit var observeUpgradeInfo: TestObserveUpgradeInfo

    @Inject
    lateinit var observeAliasOptions: TestObserveAliasOptions

    @Inject
    lateinit var observeUserAccessData: TestObserveUserAccessData

    @Before
    fun setup() {
        hiltRule.inject()
        accountManager.sendPrimaryUserId(USER_ID)
        observeCurrentUser.sendUser(TestUser.create(userId = USER_ID, email = USER_EMAIL))
        savedStateHandle.get().apply {
            set(CommonNavArgId.ShareId.key, SHARE_ID)
        }
        setupPlan(PlanType.Paid.Plus("", ""))

        val vault = VaultWithItemCount(
            vault = Vault(
                userId = USER_ID,
                shareId = ShareId(SHARE_ID),
                vaultId = VaultId("vault-id"),
                name = "Test vault",
                createTime = Date()
            ),
            activeItemCount = 0,
            trashedItemCount = 0
        )
        observeVaults.sendResult(Result.success(listOf(vault)))

        observeAliasOptions.sendAliasOptions(
            proton.android.pass.domain.AliasOptions(
                suffixes = listOf(DEFAULT_SUFFIX),
                mailboxes = listOf(DEFAULT_MAILBOX)
            )
        )
        observeUserAccessData.sendValue(null)
    }

    @Test
    fun canCreateAlias() {
        val title = "Test alias"
        val expectedPrefix = "test-alias"
        val note = "A note"

        createAlias.setResult(Result.success(TestObserveItems.createAlias()))

        val checker = CallChecker<Unit>()
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    CreateAliasScreen(
                        selectVault = null,
                        onNavigate = {
                            if (it is CreateAliasNavigation.Created) {
                                checker.call()
                            }
                        },
                        canUseAttachments = true
                    )
                }
            }

            val buttonText = activity.getString(R.string.title_create)
            waitUntilExists(hasText(buttonText))

            // Title
            val titleText = activity.getString(CompR.string.field_title_title)
            onNode(hasText(titleText)).performClick().performScrollTo()
            writeTextAndWait(hasText(titleText), title)

            Espresso.closeSoftKeyboard()

            // Note
            val noteText = activity.getString(CompR.string.field_note_title)
            onNode(hasText(noteText)).performScrollTo()
            writeTextAndWait(hasText(noteText), note)

            Espresso.closeSoftKeyboard()

            onNode(hasText(buttonText)).performClick()

            waitUntil { checker.isCalled }
        }

        val memory = createAlias.getMemory()
        assertThat(memory.size).isEqualTo(1)

        val memoryItem = memory.first()
        assertThat(memoryItem.userId).isEqualTo(USER_ID)
        assertThat(memoryItem.shareId).isEqualTo(ShareId(SHARE_ID))

        val alias = NewAlias(
            title = title,
            note = note,
            prefix = expectedPrefix,
            suffix = DEFAULT_SUFFIX,
            aliasName = null,
            mailboxes = listOf(DEFAULT_MAILBOX)
        )

        assertThat(memoryItem.newAlias).isEqualTo(alias)

    }


    @Test
    fun cannotCreateLoginWithoutTitle() {
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    CreateAliasScreen(
                        selectVault = null,
                        onNavigate = {},
                        canUseAttachments = true
                    )
                }
            }

            val buttonText = activity.getString(R.string.title_create)
            waitUntilExists(hasText(buttonText))

            onNode(hasText(buttonText)).performClick()

            val errorMessage = activity.getString(CompR.string.field_title_required)
            onNode(hasText(errorMessage)).assertExists()
        }
    }


    @Test
    fun clickOnCloseClosesScreen() {
        val checker = CallChecker<Unit>()
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    CreateAliasScreen(
                        selectVault = null,
                        onNavigate = {
                            if (it == CreateAliasNavigation.CloseScreen) {
                                checker.call()
                            }
                        },
                        canUseAttachments = true
                    )
                }
            }

            val buttonText = activity.getString(R.string.title_create)
            waitUntilExists(hasText(buttonText))

            val closeContentDescription =
                activity.getString(R.string.close_scree_icon_content_description)
            onNode(hasContentDescription(closeContentDescription)).performClick()

            waitUntil { checker.isCalled }
        }
    }


    private fun setupPlan(plan: PlanType, totpLimit: PlanLimit = PlanLimit.Unlimited) {
        canPerformPaidAction.setResult(plan !is PlanType.Free)
        val upgradeInfo = UpgradeInfo(
            isUpgradeAvailable = plan is PlanType.Free,
            isSubscriptionAvailable = true,
            plan = Plan(
                planType = plan,
                hideUpgrade = false,
                vaultLimit = PlanLimit.Unlimited,
                aliasLimit = PlanLimit.Unlimited,
                totpLimit = totpLimit,
                updatedAt = 123
            ),
            totalVaults = 1,
            totalAlias = 0,
            totalTotp = 0
        )
        observeUpgradeInfo.setResult(upgradeInfo)
    }

    companion object {
        private const val SHARE_ID = "shareId-123"
        private val USER_ID = UserId("user-id-123")
        private const val USER_EMAIL = "a@b.c"

        private val DEFAULT_SUFFIX =
            proton.android.pass.domain.AliasSuffix(".test@test.test", "Test", false, "test")
        private val DEFAULT_MAILBOX = AliasMailbox(1, "test@test.test")
    }

}
