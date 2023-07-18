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

package proton.android.pass.commonui.api

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.some
import java.lang.ref.WeakReference

fun Context.findActivity(): Option<Activity> {
    var context = this
    while (context is ContextWrapper) {
        when (context) {
            is Activity -> return context.some()
            else -> context = context.baseContext
        }
    }
    return None
}

fun Context.toClassHolder() = ClassHolder(Some(WeakReference(this)))
fun Activity.toClassHolder() = ClassHolder(Some(WeakReference(this)))
