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

package proton.android.pass.features.upsell.plus.ui

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.persistentListOf
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.features.upsell.R
import proton.android.pass.features.upsell.shared.navigation.UpsellNavDestination
import proton.android.pass.features.upsell.shared.ui.UpsellFeatureModel
import me.proton.core.presentation.R as CoreR
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
fun UpsellPlusScreen(onNavigated: (UpsellNavDestination) -> Unit) {

    val features = persistentListOf(
        UpsellFeatureModel(
            iconResId = CoreR.drawable.ic_proton_alias,
            iconColor = PassTheme.colors.aliasInteractionNorm,
            textResId = R.string.upsell_plus_feature_alias
        ),
        UpsellFeatureModel(
            iconResId = CoreR.drawable.ic_proton_lock,
            iconColor = PassTheme.colors.loginInteractionNorm,
            textResId = R.string.upsell_plus_feature_2fa
        ),
        UpsellFeatureModel(
            iconResId = CoreR.drawable.ic_proton_users_plus,
            iconColor = PassTheme.colors.noteInteractionNorm,
            textResId = R.string.upsell_plus_feature_share
        ),
        UpsellFeatureModel(
            iconResId = CompR.drawable.ic_vaults,
            iconColor = Color.Unspecified,
            textResId = R.string.upsell_plus_feature_vaults
        )
    )

    UpsellPlusContent(
        features = features,
        onNavigate = onNavigated
    )
}

@[Preview Composable]
fun UpsellPlusScreenPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            UpsellPlusScreen(onNavigated = {})
        }
    }
}
