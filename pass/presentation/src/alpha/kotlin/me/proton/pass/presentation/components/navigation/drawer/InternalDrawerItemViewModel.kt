package me.proton.pass.presentation.components.navigation.drawer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.proton.android.pass.appconfig.api.AppConfig
import me.proton.android.pass.log.api.LogSharing
import javax.inject.Inject

@HiltViewModel
class InternalDrawerItemViewModel @Inject constructor(
    private val appConfig: AppConfig,
    private val logSharing: LogSharing
) : ViewModel() {

    fun shareLogCatOutput(context: Context) = viewModelScope.launch(Dispatchers.IO) {
        logSharing.shareLogs(appConfig.applicationId, context)
    }
}
