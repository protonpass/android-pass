/*
 * Copyright (c) 2026 Proton AG.
 * This file is part of Proton Pass.
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

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasAnySibling
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.performClick
import proton.android.pass.composecomponents.impl.R as CompR

object DrawerRobot : Robot {

    fun assertVaultShown(vaultName: String): DrawerRobot = apply {
        val vault = hasText(vaultName)
        composeRule.waitUntil(TIMEOUT_MILLIS) { exists(vault) }
    }

    fun openOptionsOf(folderName: String): FolderRobot {
        val options = optionsButtonMatcher(hasText(folderName)) and hasParent(hasClickAction())
        composeRule.waitUntil(TIMEOUT_MILLIS) {
            exists(options) && composeRule.onNode(options, useUnmergedTree = true).isDisplayed()
        }
        waitUntilPositionStable(options)
        composeRule.onNode(options, useUnmergedTree = true).performClick()
        return FolderRobot
    }

    fun openOptionsOfVault(vaultName: String): FolderRobot {
        val vault = hasText(vaultName)
        composeRule.waitUntil(TIMEOUT_MILLIS) {
            exists(vault) && composeRule.onNode(vault, useUnmergedTree = true).isDisplayed()
        }
        waitUntilPositionStable(vault)
        optionsButtonOf(vault).performClick()
        return FolderRobot
    }

    fun openFolder(folderName: String): FolderRobot {
        val folder = hasText(folderName)
        composeRule.waitUntil(TIMEOUT_MILLIS) {
            exists(folder) && composeRule.onNode(folder, useUnmergedTree = true).isDisplayed()
        }
        waitUntilPositionStable(folder)
        composeRule.onNode(folder, useUnmergedTree = true).performClick()
        return FolderRobot
    }

    private fun optionsButtonOf(sibling: SemanticsMatcher): SemanticsNodeInteraction =
        composeRule.onNode(useUnmergedTree = true, matcher = optionsButtonMatcher(sibling))

    private fun optionsButtonMatcher(sibling: SemanticsMatcher): SemanticsMatcher = hasClickAction() and
        hasAnyDescendant(hasContentDescription(string(CompR.string.options_menu_content_description))) and
        !hasAnyDescendant(SemanticsMatcher.keyIsDefined(SemanticsProperties.Text)) and
        hasAnySibling(sibling)
}
