package me.proton.android.pass.biometry

import kotlinx.coroutines.flow.Flow

interface BiometryManager {
    fun getBiometryStatus(): BiometryStatus
    fun launch(context: ContextHolder): Flow<BiometryResult>
}
