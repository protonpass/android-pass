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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import proton.android.pass.composecomponents.impl.topbar.TopBarTitleView
import proton.android.pass.navigation.api.NavItem

object SessionsList : NavItem(baseRoute = "sessionsList")

@Composable
fun AutofillDebugSessions(
    modifier: Modifier = Modifier,
    sessions: ImmutableList<AutofillSession>,
    onSessionClick: (AutofillSession) -> Unit,
) {
    val context = LocalContext.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar {
                TopBarTitleView(title = "Autofill Debug")
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = { DebugUtils.autofillDumpDir(context).deleteRecursively() }) {
                    Text(text = "Clear All")
                }
            }
        }
    ) { padding ->
        if (sessions.isEmpty()) {
            Text(text = "No debug sessions")
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(items = sessions, key = { it.filename }) { session ->
                    Row(
                        modifier = modifier
                            .fillMaxWidth()
                            .clickable { onSessionClick(session) }
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = AnnotatedString(
                                    text = session.packageName,
                                    spanStyle = SpanStyle(fontWeight = FontWeight.Bold)
                                ),
                                fontSize = 14.sp
                            )
                            Text(
                                text = session.timestamp,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
