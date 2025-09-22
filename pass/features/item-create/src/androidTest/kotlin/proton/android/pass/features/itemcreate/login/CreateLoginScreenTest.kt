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

package proton.android.pass.features.itemcreate.login

import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performScrollToKey
import androidx.compose.ui.test.performTextReplacement
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.runBlocking
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.account.fakes.TestAccountManager
import proton.android.pass.common.api.None
import proton.android.pass.common.api.some
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.fakes.TestSavedStateHandleProvider
import proton.android.pass.crypto.fakes.context.TestEncryptionContext
import proton.android.pass.data.api.usecases.UpgradeInfo
import proton.android.pass.data.fakes.repositories.TestDraftRepository
import proton.android.pass.data.fakes.usecases.TestCanPerformPaidAction
import proton.android.pass.data.fakes.usecases.TestCreateItem
import proton.android.pass.data.fakes.usecases.TestCreateLoginAndAlias
import proton.android.pass.data.fakes.usecases.TestObserveCurrentUser
import proton.android.pass.data.fakes.usecases.TestObserveUpgradeInfo
import proton.android.pass.data.fakes.usecases.TestObserveUserAccessData
import proton.android.pass.data.fakes.usecases.TestObserveVaultsWithItemCount
import proton.android.pass.domain.AliasSuffix
import proton.android.pass.domain.CustomFieldContent
import proton.android.pass.domain.CustomFieldType
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.Plan
import proton.android.pass.domain.PlanLimit
import proton.android.pass.domain.PlanType
import proton.android.pass.domain.ShareFlags
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault
import proton.android.pass.domain.VaultId
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.features.itemcreate.R
import proton.android.pass.features.itemcreate.alias.AliasItemFormState
import proton.android.pass.features.itemcreate.alias.AliasSuffixUiModel
import proton.android.pass.features.itemcreate.alias.CreateAliasViewModel
import proton.android.pass.features.itemcreate.common.CustomFieldDraftRepository
import proton.android.pass.features.itemcreate.common.DraftFormFieldEvent
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.test.CallChecker
import proton.android.pass.test.HiltComponentActivity
import proton.android.pass.test.TestConstants
import proton.android.pass.test.domain.TestItem
import proton.android.pass.test.domain.TestUser
import proton.android.pass.test.waitUntilExists
import proton.android.pass.test.writeTextAndWait
import proton.android.pass.totp.api.TotpSpec
import proton.android.pass.totp.fakes.TestTotpManager
import java.util.Date
import javax.inject.Inject
import proton.android.pass.composecomponents.impl.R as CompR

