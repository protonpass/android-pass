package me.proton.android.pass.ui.drawer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import me.proton.android.pass.BuildConfig
import me.proton.android.pass.R
import me.proton.core.pass.domain.usecases.ObserveCurrentUser
import me.proton.core.pass.presentation.components.navigation.drawer.DrawerUiState
import me.proton.core.pass.presentation.components.navigation.drawer.NavigationDrawerSection
import javax.inject.Inject

@HiltViewModel
class DrawerViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUser
) : ViewModel() {

    private val currentUserFlow = observeCurrentUser().filterNotNull()
    private val drawerSectionState: MutableStateFlow<NavigationDrawerSection> =
        MutableStateFlow(NavigationDrawerSection.Items)

    val drawerUiState: StateFlow<DrawerUiState> = combine(
        currentUserFlow,
        drawerSectionState
    ) { user, sectionState ->
        DrawerUiState(
            appNameResId = R.string.app_name,
            appVersion = BuildConfig.VERSION_NAME,
            currentUser = user,
            selectedSection = sectionState
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DrawerUiState(
                appNameResId = R.string.app_name,
                appVersion = BuildConfig.VERSION_NAME
            )
        )

    fun onDrawerSectionChanged(drawerSection: NavigationDrawerSection) {
        drawerSectionState.value = drawerSection
    }
}
