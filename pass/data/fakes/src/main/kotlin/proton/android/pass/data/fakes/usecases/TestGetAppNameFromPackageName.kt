package proton.android.pass.data.fakes.usecases

import proton.android.pass.data.api.usecases.GetAppNameFromPackageName
import proton.pass.domain.entity.PackageName
import javax.inject.Inject

class TestGetAppNameFromPackageName @Inject constructor() : GetAppNameFromPackageName {

    private var result: String = ""

    fun setResult(result: String) {
        this.result = result
    }

    override fun invoke(packageName: PackageName): String = result
}
