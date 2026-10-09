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

package proton.android.pass.uitest.robot

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasText
import androidx.test.platform.app.InstrumentationRegistry
import me.proton.test.fusion.Fusion.node
import me.proton.test.fusion.FusionConfig
import proton.android.pass.features.sync.R
import kotlin.time.Duration.Companion.seconds
import proton.android.pass.composecomponents.impl.R as CompR

object SyncDialogRobot : Robot {

    private val dialogTimeout = 5.seconds

    private val syncTimeout = 90.seconds

    private val syncTitles = listOf(
        R.string.sync_dialog_title,
        R.string.sync_dialog_title_inserting,
        R.string.sync_dialog_title_indexing,
        R.string.sync_dialog_title_success,
        R.string.sync_dialog_title_partial_error,
        R.string.sync_dialog_title_error
    )

    private val continueButton get() = node.withText(CompR.string.action_continue)

    fun completeSyncIfDisplayed(): SyncDialogRobot = apply {
        val isSyncDialogDisplayed = runCatching {
            waitUntilAnyExists(syncTitles.map { hasText(string(it)) }, dialogTimeout.inWholeMilliseconds)
        }.isSuccess
        if (isSyncDialogDisplayed) {
            continueButton.await(syncTimeout) { assertIsDisplayed() }
            continueButton.click()
            continueButton.await { assertDoesNotExist() }
        }
    }

    private fun waitUntilAnyExists(matchers: List<SemanticsMatcher>, timeoutMillis: Long) {
        val composeRule = FusionConfig.Compose.testRule.get()
        composeRule.waitUntil(timeoutMillis) {
            matchers.any { matcher ->
                composeRule.onAllNodes(matcher, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
            }
        }
    }

    private fun string(resId: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(resId)
}
