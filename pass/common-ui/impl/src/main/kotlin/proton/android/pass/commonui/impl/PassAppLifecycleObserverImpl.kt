/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.commonui.impl

import android.os.Looper
import androidx.core.os.HandlerCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import proton.android.pass.commonui.api.PassAppLifecycleProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PassAppLifecycleObserverImpl internal constructor(
    mainLooper: Looper,
    processLifecycleOwner: LifecycleOwner
) : PassAppLifecycleProvider, DefaultLifecycleObserver {

    private val mutableState = MutableStateFlow(PassAppLifecycleProvider.State.Background)

    override val lifecycle: Lifecycle by lazy {
        processLifecycleOwner.lifecycle
    }

    override val state: StateFlow<PassAppLifecycleProvider.State> = mutableState.asStateFlow()

    init {
        HandlerCompat.createAsync(mainLooper).post {
            lifecycle.addObserver(this)
        }
    }

    @Inject
    constructor() : this(Looper.getMainLooper(), ProcessLifecycleOwner.get())

    override fun onStart(owner: LifecycleOwner) {
        mutableState.tryEmit(PassAppLifecycleProvider.State.Foreground)
    }

    override fun onStop(owner: LifecycleOwner) {
        mutableState.tryEmit(PassAppLifecycleProvider.State.Background)
    }
}
