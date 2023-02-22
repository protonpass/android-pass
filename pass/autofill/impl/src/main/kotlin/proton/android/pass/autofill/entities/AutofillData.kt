package proton.android.pass.autofill.entities

import proton.android.pass.common.api.Option
import proton.pass.domain.entity.PackageInfo

data class AutofillData(
    val assistInfo: AssistInfo,
    val packageInfo: Option<PackageInfo>
)
