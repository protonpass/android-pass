package proton.android.pass.biometry

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import proton.android.pass.common.api.Option
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.InternalSettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometryAuthTimeHolderImpl @Inject constructor(
    private val internalSettingsRepository: InternalSettingsRepository
) : BiometryAuthTimeHolder {

    override fun getBiometryAuthTime(): Flow<Option<Instant>> =
        internalSettingsRepository.getLastUnlockedTime()

    override fun storeBiometryAuthTime(instant: Instant) {
        runBlocking {
            internalSettingsRepository.setLastUnlockedTime(instant)
                .onFailure {
                    PassLogger.w(TAG, it, "Error storing last unlocked time")
                }
        }
    }

    companion object {
        private const val TAG = "BiometryAuthTimeHolderImpl"
    }
}
