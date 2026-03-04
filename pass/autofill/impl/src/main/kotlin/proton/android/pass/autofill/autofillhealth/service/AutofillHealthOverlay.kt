/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.autofill.autofillhealth.service

import android.content.Context
import proton.android.pass.autofill.autofillhealth.model.AutofillHealthEvent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PixelFormat
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AutofillHealthOverlay @Inject constructor(
    @ApplicationContext private val context: Context,
    private val monitor: AutofillHealthMonitor
) {

    private var overlayView: View? = null
    private var overlayScope: CoroutineScope? = null

    val isVisible: Boolean
        get() = overlayView != null

    val hasPermissionInManifest: Boolean by lazy {
        runCatching {
            val info = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_PERMISSIONS
            )
            info.requestedPermissions?.contains(android.Manifest.permission.SYSTEM_ALERT_WINDOW) == true
        }.getOrDefault(false)
    }

    val canShow: Boolean
        get() = hasPermissionInManifest && Settings.canDrawOverlays(context)

    @SuppressWarnings("LongMethod")
    fun show() {
        if (!canShow) return
        if (overlayView != null) return

        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.argb(BACKGROUND_ALPHA, 0, 0, 0))
            setPadding(PADDING, PADDING_VERTICAL, PADDING, PADDING_VERTICAL)
            gravity = Gravity.CENTER_VERTICAL
        }

        val dot = View(context).apply {
            val size = DOT_SIZE
            layoutParams = LinearLayout.LayoutParams(size, size).apply {
                marginEnd = DOT_MARGIN
            }
            setBackgroundColor(Color.RED)
        }
        layout.addView(dot)

        val text = TextView(context).apply {
            setTextColor(Color.WHITE)
            textSize = TEXT_SIZE
            maxLines = MAX_LINES
            text = "Autofill: --"
        }
        layout.addView(text)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = INITIAL_X
            y = INITIAL_Y
        }

        setupDrag(layout, params, windowManager)

        runCatching {
            windowManager.addView(layout, params)
            overlayView = layout
        }.onFailure {
            PassLogger.w(TAG, "Failed to add overlay view")
            PassLogger.w(TAG, it)
            return
        }

        overlayScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        overlayScope?.launch {
            combine(
                monitor.isConnected,
                monitor.lastFillRequestEvent,
                monitor.currentIme,
                monitor.events
            ) { values ->
                OverlayData(
                    isConnected = values[0] as Boolean,
                    lastFillRequest = values[1] as? AutofillHealthEvent,
                    currentIme = values[2] as String,
                    eventCount = (values[3] as List<*>).size
                )
            }.collect { data ->
                dot.setBackgroundColor(if (data.isConnected) Color.GREEN else Color.RED)

                val fillInfo = data.lastFillRequest?.let { event ->
                    val pkg = event.packageName ?: "?"
                    val type = event.type.name.removePrefix("FILL_REQUEST_").lowercase()
                    "$pkg ($type)"
                } ?: "--"

                val imeShort = data.currentIme.ifEmpty { "?" }
                text.text = "AF: $fillInfo | $imeShort [${data.eventCount}]"
            }
        }
    }

    fun hide() {
        overlayScope?.cancel()
        overlayScope = null

        overlayView?.let { view ->
            runCatching {
                val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                windowManager.removeView(view)
            }.onFailure {
                PassLogger.w(TAG, "Failed to remove overlay view")
                PassLogger.w(TAG, it)
            }
        }
        overlayView = null
    }

    private fun setupDrag(
        view: View,
        params: WindowManager.LayoutParams,
        windowManager: WindowManager
    ) {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    runCatching { windowManager.updateViewLayout(view, params) }
                    true
                }
                else -> false
            }
        }
    }

    private data class OverlayData(
        val isConnected: Boolean,
        val lastFillRequest: AutofillHealthEvent?,
        val currentIme: String,
        val eventCount: Int
    )

    private companion object {
        const val TAG = "AutofillHealthOverlay"
        const val BACKGROUND_ALPHA = 200
        const val PADDING = 24
        const val PADDING_VERTICAL = 16
        const val DOT_SIZE = 24
        const val DOT_MARGIN = 16
        const val TEXT_SIZE = 11f
        const val MAX_LINES = 2
        const val INITIAL_X = 16
        const val INITIAL_Y = 100
    }
}
