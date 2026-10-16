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
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import proton.android.pass.composecomponents.impl.R as CompR
import proton.android.pass.features.vault.R as VaultR

object FolderRobot : Robot {

    private val createSubfolderRow get() = hasText(string(CompR.string.bottomsheet_create_sub_folder))

    private val createFolderRow get() = hasText(string(CompR.string.bottomsheet_create_folder))

    fun chooseAddFolderTo(vaultName: String): FolderRobot = apply {
        HomeRobot.openDrawer().openOptionsOfVault(vaultName)
        waitForVaultOptions()
        composeRule.onNode(createFolderRow, useUnmergedTree = true).performClick()
    }

    fun confirmCreation(): FolderRobot = apply {
        createButton().performClick()
    }

    fun assertMessageShown(message: String): FolderRobot = apply {
        composeRule.waitUntil(TIMEOUT_MILLIS) { exists(hasText(message)) }
    }

    fun assertCreateDisabled(): FolderRobot = apply { createButton().assertIsNotEnabled() }

    fun assertErrorShown(error: String): FolderRobot = apply {
        val message = hasText(error, ignoreCase = true)
        composeRule.waitUntil(TIMEOUT_MILLIS) { exists(message) }
    }

    fun assertCannotAddFolder() {
        waitForVaultOptions()
        check(!exists(createFolderRow)) { "Create folder is offered" }
    }

    fun assertFolderCreationOfferedAsUpgrade() {
        waitForVaultOptions()
        check(exists(createFolderRow)) { "Create folder is not offered" }
        composeRule.onNode(createFolderRow, useUnmergedTree = true).performClick()
        val upsell = listOf("upsell_plan_title", "upsell_plan_what_included")
            .map { hasText(string(stringIdentifier(it))) }
        composeRule.waitUntil(TIMEOUT_MILLIS) { upsell.any { exists(it) } }
        check(!exists(hasText(string(stringIdentifier("vault_add_folder_dialog_title"))))) {
            "Add folder dialog is shown instead of the upgrade flow"
        }
    }

    private fun createButton(): SemanticsNodeInteraction {
        val create = hasText(string(VaultR.string.vault_add_folder_dialog_create_action)) and hasClickAction()
        composeRule.waitUntil(TIMEOUT_MILLIS) { composeRule.onAllNodes(create).fetchSemanticsNodes().isNotEmpty() }
        return composeRule.onNode(create)
    }

    private fun waitForFolderOptions() {
        val rename = hasText(string(CompR.string.bottomsheet_rename_folder))
        composeRule.waitUntil(TIMEOUT_MILLIS) { exists(rename) }
    }

    private fun waitForVaultOptions() {
        val sheetContent = listOf("bottomsheet_edit", "bottomsheet_leave_vault", "bottomsheet_view_members")
            .map { hasText(string(stringIdentifier(it))) }
        composeRule.waitUntil(TIMEOUT_MILLIS) { sheetContent.any { exists(it) } }
    }

    fun chooseAddSubfolderTo(folderName: String): FolderRobot = apply {
        HomeRobot.openDrawer().openOptionsOf(folderName)
        composeRule.waitUntil(TIMEOUT_MILLIS) { exists(createSubfolderRow) }
        composeRule.onNode(createSubfolderRow, useUnmergedTree = true).performClick()
    }

    fun assertCannotAddSubfolder() {
        waitForFolderOptions()
        check(!exists(createSubfolderRow)) { "Create sub-folder is offered" }
    }

    fun chooseRename(): FolderRobot = apply {
        val renameOption = hasText(string(CompR.string.bottomsheet_rename_folder))
        composeRule.waitUntil(TIMEOUT_MILLIS) { exists(renameOption) }
        composeRule.onNode(renameOption, useUnmergedTree = true).performClick()
    }

    fun confirmRename(): FolderRobot = apply {
        val save = hasText(string(VaultR.string.vault_edit_folder_dialog_save_action)) and hasClickAction()
        composeRule.waitUntil(TIMEOUT_MILLIS) { composeRule.onAllNodes(save).fetchSemanticsNodes().isNotEmpty() }
        composeRule.onNode(save).performClick()
    }

    fun assertViewing(folderName: String): FolderRobot = apply {
        val placeholder = hasText(string(stringIdentifier("search_topbar_placeholder_folder"), folderName))
        composeRule.waitUntil(TIMEOUT_MILLIS) { exists(placeholder) }
    }

    fun assertNotViewing(folderName: String): FolderRobot = apply {
        val viewingAFolder = listOf(
            hasText(string(stringIdentifier("search_topbar_placeholder_folder"), folderName)),
            hasText(string(stringIdentifier("search_topbar_placeholder_folder_fallback"))),
            hasText(string(stringIdentifier("home_topbar_title_folder_fallback"))),
            hasText(folderName)
        )
        composeRule.waitUntil(TIMEOUT_MILLIS) { viewingAFolder.none { exists(it) } }
    }

    fun chooseDeletePermanently(): DeleteFolderDialogRobot {
        val deleteOption = hasText(string(CompR.string.bottomsheet_delete_permanently))
        composeRule.waitUntil(TIMEOUT_MILLIS) { exists(deleteOption) }
        composeRule.onNode(deleteOption, useUnmergedTree = true).performClick()
        return DeleteFolderDialogRobot
    }
}
