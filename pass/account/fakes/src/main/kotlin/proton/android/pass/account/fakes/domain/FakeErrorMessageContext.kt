package proton.android.pass.account.fakes.domain

import me.proton.core.domain.arch.ErrorMessageContext
import javax.inject.Inject

class FakeErrorMessageContext @Inject constructor() : ErrorMessageContext {
    override fun getUserMessage(throwable: Throwable): String? = null
    override fun getUserMessageOrDefault(throwable: Throwable): String = "FakeErrorMessageContext-error"
}
