package proton.android.pass.account.fakes.payment

import kotlinx.coroutines.flow.Flow
import me.proton.core.payment.domain.entity.Purchase
import me.proton.core.payment.domain.repository.PurchaseRepository
import javax.inject.Inject

@Suppress("NotImplementedDeclaration")
class FakePurchaseRepository @Inject constructor() : PurchaseRepository {
    override fun observePurchase(planName: String): Flow<Purchase?> {
        TODO("Not yet implemented")
    }

    override fun observePurchases(): Flow<List<Purchase>> {
        TODO("Not yet implemented")
    }

    override suspend fun getPurchase(planName: String): Purchase? {
        TODO("Not yet implemented")
    }

    override suspend fun getPurchases(): List<Purchase> {
        TODO("Not yet implemented")
    }

    override suspend fun upsertPurchase(purchase: Purchase) {
        TODO("Not yet implemented")
    }

    override suspend fun deletePurchase(planName: String) {
        TODO("Not yet implemented")
    }

    override fun onPurchaseStateChanged(initialState: Boolean): Flow<Purchase> {
        TODO("Not yet implemented")
    }
}
