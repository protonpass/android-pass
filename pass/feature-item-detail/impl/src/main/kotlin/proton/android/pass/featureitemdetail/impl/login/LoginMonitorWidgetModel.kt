/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.featureitemdetail.impl.login

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import proton.android.pass.featureitemdetail.impl.R
import me.proton.core.presentation.R as CoreR

@Stable
internal sealed class LoginMonitorWidgetModel {

    internal abstract val titleResId: Int

    internal abstract val subtitleResId: Int

    internal abstract val isExcludedFromMonitor: Boolean

    internal val menuActionTextResId: Int by lazy {
        if (isExcludedFromMonitor) {
            R.string.login_item_monitor_widget_menu_action_include
        } else {
            R.string.login_item_monitor_widget_menu_action_exclude
        }
    }

    internal val menuActionIconResId: Int by lazy {
        if (isExcludedFromMonitor) {
            CoreR.drawable.ic_proton_eye
        } else {
            CoreR.drawable.ic_proton_eye_slash
        }
    }

    internal val menuActionEvent: LoginDetailEvent by lazy {
        if (isExcludedFromMonitor) {
            LoginDetailEvent.OnIncludeItemInMonitoring
        } else {
            LoginDetailEvent.OnExcludeItemFromMonitoring
        }
    }

    @Stable
    internal data class ReusedPassword(
        @StringRes override val titleResId: Int,
        @StringRes override val subtitleResId: Int,
        override val isExcludedFromMonitor: Boolean
    ) : LoginMonitorWidgetModel()

    @Stable
    internal data class WeakPassword(
        @StringRes override val titleResId: Int,
        @StringRes override val subtitleResId: Int,
        override val isExcludedFromMonitor: Boolean
    ) : LoginMonitorWidgetModel()

}
