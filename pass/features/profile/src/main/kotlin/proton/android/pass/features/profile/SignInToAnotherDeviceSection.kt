package proton.android.pass.features.profile

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import me.proton.core.devicemigration.presentation.settings.SignInToAnotherDeviceItem
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.composecomponents.impl.setting.SettingOption
import me.proton.core.presentation.R as CoreR

@Composable
internal fun SignInToAnotherDeviceSection(modifier: Modifier = Modifier) {
    SignInToAnotherDeviceItem(
        content = { label, onClick ->
            PassDivider()

            SettingOption(
                modifier = modifier,
                text = label,
                onClick = onClick,
                leadingIcon = {
                    Icon(
                        modifier = Modifier
                            .padding(end = 21.dp)
                            .width(40.dp),
                        painter = painterResource(CoreR.drawable.ic_proton_qr_code),
                        contentDescription = null
                    )
                }
            )
        }
    )
}
