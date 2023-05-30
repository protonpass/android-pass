package proton.android.pass.featureitemcreate.impl.login

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.account.fakes.TestAccountManager
import proton.android.pass.clipboard.fakes.TestClipboardManager
import proton.android.pass.crypto.fakes.context.TestEncryptionContext
import proton.android.pass.crypto.fakes.context.TestEncryptionContextProvider
import proton.android.pass.data.fakes.repositories.TestDraftRepository
import proton.android.pass.data.fakes.usecases.TestCreateAlias
import proton.android.pass.data.fakes.usecases.TestGetItemById
import proton.android.pass.data.fakes.usecases.TestObserveCurrentUser
import proton.android.pass.data.fakes.usecases.TestObserveItems
import proton.android.pass.data.fakes.usecases.TestObserveUpgradeInfo
import proton.android.pass.data.fakes.usecases.TestUpdateItem
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.notifications.fakes.TestSnackbarDispatcher
import proton.android.pass.preferences.TestFeatureFlagsPreferenceRepository
import proton.android.pass.telemetry.fakes.TestTelemetryManager
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.TestSavedStateHandle
import proton.android.pass.test.domain.TestUser
import proton.android.pass.totp.api.TotpSpec
import proton.android.pass.totp.fakes.TestTotpManager
import proton.pass.domain.HiddenState
import proton.pass.domain.ItemContents

class UpdateLoginViewModelTest {

    @get:Rule
    val dispatcher = MainDispatcherRule()

    private lateinit var instance: UpdateLoginViewModel

    private lateinit var getItemById: TestGetItemById
    private lateinit var totpManager: TestTotpManager

    @Before
    fun setup() {
        getItemById = TestGetItemById()
        totpManager = TestTotpManager()

        instance = UpdateLoginViewModel(
            getItemById = getItemById,
            accountManager = TestAccountManager().apply {
                sendPrimaryUserId(UserId("UserId"))
            },
            clipboardManager = TestClipboardManager(),
            totpManager = totpManager,
            snackbarDispatcher = TestSnackbarDispatcher(),
            savedStateHandle = TestSavedStateHandle.create().apply {
                set(CommonOptionalNavArgId.ShareId.key, SHARE_ID)
                set(CommonNavArgId.ItemId.key, ITEM_ID)
            },
            encryptionContextProvider = TestEncryptionContextProvider(),
            observeCurrentUser = TestObserveCurrentUser().apply { sendUser(TestUser.create()) },
            telemetryManager = TestTelemetryManager(),
            draftRepository = TestDraftRepository(),
            observeUpgradeInfo = TestObserveUpgradeInfo(),
            updateItem = TestUpdateItem(),
            createAlias = TestCreateAlias(),
            ffRepo = TestFeatureFlagsPreferenceRepository()
        )
    }

    @Test
    fun `item with totp using default parameters shows only secret`() = runTest {
        val secret = "secret"
        val uri = "otpauth://totp/label?secret=$secret&algorithm=SHA1&period=30&digits=6"
        val primaryTotp = HiddenState.Revealed("", uri)
        val item = TestObserveItems.createItem(
            itemContents = ItemContents.Login(
                title = "item",
                note = "note",
                username = "username",
                password = HiddenState.Revealed("", "password"),
                urls = emptyList(),
                packageInfoSet = emptySet(),
                primaryTotp = primaryTotp,
                customFields = emptyList()
            )
        )
        totpManager.setParseResult(Result.success(TotpSpec(secret = secret, label = "label")))
        getItemById.emitValue(Result.success(item))

        instance.updateLoginUiState.test {
            val state = awaitItem()
            assertThat(state.baseLoginUiState.contents.primaryTotp).isEqualTo(primaryTotp)
        }
    }

    @Test
    fun `item with totp using non-default parameters shows full URI`() = runTest {
        val secret = "secret"
        val uri = "otpauth://totp/label?secret=$secret&algorithm=SHA256&period=10&digits=8"
        val primaryTotp = HiddenState.Revealed(TestEncryptionContext.encrypt(uri), uri)
        val item = TestObserveItems.createItem(
            itemContents = ItemContents.Login(
                title = "item",
                note = "note",
                username = "username",
                password = HiddenState.Revealed(
                    TestEncryptionContext.encrypt("password"),
                    "password"
                ),
                urls = emptyList(),
                packageInfoSet = emptySet(),
                primaryTotp = primaryTotp,
                customFields = emptyList()
            )
        )
        getItemById.emitValue(Result.success(item))

        instance.updateLoginUiState.test {
            val state = awaitItem()
            assertThat(state.baseLoginUiState.contents.primaryTotp).isEqualTo(primaryTotp)
        }
    }

    companion object {
        private const val SHARE_ID = "shareId"
        private const val ITEM_ID = "itemId"
    }

}
