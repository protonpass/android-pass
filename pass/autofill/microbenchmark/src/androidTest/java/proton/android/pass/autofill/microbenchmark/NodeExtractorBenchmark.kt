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

package proton.android.pass.autofill.microbenchmark

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import proton.android.pass.autofill.debug.AutofillDebugSaver
import proton.android.pass.autofill.entities.AutofillNode
import proton.android.pass.autofill.heuristics.NodeExtractor
import proton.android.pass.common.api.toOption

private const val FILE = "resources/creditcard/chrome_stripe_credit_card.json"

@LargeTest
@RunWith(AndroidJUnit4::class)
class NodeExtractorBenchmark {
    @get:Rule
    val benchmarkRule = BenchmarkRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun benchmarkNodeExtractor() {
        val content = context.assets.open(FILE).bufferedReader().use { it.readText() }
        val parsed: AutofillDebugSaver.DebugAutofillEntry = Json.decodeFromString(content)
        val asAutofillNodes = parsed.rootContent.toAutofillNode()
        benchmarkRule.measureRepeated {
            NodeExtractor(emptyList()).extract(asAutofillNodes)
        }
    }

    private fun AutofillDebugSaver.DebugAutofillNode.toAutofillNode(): AutofillNode = AutofillNode(
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

