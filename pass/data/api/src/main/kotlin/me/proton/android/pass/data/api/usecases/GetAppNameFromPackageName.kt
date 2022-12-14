package me.proton.android.pass.data.api.usecases

import me.proton.pass.domain.entity.PackageName

interface GetAppNameFromPackageName {
    operator fun invoke(packageName: PackageName): String
}
