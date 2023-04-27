package proton.android.pass.biometry

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.Instant
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.some
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometryAuthTimeHolderImpl @Inject constructor() : BiometryAuthTimeHolder {

    private val authTimeFlow: MutableStateFlow<Option<Instant>> = MutableStateFlow(None)

    override fun getBiometryAuthTime(): Flow<Option<Instant>> = authTimeFlow

    override fun storeBiometryAuthTime(instant: Instant) {
        authTimeFlow.tryEmit(instant.some())
    }

}
