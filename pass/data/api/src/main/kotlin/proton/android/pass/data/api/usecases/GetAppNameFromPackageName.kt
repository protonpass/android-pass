package proton.android.pass.data.api.usecases

import proton.pass.domain.entity.PackageName

interface GetAppNameFromPackageName {
    operator fun invoke(packageName: PackageName): String
}
