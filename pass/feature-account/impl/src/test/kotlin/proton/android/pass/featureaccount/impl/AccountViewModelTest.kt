package proton.android.pass.featureaccount.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.UserPlan
import proton.android.pass.data.fakes.usecases.TestGetUserPlan
import proton.android.pass.data.fakes.usecases.TestObserveCurrentUser
import proton.android.pass.notifications.fakes.TestSnackbarDispatcher
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.TestUser

class AccountViewModelTest {

    @get:Rule
    val dispatcher = MainDispatcherRule()

    private lateinit var instance: AccountViewModel
    private lateinit var observeCurrentUser: TestObserveCurrentUser
    private lateinit var getUserPlan: TestGetUserPlan
    private lateinit var snackbarDispatcher: TestSnackbarDispatcher

    @Before
    fun setup() {
        observeCurrentUser = TestObserveCurrentUser()
        getUserPlan = TestGetUserPlan()
        snackbarDispatcher = TestSnackbarDispatcher()

        instance = AccountViewModel(
            observeCurrentUser = observeCurrentUser,
            getUserPlan = getUserPlan,
            snackbarDispatcher = snackbarDispatcher
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
        val plan = UserPlan.Paid(internal = "internal", humanReadable = "testplan")

        val user = TestUser.create(email = email)
        observeCurrentUser.sendUser(user)
        getUserPlan.setResult(Result.success(plan))

        instance.state.test {
            val item = awaitItem()
            assertThat(item.isLoadingState).isEqualTo(IsLoadingState.NotLoading)
            assertThat(item.email).isEqualTo(email)
            assertThat(item.plan).isEqualTo(PlanSection.Data(plan.humanReadable))
        }
    }

    @Test
    fun `emits snackbar message on error getting user plan`() = runTest {
        val email = "test@email.local"
        val user = TestUser.create(email = email)
        observeCurrentUser.sendUser(user)
        getUserPlan.setResult(Result.failure(IllegalStateException("test")))

        instance.state.test {
            val item = awaitItem()
            assertThat(item.isLoadingState).isEqualTo(IsLoadingState.NotLoading)
            assertThat(item.email).isEqualTo(email)
            assertThat(item.plan).isEqualTo(PlanSection.Hide)
        }

        val message = snackbarDispatcher.snackbarMessage.first().value()
        assertThat(message).isEqualTo(AccountSnackbarMessage.GetUserInfoError)
    }

}
