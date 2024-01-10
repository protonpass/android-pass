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

package proton.android.pass.autofill

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import proton.android.pass.autofill.debug.AutofillDebugSaver
import proton.android.pass.autofill.entities.AutofillNode
import proton.android.pass.common.api.toOption
import java.io.File

object TestUtils {

    fun parseResourceFile(file: String): AutofillDebugSaver.DebugAutofillEntry {
        val path = "src/test/resources/$file"
        val asFile = File(path)
        val content = asFile.readText()
        return Json.decodeFromString(content)
    }

    fun getExpectedContents(
        entry: AutofillDebugSaver.DebugAutofillEntry,
        allowEmptyFields: Boolean
    ): List<Pair<AutofillDebugSaver.DebugAutofillNode, ExpectedAutofill>> {
        val withContents = mutableListOf<Pair<AutofillDebugSaver.DebugAutofillNode, ExpectedAutofill>>()
        getExpectedContents(entry.rootContent, withContents)
        if (withContents.isEmpty() && !allowEmptyFields) {
            throw IllegalStateException("There are no fields with 'expectedAutofill'")
        }
        return withContents
    }

    private fun getExpectedContents(
        node: AutofillDebugSaver.DebugAutofillNode,
        withExpectedContents: MutableList<Pair<AutofillDebugSaver.DebugAutofillNode, ExpectedAutofill>>
    ) {
        val expectedContents = node.expectedAutofill
        if (expectedContents != null) {
            val expectedAutofill = ExpectedAutofill.values()
                .firstOrNull { it.value == expectedContents }
                ?: throw IllegalStateException(
                    "Unknown expectedAutofill: $expectedContents. Must be one of ${ExpectedAutofill.all()}"
                )

            withExpectedContents.add(node to expectedAutofill)
        }
        node.children.forEach { getExpectedContents(it, withExpectedContents) }
    }

    fun AutofillDebugSaver.DebugAutofillNode.toAutofillNode(): AutofillNode = AutofillNode(
        className = className,
        isImportantForAutofill = isImportantForAutofill,
        text = text,
        isFocused = isFocused,
        inputType = inputType,
        autofillHints = autofillHints,
        htmlAttributes = htmlAttributes.map { it.key to it.value },
        children = children.map { it.toAutofillNode() },
        url = url.toOption(),
        hintKeywordList = hintKeywordList,
        autofillValue = null,
        id = TestAutofillId(id)
    )
}
