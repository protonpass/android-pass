package proton.android.pass.featureitemdetail.impl.login

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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.clipboard.fakes.TestClipboardManager
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.fakes.TestSavedStateHandleProvider
import proton.android.pass.data.api.usecases.ItemWithVaultInfo
import proton.android.pass.data.fakes.usecases.TestGetItemById
import proton.android.pass.data.fakes.usecases.TestGetItemByIdWithVault
import proton.android.pass.data.fakes.usecases.TestObserveItems
import proton.android.pass.featureitemdetail.impl.ItemDetailNavigation
import proton.android.pass.featureitemdetail.impl.ItemDetailScreen
import proton.android.pass.featureitemdetail.impl.R
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.test.CallChecker
import proton.android.pass.test.HiltComponentActivity
import proton.android.pass.test.waitUntilExists
import proton.android.pass.totp.api.TotpManager
import proton.android.pass.totp.fakes.TestObserveTotpFromUri
import proton.pass.domain.ItemContents
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import proton.pass.domain.Vault
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
    lateinit var getItemById: TestGetItemById

    @Inject
    lateinit var getItemByIdWithVault: TestGetItemByIdWithVault

    @Inject
    lateinit var clipboardManager: TestClipboardManager

    @Inject
    lateinit var observeTotp: TestObserveTotpFromUri

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
        val username = "username"
        val note = "some note for the item"
        performSetup(title = itemTitle, username = username, note = note)
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    ItemDetailScreen(
                        onNavigate = {}
                    )
                }
            }

            waitUntilExists(hasText(itemTitle))

            onNode(hasText(itemTitle)).assertExists()
            onNode(hasText(username)).assertExists()
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
                    ItemDetailScreen(
                        onNavigate = {}
                    )
                }
            }

            waitUntilExists(hasText(title))

            val concealedContentDescription = activity.getString(R.string.action_reveal_password)
            val revealedContentDescription = activity.getString(R.string.action_conceal_password)

            onNodeWithContentDescription(concealedContentDescription).performClick()
            waitUntilExists(hasText(password))

            onNodeWithContentDescription(revealedContentDescription).performClick()
            onNode(hasText(password)).assertDoesNotExist()
        }
    }

    @Test
    fun clickUsernameCopiesUsername() {
        val username = "some_username"
        performSetup(username = username)
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    ItemDetailScreen(
                        onNavigate = {}
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
                    ItemDetailScreen(
                        onNavigate = {}
                    )
                }
            }

            waitUntilExists(hasText(title))

            val passwordLabel = activity.getString(R.string.field_password)
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
                    ItemDetailScreen(
                        onNavigate = {}
                    )
                }
            }

            waitUntilExists(hasText(title))

            val label = activity.getString(R.string.totp_section_title)
            onNode(hasText(label)).performClick()
            assertEquals(totpCode, clipboardManager.getContents())
        }
    }

    @Test
    fun navigateToEdit() {
        val title = performSetup()
        val checker = CallChecker<Pair<ShareId, ItemId>>()
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    ItemDetailScreen(
                        onNavigate = {
                            if (it is ItemDetailNavigation.OnEdit) {
                                checker.call(it.shareId to it.itemId)
                            }
                        }
                    )
                }
            }

            waitUntilExists(hasText(title))

            onNode(hasText(activity.getString(R.string.top_bar_edit_button_text))).performClick()
            waitUntil { checker.isCalled }
            assertEquals(SHARE_ID, checker.memory?.first?.id)
            assertEquals(ITEM_ID, checker.memory?.second?.id)
        }
    }

    private fun performSetup(
        title: String = "some title",
        username: String = "someusername",
        password: String = "password",
        note: String = "a note",
        urls: List<String> = emptyList(),
        primaryTotp: String = "",
        vaultName: String = "vault",
        hasManyVaults: Boolean = false
    ): String {
        val item = TestObserveItems.createItem(
            shareId = ShareId(SHARE_ID),
            itemId = ItemId(ITEM_ID),
            itemContents = ItemContents.Login(
                title = title,
                username = username,
                password = password,
                note = note,
                urls = urls,
                packageInfoSet = emptySet(),
                primaryTotp = primaryTotp,
                customFields = emptyList()
            )
        )

        val withVault = ItemWithVaultInfo(
            item = item,
            vault = Vault(
                shareId = ShareId(SHARE_ID),
                name = vaultName,
                isPrimary = true
            ),
            hasMoreThanOneVault = hasManyVaults
        )
        getItemByIdWithVault.emitValue(Result.success(withVault))
        getItemById.emitValue(Result.success(item))

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

