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

package proton.android.pass.autofill.debug

import android.content.Context
import android.service.autofill.FillRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import proton.android.pass.autofill.Utils
import proton.android.pass.autofill.debug.DebugUtils.autofillDumpDir
import proton.android.pass.autofill.entities.AutofillNode
import proton.android.pass.autofill.entities.InputTypeValue
import proton.android.pass.autofill.heuristics.toAutofillNode
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.log.api.PassLogger
import java.io.File

object AutofillDebugSaver {

    private const val TAG = "AutofillDebugSaver"

    @Serializable
    data class DebugAutofillEntry(
        val rootContent: DebugAutofillNode,
        val packageName: String
    )

    @Serializable
    data class DebugAutofillNode(
        val id: Int = 0,
        val className: String? = null,
        val isImportantForAutofill: Boolean = false,
        val text: String? = null,
        val isFocused: Boolean = false,
        val inputType: InputTypeValue = InputTypeValue(0),
        val autofillHints: List<String> = emptyList(),
        val htmlAttributes: List<HtmlAttribute> = emptyList(),
        val children: List<DebugAutofillNode> = emptyList(),
        val url: String? = null,
        val hintKeywordList: List<String> = emptyList(),
        val expectedAutofill: String? = null
    )

    @Serializable
    data class HtmlAttribute(
        val key: String,
        val value: String
    )

    suspend fun save(context: Context, request: FillRequest) {
        val windowNode = Utils.getWindowNodes(request.fillContexts).lastOrNull()
        val rootViewNode = windowNode?.rootViewNode ?: return
        val packageName = Utils.getApplicationPackageName(windowNode)

        safeRunCatching {
            val debugEntry = DebugAutofillEntry(
                rootContent = rootViewNode.toAutofillNode().toDebugNode(),
                packageName = packageName
            )
            val asString = Json.encodeToString(debugEntry)
            storeFile(
                context = context,
                packageName = packageName,
                content = asString
            )
        }.onSuccess {
            PassLogger.i(TAG, "Debug autofill stored")
        }.onFailure {
            PassLogger.w(TAG, "Error storing debug autofill")
            PassLogger.w(TAG, it)
        }
    }

    @Suppress("unused")
    private fun getAllUrls(debugAutofillEntry: DebugAutofillEntry): Set<String> {
        val urls = mutableSetOf<String>()

        fun traverseAutofillNode(node: DebugAutofillNode) {
            node.url?.let { urls.add(it) }
            node.children.forEach { traverseAutofillNode(it) }
        }

        traverseAutofillNode(debugAutofillEntry.rootContent)
        return urls
    }

    private suspend fun storeFile(
        context: Context,
        packageName: String,
        content: String
    ) = withContext(Dispatchers.IO) {
        val dir = autofillDumpDir(context)
        val now = System.currentTimeMillis().toString()
        val file = File(dir, "$packageName-$now.json")
        file.writeText(content)
    }

    private fun AutofillNode.toDebugNode(): DebugAutofillNode = DebugAutofillNode(
        id = this.id?.hashCode() ?: 0,
        className = className,
        isImportantForAutofill = isImportantForAutofill,
        text = text,
        isFocused = isFocused,
        inputType = inputType,
        autofillHints = autofillHints,
        htmlAttributes = htmlAttributes.map { HtmlAttribute(it.first, it.second) },
        children = children.map { it.toDebugNode() },
        url = url.value(),
        hintKeywordList = hintKeywordList.map { it.toString() }
    )

}
