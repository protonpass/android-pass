package proton.android.pass.biometry

import kotlinx.coroutines.flow.Flow

interface NeedsBiometricAuth {
    operator fun invoke(): Flow<Boolean>
}
