/*
 * Copyright (c) 2024-2025 Proton AG
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

package proton.android.pass.features.upsell.v1.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import me.proton.core.presentation.R
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.container.RoundedCornersColumn
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun UpsellFeatures(modifier: Modifier = Modifier, features: ImmutableList<Pair<Int, Int>>) {
    RoundedCornersColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = Spacing.mediumSmall)
    ) {
        Spacer(modifier = Modifier.height(height = Spacing.small))

        features.forEach { (iconResId, textResId) ->
            UpsellFeatureRow(
                iconResId = iconResId,
                textResId = textResId
            )
        }

        Spacer(modifier = Modifier.height(height = Spacing.small))
    }
}

@[Preview Composable]
fun UpsellFeaturesPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            UpsellFeatures(
                features = persistentListOf(
                    CompR.drawable.ic_shield_union to
                        proton.android.pass.features.upsell.v1.R.string.upsell_paid_feature_dark_web_monitoring,
                    R.drawable.ic_proton_user to
                        proton.android.pass.features.upsell.v1.R.string.upsell_paid_feature_sentinel,
                    R.drawable.ic_proton_lock to
                        proton.android.pass.features.upsell.v1.R.string.upsell_paid_feature_authenticator,
                    R.drawable.ic_proton_alias to
                        proton.android.pass.features.upsell.v1.R.string.upsell_paid_feature_unlimited_aliases,
                    R.drawable.ic_proton_users_plus to
                        proton.android.pass.features.upsell.v1.R.string.upsell_paid_feature_vault_sharing
                )
            )
        }
    }
}
