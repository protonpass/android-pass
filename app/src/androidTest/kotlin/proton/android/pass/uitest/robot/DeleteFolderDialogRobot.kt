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

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.platform.app.InstrumentationRegistry
import proton.android.pass.features.vault.R as VaultR

object DeleteFolderDialogRobot : Robot {

    private const val FOLDER_NAME_TAG = "__FOLDER_NAME__"

    fun assertAsksForFolderName(): DeleteFolderDialogRobot = apply {
        val title = hasText(string(VaultR.string.folder_delete_dialog_title))
        composeRule.waitUntil(TIMEOUT_MILLIS) {
            exists(title)
        }
        composeRule.onNode(hasText(string(VaultR.string.folder_delete_dialog_placeholder))).assertExists()
    }

    fun enterFolderName(folderName: String): DeleteFolderDialogRobot = apply {
        composeRule.onNode(hasSetTextAction() and hasAnyAncestor(isDialog())).performTextReplacement(folderName)
    }

    fun assertWarnsAllContentsDeleted(folderName: String): DeleteFolderDialogRobot = apply {
        val body = string(VaultR.string.folder_delete_dialog_body).replace(FOLDER_NAME_TAG, folderName)
        waitForText(body)
    }

    fun assertWarnsItemsDeleted(itemCount: Int, folderName: String): DeleteFolderDialogRobot = apply {
        val body = InstrumentationRegistry.getInstrumentation().targetContext.resources.getQuantityString(
            VaultR.plurals.folder_delete_dialog_body_with_items,
            itemCount,
            itemCount,
            folderName
        )
        waitForText(body)
    }

    fun confirmDeletion(): DeleteFolderDialogRobot = apply { deleteButton().performClick() }

    fun assertDeleteDisabled(): DeleteFolderDialogRobot = apply { deleteButton().assertIsNotEnabled() }

    fun assertDeleteEnabled(): DeleteFolderDialogRobot = apply { deleteButton().assertIsEnabled() }

    private fun waitForText(text: String) {
        val matcher = hasText(text)
        composeRule.waitUntil(TIMEOUT_MILLIS) {
            exists(matcher)
        }
    }

    private fun deleteButton(): SemanticsNodeInteraction = composeRule.onNode(
        hasText(string(VaultR.string.folder_delete_dialog_delete_action)) and hasClickAction()
    )
}
