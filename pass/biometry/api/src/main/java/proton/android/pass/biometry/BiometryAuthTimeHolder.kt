package proton.android.pass.biometry

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import proton.android.pass.common.api.Option

interface BiometryAuthTimeHolder {
    fun getBiometryAuthTime(): Flow<Option<Instant>>
    fun storeBiometryAuthTime(instant: Instant)
}
