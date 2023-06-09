package proton.android.pass.biometry

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestNeedsBiometricAuth @Inject constructor() : NeedsBiometricAuth {

    private val resultFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    fun sendValue(value: Boolean) {
        resultFlow.tryEmit(value)
    }

    override fun invoke(): Flow<Boolean> = resultFlow

}
