package proton.android.pass.featurehome.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.autofill.api.AutofillStatus
import proton.android.pass.autofill.api.AutofillSupportedStatus
import proton.android.pass.autofill.fakes.TestAutofillManager
import proton.android.pass.data.fakes.usecases.TestGetUserPlan
import proton.android.pass.featurehome.impl.onboardingtips.OnBoardingTipPage.AUTOFILL
import proton.android.pass.featurehome.impl.onboardingtips.OnBoardingTipPage.TRIAL
import proton.android.pass.featurehome.impl.onboardingtips.OnBoardingTipsUiState
import proton.android.pass.featurehome.impl.onboardingtips.OnBoardingTipsViewModel
import proton.android.pass.preferences.HasDismissedAutofillBanner
import proton.android.pass.preferences.HasDismissedTrialBanner
import proton.android.pass.preferences.TestPreferenceRepository
import proton.android.pass.test.MainDispatcherRule
import proton.pass.domain.Plan
import proton.pass.domain.PlanType

class OnBoardingTipsViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: OnBoardingTipsViewModel
    private lateinit var preferenceRepository: TestPreferenceRepository
    private lateinit var autofillManager: TestAutofillManager
    private lateinit var getUserPlan: TestGetUserPlan

    @Before
    fun setUp() {
        preferenceRepository = TestPreferenceRepository()
        autofillManager = TestAutofillManager()
        getUserPlan = TestGetUserPlan()
    }

    @Test
    fun `Should not show banner if autofill is unsupported`() = runTest {
        setupPlan()
        autofillManager.emitStatus(AutofillSupportedStatus.Unsupported)
        preferenceRepository.setHasDismissedAutofillBanner(HasDismissedAutofillBanner.NotDismissed)
        viewModel = OnBoardingTipsViewModel(autofillManager, preferenceRepository, getUserPlan)
        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(OnBoardingTipsUiState())
        }
    }

    @Test
    fun `Should not show banner if autofill is enabled by our service`() = runTest {
        setupPlan()
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.EnabledByOurService))
        preferenceRepository.setHasDismissedAutofillBanner(HasDismissedAutofillBanner.NotDismissed)
        viewModel = OnBoardingTipsViewModel(autofillManager, preferenceRepository, getUserPlan)
        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(OnBoardingTipsUiState())
        }
    }

    @Test
    fun `Should show banner if autofill is enabled by other service`() = runTest {
        setupPlan()
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.EnabledByOtherService))
        preferenceRepository.setHasDismissedAutofillBanner(HasDismissedAutofillBanner.NotDismissed)
        viewModel = OnBoardingTipsViewModel(autofillManager, preferenceRepository, getUserPlan)
        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(OnBoardingTipsUiState(persistentSetOf(AUTOFILL)))
        }
    }

    @Test
    fun `Should show banner if autofill is disabled `() = runTest {
        setupPlan()
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.Disabled))
        preferenceRepository.setHasDismissedAutofillBanner(HasDismissedAutofillBanner.NotDismissed)
        viewModel = OnBoardingTipsViewModel(autofillManager, preferenceRepository, getUserPlan)
        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(OnBoardingTipsUiState(persistentSetOf(AUTOFILL)))
        }
    }

    @Test
    fun `Should not show banner if autofill banner has been dismised`() = runTest {
        setupPlan()
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.Disabled))
        preferenceRepository.setHasDismissedAutofillBanner(HasDismissedAutofillBanner.Dismissed)
        viewModel = OnBoardingTipsViewModel(autofillManager, preferenceRepository, getUserPlan)
        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(OnBoardingTipsUiState())
        }
    }

    @Test
    fun `Should display trial banner if plan is trial`() = runTest {
        setupPlan(PlanType.Trial("", "", 1))
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.Disabled))
        preferenceRepository.setHasDismissedAutofillBanner(HasDismissedAutofillBanner.NotDismissed)
        preferenceRepository.setHasDismissedTrialBanner(HasDismissedTrialBanner.NotDismissed)
        viewModel = OnBoardingTipsViewModel(autofillManager, preferenceRepository, getUserPlan)
        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(OnBoardingTipsUiState(persistentSetOf(TRIAL)))
        }
    }

    @Test
    fun `Should display autofill banner when trial is dismissed`() = runTest {
        setupPlan(PlanType.Trial("", "", 1))
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.Disabled))
        preferenceRepository.setHasDismissedAutofillBanner(HasDismissedAutofillBanner.NotDismissed)
        preferenceRepository.setHasDismissedTrialBanner(HasDismissedTrialBanner.NotDismissed)
        viewModel = OnBoardingTipsViewModel(autofillManager, preferenceRepository, getUserPlan)

        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(OnBoardingTipsUiState(persistentSetOf(TRIAL)))

            viewModel.onDismiss(TRIAL)

            assertThat(awaitItem()).isEqualTo(OnBoardingTipsUiState(persistentSetOf(AUTOFILL)))
        }
    }

    private fun setupPlan(
        planType: PlanType = PlanType.Paid("", "")
    ) {
        val plan = Plan(
            planType = planType,
            hideUpgrade = false,
            vaultLimit = 1,
            aliasLimit = 1,
            totpLimit = 1,
            updatedAt = 123
        )
        getUserPlan.setResult(Result.success(plan))
    }
}
