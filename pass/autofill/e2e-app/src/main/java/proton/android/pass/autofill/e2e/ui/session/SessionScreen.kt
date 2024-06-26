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

package proton.android.pass.autofill.e2e.ui.session

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.autofill.debug.AutofillDebugSaver
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.applyIf

const val SESSION_DETAIL_ROUTE = "e2eapp/session"
const val SESSION_DETAIL_ARG_NAME = "session"

@Composable
fun SessionScreen(modifier: Modifier = Modifier, viewModel: SessionDetailViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val content = state) {
        DetailContent.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is DetailContent.Success -> {
            SessionDetailContent(modifier = modifier, content = content.content)
        }
    }
}

@Composable
private fun SessionDetailContent(modifier: Modifier = Modifier, content: AutofillDebugSaver.DebugAutofillEntry) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("Session Detail") })
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .horizontalScroll(rememberScrollState())
                .padding(padding)
        ) {
            DebugAutofillNodeView(
                content = content.rootContent,
                padding = 8.dp,
                level = 0
            )
        }
    }
}

fun AutofillDebugSaver.DebugAutofillNode.hasEditTextOrUrl(): Boolean {
    if (className == "android.widget.EditText" || !url.isNullOrBlank()) {
        return true
    }

    return children.any { it.hasEditTextOrUrl() }
}

@Suppress("ComplexMethod")
@Composable
private fun DebugAutofillNodeView(
    modifier: Modifier = Modifier,
    content: AutofillDebugSaver.DebugAutofillNode,
    padding: Dp,
    level: Int
) {
    var showContent by remember { mutableStateOf(content.hasEditTextOrUrl()) }
    Column(modifier = modifier.padding(start = padding, top = Spacing.extraSmall, bottom = Spacing.extraSmall)) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .applyIf(
                    condition = content.children.isNotEmpty(),
                    ifTrue = {
                        clickable { showContent = !showContent }
                    }
                ),
            horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(colorFromLevel(level))
            )
            Column(modifier = Modifier.fillMaxHeight()) {
                RowText(
                    text = remember { fieldRow("CN", content.className) },
                    color = if (content.className == "android.widget.EditText") Color.Red else PassTheme.colors.textNorm
                )

                if (!content.text.isNullOrBlank()) {
                    RowText(text = remember { fieldRow("Text", content.text) })
                }

                if (!content.url.isNullOrBlank()) {
                    RowText(text = remember { fieldRow("URL", content.url) }, color = Color.Green)
                }

                val autofillHints = content.autofillHints.filter { it.isNotBlank() }
                if (autofillHints.isNotEmpty()) {
                    Column {
                        RowText(text = remember { fieldRow("AF Hints", "") })
                        autofillHints.forEach {
                            RowText(text = remember { fieldRow(" - $it", "") })
                        }
                    }
                }

                val hintKeywords = content.hintKeywordList.filter { it.isNotBlank() }
                if (hintKeywords.isNotEmpty()) {
                    Column {
                        RowText(text = remember { fieldRow("Hint keywords", "") })
                        hintKeywords.forEach {
                            RowText(text = remember { fieldRow(" - $it", "") })
                        }
                    }
                }

                val htmlAttributes =
                    content.htmlAttributes.filter { it.key.isNotBlank() && it.value.isNotBlank() }
                if (htmlAttributes.isNotEmpty()) {
                    Column {
                        RowText(text = remember { fieldRow("HTML Attrs", "") })
                        htmlAttributes.forEach {
                            RowText(text = remember { fieldRow(" - ${it.key}", it.value) })
                        }
                    }
                }
                if (content.inputType.value != 0) {
                    RowText(text = remember { fieldRow("InputType", "${content.inputType}") })
                }

                RowText(
                    text = remember {
                        fieldRow(
                            "ImportantForAutofill",
                            "${content.isImportantForAutofill}"
                        )
                    }
                )
                if (content.isFocused) {
                    RowText(text = remember { fieldRow("Focused", "") }, color = Color.Cyan)
                }
                if (content.children.isNotEmpty()) {
                    RowText(text = remember { fieldRow("Children", "${content.children.size} nodes") })
                    Divider(
                        modifier = Modifier.width(200.dp),
                        thickness = 1.dp,
                        color = PassTheme.colors.textNorm
                    )
                }
            }
        }
        AnimatedVisibility(visible = showContent) {
            Column {
                content.children.forEach {
                    DebugAutofillNodeView(
                        content = it,
                        padding = padding,
                        level = level + 1
                    )
                }
            }
        }
    }
}

@Composable
fun RowText(
    modifier: Modifier = Modifier,
    text: AnnotatedString,
    color: Color = PassTheme.colors.textNorm
) {
    Text(modifier = modifier, text = text, fontSize = 12.sp, color = color)
}

fun fieldRow(fieldName: String, fieldValue: String?): AnnotatedString = buildAnnotatedString {
    val fieldNameText = if (fieldValue.isNullOrBlank()) {
        fieldName
    } else {
        "$fieldName: "
    }
    append(AnnotatedString(fieldNameText, spanStyle = SpanStyle(fontWeight = FontWeight.Bold)))
    append(fieldValue ?: "")
}

@Suppress("MagicNumber")
private fun colorFromLevel(level: Int): Color = when (level % 5) {
    0 -> Color.Red
    1 -> Color.Blue
    2 -> Color.Green
    3 -> Color.Yellow
    4 -> Color.Magenta
    else -> Color.Red
}

