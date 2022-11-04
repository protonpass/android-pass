package me.proton.android.pass.biometry

import android.content.Context
import kotlinx.coroutines.flow.Flow

interface BiometryManager {
    fun getBiometryStatus(): BiometryStatus
    fun launch(context: Context): Flow<BiometryResult>
}