@HiltAndroidTest
class CreateLoginScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltComponentActivity>()

    @Inject
    lateinit var createItem: TestCreateItem

    @Inject
    lateinit var createItemAndAlias: TestCreateLoginAndAlias

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
    lateinit var observeUserAccessData: TestObserveUserAccessData

    @Inject
    lateinit var draftRepository: TestDraftRepository

    @Inject
    lateinit var customFieldDraftRepository: CustomFieldDraftRepository

    @Inject
    lateinit var totpManager: TestTotpManager

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
                createTime = Date(),
                shareFlags = ShareFlags(0)
            ),
            activeItemCount = 0,
            trashedItemCount = 0
        )
        val totpSpec = TotpSpec(
            secret = "SECRET",
            label = "LABEL".some(),
        )
        totpManager.setParseResult(Result.success(totpSpec))
        observeVaults.sendResult(Result.success(listOf(vault)))
        observeUserAccessData.sendValue(null)
    }


    @Test
    fun canCreateLogin() {
        val title = "Some title"
        val email = "user@email.com"
        val password = "password"
        val totp =
            "otpauth://totp/thisisthelabel?secret=thisisthesecret&algorithm=SHA1&digits=6&period=10"
        val website1 = "somesite.test"
        val website1Full = "https://$website1"
        val website2 = "another.site.withtld"
        val website2Full = "https://$website2"
        val note = "some note"

        val item = TestItem.createLogin(
            title = title,
            username = email,
            note = note,
        )
        totpManager.setSanitisedEditResult(Result.success(totp))
        totpManager.addSanitisedSaveResult(Result.success(totp))
        createItem.sendItem(Result.success(item))

        val checker = CallChecker<Unit>()
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    CreateLoginScreen(
                        clearAlias = false,
                        selectVault = null,
                        canUseAttachments = true,
                        onNavigate = {
                            if (it is BaseLoginNavigation.OnCreateLoginEvent &&
                                it.event is CreateLoginNavigation.LoginCreated
                            ) {
                                checker.call()
                            }
                        }
                    )
                }
            }

            val buttonText = activity.getString(R.string.title_create)
            waitUntilExists(hasText(buttonText))

            // Title
            val titleText = activity.getString(CompR.string.field_title_title)
            onNode(hasText(titleText)).performClick().performScrollTo()
            writeTextAndWait(hasText(titleText), title)

            // Username
            val usernameText = activity.getString(R.string.field_username_or_email_title)
            onNode(hasText(usernameText)).performScrollTo()
            writeTextAndWait(hasText(usernameText), email)

            // Password
            val passwordText = activity.getString(R.string.field_password_title)
            onNode(hasText(passwordText)).performScrollTo()
            writeTextAndWait(hasText(passwordText), password)

            // TOTP
            val totpText = activity.getString(R.string.totp_create_login_field_title)
            onNode(hasText(totpText)).performScrollTo()
            writeTextAndWait(hasText(totpText), totp)

            // Websites
            val websitesText = activity.getString(R.string.field_website_address_title)
            onNode(hasText(websitesText)).performScrollTo()
            writeTextAndWait(hasText(websitesText), website1)

            val addWebsiteText = activity.getString(R.string.field_website_add_another)
            onNode(hasText(addWebsiteText)).performScrollTo().performClick()
            // Done this way because https would collide with the first website
            onNode(hasText("https://")).performTextReplacement(website2)
            waitUntilExists(hasText(website2))

            // Note
            onNodeWithTag(LoginFormTag.LAZY_COLUMN).performScrollToIndex(5)
            val noteText = activity.getString(CompR.string.field_note_title)
            onNode(hasText(noteText)).performScrollTo().performClick()
            writeTextAndWait(hasText(noteText), note)

            onNodeWithTag(LoginFormTag.LAZY_COLUMN).performScrollToIndex(0)
            onNode(hasText(buttonText)).performClick()

            waitUntil { checker.isCalled }
        }

        val memory = createItem.memory()
        assertThat(memory.size).isEqualTo(1)

        val expected = TestCreateItem.Payload(
            userId = USER_ID,
            shareId = ShareId(SHARE_ID),
            itemContents = ItemContents.Login(
                title = title,
                itemEmail = email,
                itemUsername = "",
                note = note,
                customFields = persistentListOf(),
                packageInfoSet = emptySet(),
                primaryTotp = HiddenState.Revealed(TestEncryptionContext.encrypt(totp), totp),
                password = HiddenState.Concealed(TestEncryptionContext.encrypt(password)),
                urls = listOf(website1Full, website2Full),
                passkeys = emptyList()

            )
        )
        assertThat(memory).isEqualTo(listOf(expected))
    }

    @Test
    fun canCreateLoginWithCustomFields() {
        val title = "Login title"
        val textCustomFieldLabel = "Text custom field"
        val textCustomFieldValue = "Text value"
        val hiddenCustomFieldLabel = "Hidden custom field"
        val hiddenCustomFieldValue = "Hidden value"
        val totpCustomFieldLabel = "TOTP custom field"
        val totpCustomFieldValue = "TOTPSECRET"

        createItem.sendItem(Result.success(TestItem.createLogin()))
        totpManager.addSanitisedSaveResult(Result.success(totpCustomFieldValue))

        val textCustomField = CustomFieldContent.Text(
            label = textCustomFieldLabel,
            value = textCustomFieldValue
        )

        val hiddenCustomField = CustomFieldContent.Hidden(
            label = hiddenCustomFieldLabel,
            value = HiddenState.Concealed(
                TestEncryptionContext.encrypt(
                    hiddenCustomFieldValue
                )
            )
        )

        val totpCustomField = CustomFieldContent.Totp(
            label = totpCustomFieldLabel,
            value = HiddenState.Revealed(
                TestEncryptionContext.encrypt(
                    totpCustomFieldValue
                ), totpCustomFieldValue
            )
        )

        val checker = CallChecker<Unit>()
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    CreateLoginScreen(
                        clearAlias = false,
                        selectVault = null,
                        canUseAttachments = true,
                        onNavigate = {
                            if (it is BaseLoginNavigation.OnCreateLoginEvent &&
                                it.event is CreateLoginNavigation.LoginCreated
                            ) {
                                checker.call()
                            }
                        }
                    )
                }
            }

            val buttonText = activity.getString(R.string.title_create)
            waitUntilExists(hasText(buttonText))

            val titleText = activity.getString(CompR.string.field_title_title)
            onNode(hasText(titleText)).performClick().performScrollTo()
            writeTextAndWait(hasText(titleText), title)

            runBlocking {
                customFieldDraftRepository.emit(
                    DraftFormFieldEvent.FieldAdded(
                        sectionIndex = None,
                        label = textCustomField.label,
                        type = CustomFieldType.Text
                    )
                )
            }

            onNodeWithTag(LoginFormTag.LAZY_COLUMN).performScrollToKey("-1/0")
            onNodeWithText(textCustomField.label).performScrollTo().performClick()
            val textPlaceholder =
                activity.getString(R.string.custom_field_text_placeholder)
            onNodeWithText(textPlaceholder).performTextReplacement(textCustomFieldValue)
            runBlocking {
                customFieldDraftRepository.emit(
                    DraftFormFieldEvent.FieldAdded(
                        sectionIndex = None,
                        label = hiddenCustomField.label,
                        type = CustomFieldType.Hidden
                    )
                )
            }
            onNodeWithTag(LoginFormTag.LAZY_COLUMN).performScrollToKey("-1/1")
            waitUntilExists(hasText(hiddenCustomField.label))
            onNodeWithText(hiddenCustomField.label).performScrollTo().performClick()
            val hiddenTextPlaceholder =
                activity.getString(R.string.custom_field_hidden_placeholder)
            onNodeWithText(hiddenTextPlaceholder).performTextReplacement(hiddenCustomFieldValue)
            runBlocking {
                customFieldDraftRepository.emit(
                    DraftFormFieldEvent.FieldAdded(
                        sectionIndex = None,
                        label = totpCustomField.label,
                        type = CustomFieldType.Totp
                    )
                )
            }
            onNodeWithTag(LoginFormTag.LAZY_COLUMN).performScrollToKey("-1/2")
            waitUntilExists(hasText(totpCustomField.label))
            onNodeWithText(totpCustomField.label).performScrollTo().performClick()
            val totpPlaceholder =
                activity.getString(R.string.totp_create_login_field_placeholder)
            onNodeWithText(totpPlaceholder).performTextReplacement(totpCustomFieldValue)
            onNodeWithTag(LoginFormTag.LAZY_COLUMN).performScrollToIndex(0)
            onNode(hasText(titleText)).performClick().performScrollTo()
            // Submit
            onNode(hasText(buttonText)).performClick()

            waitUntil { checker.isCalled }
        }

        val memory = createItem.memory()
        assertThat(memory.size).isEqualTo(1)

        val expectedItemContents = ItemContents.Login(
            title = title,
            itemEmail = "",
            itemUsername = "",
            note = "",
            customFields = persistentListOf(
                textCustomField,
                hiddenCustomField,
                totpCustomField
            ),
            packageInfoSet = emptySet(),
            primaryTotp = HiddenState.Empty(TestEncryptionContext.encrypt("")),
            password = HiddenState.Empty(TestEncryptionContext.encrypt("")),
            urls = emptyList(),
            passkeys = emptyList()
        )
        val received = memory.first()
        val receivedItemContents =  received.itemContents as  ItemContents.Login
        assertThat(received.userId).isEqualTo(USER_ID)
        assertThat(received.shareId).isEqualTo(ShareId(SHARE_ID))
        assertThat(receivedItemContents.title).isEqualTo(title)
        assertThat(receivedItemContents.note).isEqualTo("")
        assertThat(receivedItemContents.itemEmail).isEqualTo("")
        assertThat(receivedItemContents.itemUsername).isEqualTo("")
        assertThat(receivedItemContents.customFields).containsAtLeastElementsIn(expectedItemContents.customFields)
        assertThat(receivedItemContents.packageInfoSet).isEqualTo(expectedItemContents.packageInfoSet)
        assertThat(receivedItemContents.primaryTotp).isEqualTo(expectedItemContents.primaryTotp)
        assertThat(receivedItemContents.password).isEqualTo(expectedItemContents.password)
        assertThat(receivedItemContents.urls).containsAtLeastElementsIn(expectedItemContents.urls)
        assertThat(receivedItemContents.passkeys).isEqualTo(expectedItemContents.passkeys)
    }

    @Test
    fun clickOnAliasRedirectsToAliasBottomSheet() {
        val checker = CallChecker<Unit>()
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    CreateLoginScreen(
                        clearAlias = false,
                        selectVault = null,
                        canUseAttachments = true,
                        onNavigate = {
                            if (it is BaseLoginNavigation.CreateAlias) {
                                checker.call()
                            }
                        }
                    )
                }
            }

            val buttonText = activity.getString(R.string.title_create)
            waitUntilExists(hasText(buttonText))

            val usernameText = activity.getString(R.string.field_username_or_email_title)
            onNodeWithText(usernameText).performClick()

            val createAliasText = activity.getString(R.string.sticky_button_create_alias)
            waitUntilExists(hasText(createAliasText))
            onNodeWithText(createAliasText).performClick()

            waitUntil { checker.isCalled }
        }
    }

    @Test
    fun clickOnUserEmailAutofillsUserEmail() {
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    CreateLoginScreen(
                        clearAlias = false,
                        selectVault = null,
                        canUseAttachments = true,
                        onNavigate = {}
                    )
                }
            }

            val buttonText = activity.getString(R.string.title_create)
            waitUntilExists(hasText(buttonText))

            val usernameText = activity.getString(R.string.field_username_or_email_title)
            onNodeWithText(usernameText).performClick()

            val fillUsernameText =
                activity.getString(R.string.sticky_button_use_account_email, USER_EMAIL)
            waitUntilExists(hasText(fillUsernameText))
            onNodeWithText(fillUsernameText).performClick()

            waitUntilExists(hasText(USER_EMAIL))
        }
    }

    @Test
    fun cannotCreateLoginWithoutTitle() {
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    CreateLoginScreen(
                        clearAlias = false,
                        selectVault = null,
                        canUseAttachments = true,
                        onNavigate = {},
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
                    CreateLoginScreen(
                        clearAlias = false,
                        selectVault = null,
                        canUseAttachments = true,
                        onNavigate = {
                            if (it == BaseLoginNavigation.CloseScreen) {
                                checker.call()
                            }
                        }
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

    @Test
    fun clickOnUpgradeRedirectsToUpgrade() {
        setupPlan(TestConstants.FreePlanType, totpLimit = PlanLimit.Limited(0))
        val checker = CallChecker<Unit>()
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    CreateLoginScreen(
                        clearAlias = false,
                        selectVault = null,
                        canUseAttachments = true,
                        onNavigate = {
                            if (it == BaseLoginNavigation.Upgrade) {
                                checker.call()
                            }
                        }
                    )
                }
            }

            val buttonText = activity.getString(R.string.title_create)
            waitUntilExists(hasText(buttonText))

            val upgradeText = activity.getString(R.string.upgrade)
            onNodeWithText(upgradeText).performScrollTo().performClick()

            waitUntil { checker.isCalled }
        }
    }

    @Test
    fun clickOnAddCustomFieldRedirectsToCustomField() {
        val checker = CallChecker<Unit>()
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    CreateLoginScreen(
                        clearAlias = false,
                        selectVault = null,
                        canUseAttachments = true,
                        onNavigate = {
                            if (it == BaseLoginNavigation.AddCustomField) {
                                checker.call()
                            }
                        }
                    )
                }
            }

            val buttonText = activity.getString(R.string.title_create)
            waitUntilExists(hasText(buttonText))
            onNodeWithTag(LoginFormTag.LAZY_COLUMN)
                .performScrollToKey("-1/add_custom_field")
            val addCustomFieldText =
                activity.getString(R.string.add_custom_field_button)
            onNodeWithText(addCustomFieldText).performScrollTo().performClick()
            waitUntil { checker.isCalled }
        }
    }

    @Test
    fun callsCreateItemAliasIfAliasIsToBeCreated() {
        val title = "Item title"
        val checker = CallChecker<Unit>()

        val aliasItemFormState = AliasItemFormState(
            selectedSuffix = AliasSuffixUiModel(
                aliasSuffix = AliasSuffix(
                    suffix = "some.suffix@test.random",
                    signedSuffix = "",
                    isCustom = false,
                    isPremium = false,
                    domain = "test.random"
                )
            ),
            customFields = emptyList()
        )

        draftRepository.save(CreateAliasViewModel.KEY_DRAFT_ALIAS, aliasItemFormState)
        createItemAndAlias.setResult(Result.success(TestItem.createLogin()))

        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    CreateLoginScreen(
                        clearAlias = false,
                        selectVault = null,
                        canUseAttachments = true,
                        onNavigate = {
                            if (it is BaseLoginNavigation.OnCreateLoginEvent &&
                                it.event is CreateLoginNavigation.LoginCreated
                            ) {
                                checker.call()
                            }
                        },
                    )
                }
            }

            val buttonText = activity.getString(R.string.title_create)
            waitUntilExists(hasText(buttonText))

            val titleText = activity.getString(CompR.string.field_title_title)
            onNode(hasText(titleText)).performClick().performScrollTo()
            writeTextAndWait(hasText(titleText), title)

            onNode(hasText(buttonText)).performClick()

            waitUntil { checker.isCalled }
        }

        assertThat(createItemAndAlias.hasBeenInvoked()).isTrue()
        assertThat(createItem.memory()).isEmpty()
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
    }

}
