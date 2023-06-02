package proton.android.pass.featureitemdetail.impl.login

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.asAnnotatedString
import proton.android.pass.commonui.api.toPasswordAnnotatedString
import proton.android.pass.composecomponents.impl.container.Circle
import proton.android.pass.composecomponents.impl.item.SectionTitle
import proton.android.pass.featureitemdetail.impl.R
import proton.android.pass.featureitemdetail.impl.common.SectionSubtitle
import proton.pass.domain.HiddenState
import me.proton.core.presentation.R as CoreR

private const val CHAR_AMOUNT = 12

@Composable
internal fun LoginPasswordRow(
    modifier: Modifier = Modifier,
    passwordHiddenState: HiddenState,
    label: String = stringResource(R.string.field_password),
    @DrawableRes iconRes: Int = me.proton.core.presentation.R.drawable.ic_proton_key,
    iconContentDescription: String = stringResource(R.string.password_key_icon_content_description),
    onTogglePasswordClick: () -> Unit,
    onCopyPasswordClick: () -> Unit
) {
    val (sectionContent, icon, actionContent) = when (passwordHiddenState) {
        is HiddenState.Concealed -> LoginPasswordUIState(
            sectionContent = "•".repeat(CHAR_AMOUNT),
            icon = CoreR.drawable.ic_proton_eye,
            actionContent = R.string.action_conceal_password
        )

        is HiddenState.Revealed -> LoginPasswordUIState(
            sectionContent = passwordHiddenState.clearText,
            icon = CoreR.drawable.ic_proton_eye_slash,
            actionContent = R.string.action_conceal_password
        )

        is HiddenState.Empty -> LoginPasswordUIState(
            sectionContent = "",
            icon = null,
            actionContent = null
        )
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCopyPasswordClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = iconContentDescription,
            tint = PassTheme.colors.loginInteractionNorm
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            SectionTitle(text = label)
            Spacer(modifier = Modifier.height(8.dp))
            when (passwordHiddenState) {
                is HiddenState.Empty -> {}
                is HiddenState.Concealed -> SectionSubtitle(text = sectionContent.asAnnotatedString())
                is HiddenState.Revealed -> {
                    SectionSubtitle(
                        text = sectionContent.toPasswordAnnotatedString(
                            digitColor = ProtonTheme.colors.notificationError,
                            symbolColor = ProtonTheme.colors.notificationSuccess,
                            letterColor = ProtonTheme.colors.textNorm
                        )
                    )
                }

            }
        }
        if (icon != null && actionContent != null) {
            Circle(
                backgroundColor = PassTheme.colors.loginInteractionNormMinor1,
                onClick = { onTogglePasswordClick() }
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = stringResource(actionContent),
                    tint = PassTheme.colors.loginInteractionNormMajor2
                )
            }
        }
    }
}

data class LoginPasswordUIState(
    val sectionContent: String,
    val icon: Int?,
    val actionContent: Int?
)
