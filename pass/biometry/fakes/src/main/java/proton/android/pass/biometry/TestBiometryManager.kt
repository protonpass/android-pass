package proton.android.pass.biometry

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestBiometryManager @Inject constructor() : BiometryManager {

    private var biometryStatus: BiometryStatus = BiometryStatus.CanAuthenticate

    private val resultFlow = MutableSharedFlow<BiometryResult>(
        replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    fun setBiometryStatus(status: BiometryStatus) {
        biometryStatus = status
    }

    fun emitResult(result: BiometryResult) {
        resultFlow.tryEmit(result)
    }

    override fun getBiometryStatus(): BiometryStatus = biometryStatus

    override fun launch(context: ContextHolder): Flow<BiometryResult> = resultFlow

}
