package proton.android.pass.featureprofile.impl

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTypography
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
fun PlanInfoIndicator(
    modifier: Modifier = Modifier,
    planInfo: PlanInfo
) {
    val resources = when (planInfo) {
        PlanInfo.Hide -> null
        is PlanInfo.Trial -> {
            PlanResources(
                icon = CompR.drawable.account_trial_indicator,
                color = PassTheme.colors.interactionNormMajor2,
                text = stringResource(R.string.profile_account_plan_name_trial)
            )
        }
        is PlanInfo.Unlimited -> {
            PlanResources(
                icon = CompR.drawable.account_unlimited_indicator,
                color = PassTheme.colors.noteInteractionNorm,
                text = planInfo.planName
            )
        }
    }

    if (resources != null) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(resources.icon),
                contentDescription = resources.text,
                tint = Color.Unspecified
            )

            Text(
                text = resources.text,
                style = PassTypography.body3Regular,
                color = resources.color
            )
        }
    }
}

internal data class PlanResources(
    @DrawableRes val icon: Int,
    val color: Color,
    val text: String
)


