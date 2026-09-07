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

package proton.android.pass.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.domain.inappmessages.InAppMessage
import proton.android.pass.domain.inappmessages.InAppMessageId
import proton.android.pass.domain.inappmessages.InAppMessageKey
import proton.android.pass.features.home.HomeNavItem
import proton.android.pass.features.home.localinappmessages.LocalInAppMessageBanner
import proton.android.pass.features.inappmessages.banner.ui.InAppMessageBanner

private const val BANNER_ANIM_DURATION_MS = 300

private fun InAppMessage.Banner.stableId(): String = when (this) {
    is InAppMessage.Local.Autofill -> "local:autofill"
    is InAppMessage.Local.NotificationPermission -> "local:notification"
    is InAppMessage.Local.SLSync -> "local:slsync"
    is InAppMessage.Remote.Banner -> id.value
}

@Composable
internal fun InAppMessageBannerContainer(
    modifier: Modifier = Modifier,
    inAppMessages: List<InAppMessage.Banner>,
    currentRoute: String?,
    bannerBottomPadding: Dp,
    onLocalInAppMessageClick: (InAppMessage.Local) -> Unit,
    onLocalInAppMessageDismiss: (InAppMessage.Local) -> Unit,
    onInAppMessageBannerRead: (UserId, InAppMessageId, InAppMessageKey) -> Unit,
    onInAppMessageBannerCTAClicked: (InAppMessageKey) -> Unit,
    onInAppMessageBannerDisplayed: (InAppMessageKey) -> Unit,
    onNavigateToDeeplink: (String) -> Unit,
    onOpenExternalUrl: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    // Local display list: mirrors ViewModel but keeps items alive during exit animation
    var displayMessages by remember { mutableStateOf(emptyList<InAppMessage.Banner>()) }
    var dismissingIds by remember { mutableStateOf(emptySet<String>()) }

    LaunchedEffect(inAppMessages) {
        val incomingIds = inAppMessages.map { it.stableId() }.toSet()
        val kept = displayMessages.filter { msg ->
            msg.stableId() in incomingIds || msg.stableId() in dismissingIds
        }
        val arrivals = inAppMessages.filterNot { msg ->
            kept.any { it.stableId() == msg.stableId() }
        }
        displayMessages = kept + arrivals
    }

    val animateDismiss: (String, () -> Unit) -> Unit = remember(coroutineScope) {
        { id, realDismiss ->
            dismissingIds = dismissingIds + id
            realDismiss()
            coroutineScope.launch {
                delay(BANNER_ANIM_DURATION_MS.toLong())
                displayMessages = displayMessages.filterNot { it.stableId() == id }
                dismissingIds = dismissingIds - id
            }
        }
    }

    AnimatedVisibility(
        modifier = modifier,
        visible = displayMessages.isNotEmpty() && currentRoute == HomeNavItem.route,
        enter = EnterTransition.None,
        exit = ExitTransition.None
    ) {
        Column(
            modifier = Modifier.padding(bottom = bannerBottomPadding),
            verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            displayMessages.forEach { message ->
                val msgId = message.stableId()

                key(msgId) {
                    var entered by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) { entered = true }

                    AnimatedVisibility(
                        visible = entered && msgId !in dismissingIds,
                        enter = fadeIn(tween(BANNER_ANIM_DURATION_MS)) +
                            expandVertically(tween(BANNER_ANIM_DURATION_MS)) +
                            slideInVertically(tween(BANNER_ANIM_DURATION_MS)) { it / 2 },
                        exit = fadeOut(tween(BANNER_ANIM_DURATION_MS)) +
                            shrinkVertically(tween(BANNER_ANIM_DURATION_MS)) +
                            slideOutVertically(tween(BANNER_ANIM_DURATION_MS)) { it / 2 }
                    ) {
                        when (message) {
                            is InAppMessage.Local -> LocalInAppMessageBanner(
                                message = message,
                                onClick = { onLocalInAppMessageClick(message) },
                                onDismiss = {
                                    animateDismiss(msgId) {
                                        onLocalInAppMessageDismiss(message)
                                    }
                                }
                            )
                            is InAppMessage.Remote.Banner -> InAppMessageBanner(
                                inAppMessage = message,
                                onDismiss = { userId, id, key ->
                                    animateDismiss(msgId) {
                                        onInAppMessageBannerRead(userId, id, key)
                                    }
                                },
                                onInternalCTAClick = { userId, id, key, value ->
                                    animateDismiss(msgId) {
                                        onInAppMessageBannerRead(userId, id, key)
                                    }
                                    onInAppMessageBannerCTAClicked(key)
                                    onNavigateToDeeplink(value)
                                },
                                onExternalCTAClick = { userId, id, key, value ->
                                    animateDismiss(msgId) {
                                        onInAppMessageBannerRead(userId, id, key)
                                    }
                                    onInAppMessageBannerCTAClicked(key)
                                    onOpenExternalUrl(value)
                                },
                                onDisplay = { key -> onInAppMessageBannerDisplayed(key) }
                            )
                        }
                    }
                }
            }
        }
    }
}
