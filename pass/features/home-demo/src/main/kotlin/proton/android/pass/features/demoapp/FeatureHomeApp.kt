package proton.android.pass.features.demoapp

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import kotlinx.datetime.Clock
import me.proton.core.domain.entity.UserId
import proton.android.pass.account.fakes.FakeAccountManager
import proton.android.pass.autofill.api.AutofillStatus
import proton.android.pass.autofill.api.AutofillSupportedStatus
import proton.android.pass.autofill.fakes.FakeAutofillManager
import proton.android.pass.data.fakes.usecases.FakeObserveItems
import proton.android.pass.data.fakes.usecases.FakeObserveVaults
import proton.android.pass.data.fakes.usecases.FakeObserveVaultsWithItemCount
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareFlags
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault
import proton.android.pass.domain.VaultId
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.test.domain.ItemTestFactory
import timber.log.Timber
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@HiltAndroidApp
class FeatureHomeApp : Application() {

    @Inject
    lateinit var accountManager: FakeAccountManager

    @Inject
    lateinit var observeItems: FakeObserveItems

    @Inject
    lateinit var observeVaults: FakeObserveVaults

    @Inject
    lateinit var observeVaultsWithItemCount: FakeObserveVaultsWithItemCount

    @Inject
    lateinit var autofillManager: FakeAutofillManager

    override fun onCreate() {
        super.onCreate()
        setupAccount()
        setupItems()
        setupVaults()
        setupLogger()

        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.EnabledByOurService))

    }

    private fun setupVaults() {
        val vaults = listOf(
            Vault(
                userId = UserId(""),
                shareId = ShareId(SHARE_ID),
                vaultId = VaultId("main_vault"),
                name = "Main vault",
                createTime = Date(),
                shareFlags = ShareFlags(0)
            ),
            Vault(
                userId = UserId(""),
                shareId = ShareId("OtherShareID"),
                vaultId = VaultId("other_vault"),
                name = "Other vault",
                createTime = Date(),
                shareFlags = ShareFlags(0)
            )
        )
        val vaultsWithItemCount = vaults.map {
            VaultWithItemCount(vault = it, activeItemCount = 100, trashedItemCount = 0)
        }
        observeVaults.sendResult(Result.success(vaults))
        observeVaultsWithItemCount.sendResult(Result.success(vaultsWithItemCount))
    }

    private fun setupItems() {
        val items = createItems(50)
        observeItems.emitValue(items)
    }

    private fun setupLogger() {
        Timber.plant(Timber.DebugTree())
    }

    private fun setupAccount() {
        accountManager.sendPrimaryUserId(UserId("user1"))
    }

    private fun createItems(itemsPerType: Int): List<Item> {
        val items = mutableListOf<Item>()
        for (i in 0..itemsPerType) {
            val login = ItemTestFactory.createLogin(
                shareId = ShareId(SHARE_ID),
                itemId = ItemId("item_login_$i"),
                title = "Login $i",
                username = "Username $i",
                password = "Password$i"
            )
            items.add(login)

            val creditCard = ItemTestFactory.createCreditCard(
                shareId = ShareId(SHARE_ID),
                itemId = ItemId("item_cc_$i"),
                title = "Card $i",
                holder = "Holder $i",
                number = "${i}23423423"
            )
            items.add(creditCard)

            val note = ItemTestFactory.createNote(
                shareId = ShareId(SHARE_ID),
                itemId = ItemId("item_note_$i"),
                title = "Note $i",
                note = "Note $i"
            )
            items.add(note)

            val alias = ItemTestFactory.createAlias(
                shareId = ShareId(SHARE_ID),
                itemId = ItemId("item_alias_$i"),
                title = "Alias $i",
                alias = "alias$i@domain.test"
            )
            items.add(alias)
        }
        return items
    }

    companion object {
        private const val SHARE_ID = "DemoApp-ShareId"
    }
}

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {

    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.System

}
