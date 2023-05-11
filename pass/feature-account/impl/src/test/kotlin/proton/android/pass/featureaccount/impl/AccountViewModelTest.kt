package proton.android.pass.featureaccount.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.UpgradeInfo
import proton.android.pass.data.fakes.usecases.TestObserveUpgradeInfo
import proton.android.pass.data.fakes.usecases.TestObserveCurrentUser
import proton.android.pass.notifications.fakes.TestSnackbarDispatcher
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.TestUser
import proton.pass.domain.Plan
import proton.pass.domain.PlanType

class AccountViewModelTest {

    @get:Rule
    val dispatcher = MainDispatcherRule()

    private lateinit var instance: AccountViewModel
    private lateinit var observeCurrentUser: TestObserveCurrentUser
    private lateinit var getUpgradeInfo: TestObserveUpgradeInfo
    private lateinit var snackbarDispatcher: TestSnackbarDispatcher

    @Before
    fun setup() {
        observeCurrentUser = TestObserveCurrentUser()
        getUpgradeInfo = TestObserveUpgradeInfo()
        snackbarDispatcher = TestSnackbarDispatcher()

        instance = AccountViewModel(
            observeCurrentUser = observeCurrentUser,
            observeUpgradeInfo = getUpgradeInfo
        )
    }

    @Test
    fun `emits initial state`() = runTest {
        instance.state.test {
            assertThat(awaitItem()).isEqualTo(AccountUiState.Initial)
        }
    }

    @Test
    fun `emits user email and plan`() = runTest {
        val email = "test@email.local"
        val planType = PlanType.Paid(internal = "internal", humanReadable = "testplan")
        val plan = Plan(planType = planType, vaultLimit = 0, aliasLimit = 0, totpLimit = 0)
        val user = TestUser.create(email = email)
        observeCurrentUser.sendUser(user)
        getUpgradeInfo.setResult(
            UpgradeInfo(
                plan = plan,
                isUpgradeAvailable = false,
                totalVaults = 0,
                totalAlias = 0,
                totalTotp = 0
            )
        )

        instance.state.test {
            val item = awaitItem()
            assertThat(item.isLoadingState).isEqualTo(IsLoadingState.NotLoading)
            assertThat(item.email).isEqualTo(email)
            assertThat(item.plan).isEqualTo(PlanSection.Data(planType.humanReadable))
        }
    }
}
