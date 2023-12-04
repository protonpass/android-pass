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

package proton.android.pass.screenshottests

import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.compose.ui.platform.ComposeView
import com.android.ide.common.rendering.api.RenderSession
import com.android.ide.common.rendering.api.ViewInfo
import java.lang.ref.WeakReference
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.atomic.AtomicReference

private const val WINDOW_RECOMPOSER_ANDROID_KT_FQN =
    "androidx.compose.ui.platform.WindowRecomposer_androidKt"
private const val COMBINED_CONTEXT_FQN = "kotlin.coroutines.CombinedContext"
private const val SNAPSHOT_KT_FQN = "androidx.compose.runtime.snapshots.SnapshotKt"

/**
 * Initiates a custom [RenderSession] disposal, involving clearing several static collections
 * including some Compose-related objects as well as executing default [RenderSession.dispose].
 */
fun RenderSession.disposeHack() {
    val applyObserversRef = AtomicReference<WeakReference<MutableCollection<*>?>?>(null)
    val toRunTrampolinedRef = AtomicReference<WeakReference<MutableCollection<*>?>?>(null)

    try {
        val windowRecomposer: Class<*> = Class.forName(WINDOW_RECOMPOSER_ANDROID_KT_FQN)
        val animationScaleField = windowRecomposer.getDeclaredField("animationScale")
        animationScaleField.isAccessible = true
        val animationScale = animationScaleField[windowRecomposer]
        if (animationScale is Map<*, *>) {
            (animationScale as MutableMap<*, *>).clear()
        }
    } catch (_: ReflectiveOperationException) {
    }
    applyObserversRef.set(WeakReference(findApplyObservers()))
    toRunTrampolinedRef.set(WeakReference(findToRunTrampolined()))

    execute {
        rootViews
            .filterNotNull()
            .forEach { v -> disposeIfCompose(v) }
    }
    val weakApplyObservers = applyObserversRef.get()
    if (weakApplyObservers != null) {
        val applyObservers = weakApplyObservers.get()
        applyObservers?.clear()
    }
    val weakToRunTrampolined = toRunTrampolinedRef.get()
    if (weakToRunTrampolined != null) {
        val toRunTrampolined = weakToRunTrampolined.get()
        toRunTrampolined?.clear()
    }
    dispose()
}

/**
 * Performs dispose() call against View object associated with [ViewInfo] if that object is an
 * instance of [ComposeViewAdapter]
 *
 * @param viewInfo a [ViewInfo] associated with the View object to be potentially disposed of
 */
private fun disposeIfCompose(viewInfo: ViewInfo) {
    val viewObject: Any? = viewInfo.viewObject
    if (viewObject?.javaClass?.name != "androidx.compose.ui.tooling.ComposeViewAdapter") {
        return
    }
    try {
        val composeView = viewInfo.children[0].viewObject as ComposeView
        composeView.disposeComposition()
    } catch (_: IllegalAccessException) {
    } catch (_: InvocationTargetException) {
    }
}

private fun findApplyObservers(): MutableCollection<*>? {
    try {
        val applyObserversField = Class.forName(SNAPSHOT_KT_FQN).getDeclaredField("applyObservers")
        applyObserversField.isAccessible = true
        val applyObservers = applyObserversField[null]
        if (applyObservers is MutableCollection<*>) {
            return applyObservers
        }
    } catch (_: ReflectiveOperationException) {
    }
    return null
}

private fun findToRunTrampolined(): MutableCollection<*>? {
    try {
        val uiDispatcher = AndroidUiDispatcher::class.java
        val uiDispatcherCompanion = AndroidUiDispatcher.Companion::class.java
        val uiDispatcherCompanionField = uiDispatcher.getDeclaredField("Companion")
        val uiDispatcherCompanionObj = uiDispatcherCompanionField[null]
        val getMainMethod =
            uiDispatcherCompanion.getDeclaredMethod("getMain").apply { isAccessible = true }
        val mainObj = getMainMethod.invoke(uiDispatcherCompanionObj)
        val combinedContext = Class.forName(COMBINED_CONTEXT_FQN)
        val elementField = combinedContext.getDeclaredField("element").apply { isAccessible = true }
        val uiDispatcherObj = elementField[mainObj]

        val toRunTrampolinedField =
            uiDispatcher.getDeclaredField("toRunTrampolined").apply { isAccessible = true }
        val toRunTrampolinedObj = toRunTrampolinedField[uiDispatcherObj]
        if (toRunTrampolinedObj is MutableCollection<*>) {
            return toRunTrampolinedObj
        }
    } catch (_: ReflectiveOperationException) {
    }
    return null
}
