package proton.android.pass.featurehome.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.autofill.api.AutofillStatus
import proton.android.pass.autofill.api.AutofillSupportedStatus
import proton.android.pass.autofill.fakes.TestAutofillManager
import proton.android.pass.preferences.HasDismissedAutofillBanner
import proton.android.pass.preferences.TestPreferenceRepository
import proton.android.pass.test.MainDispatcherRule

class AutofillCardViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: AutofillCardViewModel
    private lateinit var preferenceRepository: TestPreferenceRepository
    private lateinit var autofillManager: TestAutofillManager

    @Before
    fun setUp() {
        preferenceRepository = TestPreferenceRepository()
        autofillManager = TestAutofillManager()
    }

    @Test
    fun `Should not show banner if autofill is unsupported`() = runTest {
        autofillManager.emitStatus(AutofillSupportedStatus.Unsupported)
        preferenceRepository.setHasDismissedAutofillBanner(HasDismissedAutofillBanner.NotDismissed)
        viewModel = AutofillCardViewModel(autofillManager, preferenceRepository)
        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(false)
        }
    }

    @Test
    fun `Should not show banner if autofill is enabled by our service`() = runTest {
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.EnabledByOurService))
        preferenceRepository.setHasDismissedAutofillBanner(HasDismissedAutofillBanner.NotDismissed)
        viewModel = AutofillCardViewModel(autofillManager, preferenceRepository)
        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(false)
        }
    }

    @Test
    fun `Should show banner if autofill is enabled by other service`() = runTest {
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.EnabledByOtherService))
        preferenceRepository.setHasDismissedAutofillBanner(HasDismissedAutofillBanner.NotDismissed)
        viewModel = AutofillCardViewModel(autofillManager, preferenceRepository)
        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(true)
        }
    }

    @Test
    fun `Should show banner if autofill is disabled `() = runTest {
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.Disabled))
        preferenceRepository.setHasDismissedAutofillBanner(HasDismissedAutofillBanner.NotDismissed)
        viewModel = AutofillCardViewModel(autofillManager, preferenceRepository)
        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(true)
        }
    }

    @Test
    fun `Should not show banner if autofill banner has been dismised`() = runTest {
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.Disabled))
        preferenceRepository.setHasDismissedAutofillBanner(HasDismissedAutofillBanner.Dismissed)
        viewModel = AutofillCardViewModel(autofillManager, preferenceRepository)
        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(false)
        }
    }
}
