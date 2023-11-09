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

package proton.android.pass.autofill.e2e.ui.sessions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.autofill.debug.AutofillSession
import proton.android.pass.commonui.api.toClassHolder

const val SESSIONS_ROUTE = "e2eapp/sessions"

@Composable
fun SessionsScreen(
    modifier: Modifier = Modifier,
    onSessionClick: (AutofillSession) -> Unit,
    viewModel: SessionsScreenViewModel = hiltViewModel()
) {
    val sessions by viewModel.state.collectAsStateWithLifecycle()

    val context = LocalContext.current
    SessionsScreenContent(
        modifier = modifier,
        sessions = sessions,
        onClearSessions = {
            viewModel.onClearSessions(context.toClassHolder())
        },
        onSessionClick = onSessionClick,
        onShareSessionClick = {
            viewModel.startShareIntent(it, context.toClassHolder())
        }
    )
}

