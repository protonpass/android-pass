package proton.android.pass.account.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.proton.core.usersettings.domain.entity.DeviceSettings
import me.proton.core.usersettings.domain.repository.DeviceSettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestDeviceSettingsRepository @Inject constructor() : DeviceSettingsRepository {

    private val deviceSettingsFlow = MutableStateFlow(
        DeviceSettings(
            isTelemetryEnabled = true, isCrashReportEnabled = true
        )
    )

    override suspend fun getDeviceSettings(): DeviceSettings = deviceSettingsFlow.value

    override fun observeDeviceSettings(): Flow<DeviceSettings> = deviceSettingsFlow

    override suspend fun updateIsCrashReportEnabled(isEnabled: Boolean) {
        deviceSettingsFlow.update { it.copy(isCrashReportEnabled = isEnabled) }
    }

    override suspend fun updateIsTelemetryEnabled(isEnabled: Boolean) {
        deviceSettingsFlow.update { it.copy(isTelemetryEnabled = isEnabled) }
    }
}
