package proton.android.pass.data.impl.usecases

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Test
import proton.android.pass.account.fakes.TestAccountManager
import proton.android.pass.data.fakes.usecases.TestGetUserPlan
import proton.android.pass.data.impl.fakes.TestLocalItemDataSource
import proton.android.pass.data.impl.local.ItemWithTotp
import proton.pass.domain.ItemId
import proton.pass.domain.Plan
import proton.pass.domain.PlanType
import proton.pass.domain.ShareId
import kotlin.time.Duration.Companion.days

class CanDisplayTotpImplTest {

    private lateinit var instance: CanDisplayTotpImpl
    private lateinit var getUserPlan: TestGetUserPlan
    private lateinit var dataSource: TestLocalItemDataSource

    @Before
    fun setup() {
        getUserPlan = TestGetUserPlan()
        dataSource = TestLocalItemDataSource()
        instance = CanDisplayTotpImpl(
            getUserPlan = getUserPlan,
            localItemDataSource = dataSource,
            accountManager = TestAccountManager().apply {
                sendPrimaryUserId(UserId("123"))
            }
        )
    }

    @Test
    fun `paid plan can display all totps`() = runTest {
        setupWithPlan(PAID_PLAN, totpLimit = 1)

        val items = setupWithItems(10)
        items.forEach {
            val canDisplay = instance.invoke(shareId = it.shareId, itemId = it.itemId).first()
            assertThat(canDisplay).isTrue()
        }
    }

    @Test
    fun `trial plan can display all totps`() = runTest {
        setupWithPlan(TRIAL_PLAN, totpLimit = 1)

        val items = setupWithItems(10)
        items.forEach {
            val canDisplay = instance.invoke(shareId = it.shareId, itemId = it.itemId).first()
            assertThat(canDisplay).isTrue()
        }
    }

    @Test
    fun `free plan can display first N totps`() = runTest {
        setupWithPlan(PlanType.Free, totpLimit = 3)

        val items = setupWithItems(10)
        // First 3 totps should be allowed
        items.take(3).forEach {
            val canDisplay = instance.invoke(shareId = it.shareId, itemId = it.itemId).first()
            assertThat(canDisplay).isTrue()
        }

        // Next 7 totps should not be allowed
        items.drop(3).forEach {
            val canDisplay = instance.invoke(shareId = it.shareId, itemId = it.itemId).first()
            assertThat(canDisplay).isFalse()
        }
    }

    private fun setupWithItems(count: Int): List<ItemWithTotp> {
        val items = (0 until count).map {
            ItemWithTotp(
                shareId = ShareId("share-$it"),
                itemId = ItemId("item-$it"),
                createTime = Clock.System.now().minus(it.days),
            )
        }.reversed() // Reverse so older items are first
        dataSource.emitItemsWithTotp(Result.success(items))
        return items
    }

    private fun setupWithPlan(planType: PlanType, totpLimit: Int) {
        getUserPlan.setResult(
            Result.success(
                Plan(
                    planType = planType,
                    hideUpgrade = false,
                    vaultLimit = 1,
                    aliasLimit = 1,
                    totpLimit = totpLimit,
                    updatedAt = Clock.System.now().epochSeconds
                )
            )
        )
    }

    companion object {
        private val PAID_PLAN = PlanType.Paid("", "")
        private val TRIAL_PLAN = PlanType.Trial("", "", 1)
    }

}
