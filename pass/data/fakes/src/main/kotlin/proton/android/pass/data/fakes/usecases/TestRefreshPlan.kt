package proton.android.pass.data.fakes.usecases

import proton.android.pass.data.api.usecases.RefreshPlan
import javax.inject.Inject

class TestRefreshPlan @Inject constructor() : RefreshPlan {

    override suspend fun invoke() {
    }
}
