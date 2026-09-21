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

package proton.android.pass.features.featureflags.internalsettings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import proton.android.pass.preferences.debug.PreferencesStoreDump

@Composable
fun InternalSettingsDebugContent(modifier: Modifier = Modifier, stores: ImmutableList<PreferencesStoreDump>) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding(),
        topBar = { TopAppBar { Text(text = "Internal settings") } }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(stores.size) { index ->
                PreferencesStoreSection(store = stores[index])
            }
        }
    }
}

@Composable
private fun PreferencesStoreSection(modifier: Modifier = Modifier, store: PreferencesStoreDump) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = store.storeName,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.subtitle1
        )
        Text(
            text = store.fileName,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = MaterialTheme.colors.onSurface.copy(alpha = LABEL_ALPHA)
        )
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = store.contents,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            lineHeight = 16.sp
        )
    }
}

private const val LABEL_ALPHA = 0.6f
