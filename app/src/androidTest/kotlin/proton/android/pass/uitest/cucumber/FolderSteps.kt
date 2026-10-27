/*
 * Copyright (c) 2026 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.uitest.cucumber

import android.content.Intent
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidTest
import io.cucumber.java.After
import io.cucumber.java.Before
import io.cucumber.java.Scenario
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.cucumber.junit.WithJunitRule
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.auth.domain.testing.LoginTestHelper
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.test.quark.data.User
import me.proton.test.fusion.FusionConfig
import org.junit.Rule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import proton.android.pass.data.api.errors.ItemNotFoundError
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.data.api.usecases.CreateItem
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.ObserveItemById
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.android.pass.data.api.usecases.capabilities.CanCreateFolder
import proton.android.pass.data.api.usecases.folders.CreateFolder
import proton.android.pass.data.api.usecases.folders.DeleteFolders
import proton.android.pass.data.api.usecases.folders.ObserveFolderLimits
import proton.android.pass.data.api.usecases.folders.ObserveFoldersByParentId
import proton.android.pass.data.api.usecases.folders.RefreshFolders
import proton.android.pass.data.impl.network.RemoteApiException
import proton.android.pass.data.impl.network.RemoteResult
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.SharePermissionFlag
import proton.android.pass.domain.hasFlag
import proton.android.pass.domain.toPermissions
import proton.android.pass.features.home.HomeScreenTestTag
import proton.android.pass.features.onboarding.OnBoardingPageName
import proton.android.pass.features.onboarding.OnBoardingScreenTestTag
import proton.android.pass.ui.MainActivity
import proton.android.pass.uitest.BaseTest
import proton.android.pass.uitest.TestUsers
import proton.android.pass.uitest.robot.DeleteFolderDialogRobot
import proton.android.pass.uitest.robot.FolderRobot
import proton.android.pass.uitest.robot.HomeRobot
import proton.android.pass.uitest.robot.OnBoardingRobot
import proton.android.pass.uitest.robot.SyncDialogRobot
import proton.android.pass.uitest.robot.TIMEOUT_MILLIS
import proton.android.pass.uitest.rule.MainInitializerRule
import proton.android.pass.uitest.rule.UserLoginRule
import javax.inject.Inject

@HiltAndroidTest
@WithJunitRule
class FolderSteps {

    @get:Rule(order = BaseTest.RuleOrder_10_Initialization)
    val mainInitializerRule = MainInitializerRule()

    @get:Rule(order = BaseTest.RuleOrder_30_ActivityLaunch)
    val composeTestRule = AndroidComposeTestRule(
        activityRule = ActivityScenarioRule<MainActivity>(
            Intent().setClassName(
                InstrumentationRegistry.getInstrumentation().targetContext.packageName,
                MainActivity::class.java.name
            )
        ),
        activityProvider = { rule ->
            var activity: MainActivity? = null
            rule.scenario.onActivity { activity = it }
            requireNotNull(activity)
        }
    )

    @Inject
    lateinit var loginTestHelper: LoginTestHelper

    @Inject
    lateinit var accountManager: AccountManager

    @Inject
    lateinit var observeVaults: ObserveVaults

    @Inject
    lateinit var canCreateFolder: CanCreateFolder

    @Inject
    lateinit var observeFolderLimits: ObserveFolderLimits

    @Inject
    lateinit var createFolder: CreateFolder

    @Inject
    lateinit var deleteFolders: DeleteFolders

    @Inject
    lateinit var observeFoldersByParentId: ObserveFoldersByParentId

    @Inject
    lateinit var refreshFolders: RefreshFolders

    @Inject
    lateinit var observeCurrentUser: ObserveCurrentUser

    @Inject
    lateinit var createItem: CreateItem

    @Inject
    lateinit var observeItemById: ObserveItemById

    private val createdFolders = mutableListOf<Pair<ShareId, FolderId>>()

    private var uiFolderVault: ShareId? = null

    private val uiFolderNames = mutableListOf<String>()

    private var nestedFolderVault: String? = null

    private var nestedFolderNames: List<String> = emptyList()

    private var folderPendingDeletion: String? = null

    private val rootFolders = mutableMapOf<String, FolderId>()

    private val itemsByFolder = mutableMapOf<String, MutableList<Pair<ShareId, ItemId>>>()

    init {
        FusionConfig.Compose.testRule.set(composeTestRule)
        FusionConfig.Compose.useUnmergedTree.set(true)
    }

    private var scenarioUser: User? = null

    private val usersByTag: Map<String, () -> User?> = mapOf(
        PLUS_TAG to { TestUsers.plus },
        FREE_TAG to { TestUsers.free },
        FOLDER_LIMIT_TAG to { TestUsers.folderLimit },
        SIGNED_OUT_TAG to { null }
    )

    @Before
    fun selectUserForScenario(scenario: Scenario) {
        val userTags = scenario.sourceTagNames.filter(usersByTag::containsKey).distinct()
        check(userTags.size == 1) {
            "Scenario '${scenario.name}' needs exactly one of ${usersByTag.keys}, found $userTags"
        }
        scenarioUser = usersByTag.getValue(userTags.single())()
    }

    @Given("I am signed in")
    fun signedIn() {
        scenarioUser?.let(::signIn) ?: assertSignedIn()
    }

    @Given("I am signed in with a plan that allows folders")
    fun signedInWithFoldersPlan() {
        signIn(TestUsers.plus)
    }

    private fun signIn(user: User) {
        if (!isSignedInAs(user)) {
            retryLoginWhenUnavailable {
                loginTestHelper.logoutAll()
                UserLoginRule(
                    quark = { BaseTest.quark },
                    loginTestHelper = { loginTestHelper },
                    testUser = user,
                    shouldSeedUser = false
                ).apply(
                    object : Statement() {
                        override fun evaluate() = Unit
                    },
                    Description.EMPTY
                ).evaluate()
            }
        }
        reachHome()
    }

    private fun retryLoginWhenUnavailable(block: () -> Unit) {
        var backoffMillis = RETRY_INITIAL_BACKOFF_MILLIS
        repeat(RETRY_ATTEMPTS - 1) {
            try {
                return block()
            } catch (exception: ApiException) {
                val status = (exception.error as? ApiResult.Error.Http)?.httpCode
                if (status !in RETRYABLE_HTTP_STATUSES) throw exception
            }
            Thread.sleep(backoffMillis)
            backoffMillis *= 2
        }
        block()
    }

    private fun assertSignedIn() {
        val readyAccount = runBlocking {
            withTimeoutOrNull(TIMEOUT_MILLIS) {
                accountManager.getAccounts().first { accounts -> accounts.any { it.state == AccountState.Ready } }
            }
        }
        check(readyAccount != null) { "No account is signed in" }
        reachHome()
    }

    private fun reachHome() {
        SyncDialogRobot.completeSyncIfDisplayed()
        composeTestRule.waitUntil(TIMEOUT_MILLIS) {
            nodeExists(hasTestTag(OnBoardingScreenTestTag.screen)) ||
                nodeExists(hasTestTag(HomeScreenTestTag.SCREEN))
        }
        if (nodeExists(hasTestTag(OnBoardingScreenTestTag.screen))) {
            OnBoardingRobot.onBoardingScreenDisplayed()
                .clickSkip(OnBoardingPageName.Autofill)
                .clickMain(OnBoardingPageName.Last)
            SyncDialogRobot.completeSyncIfDisplayed()
        }
        HomeRobot.homeScreenDisplayed()
    }

    @Given("I have a vault {string} where I can create items")
    fun vaultWhereICanCreateItems(vaultName: String) {
        val vault = runBlocking { observeVaults(includeHidden = false).first() }
            .firstOrNull { it.name == vaultName }
        requireNotNull(vault) { "No vault named $vaultName" }
        check(vault.role.toPermissions().hasFlag(SharePermissionFlag.Create)) {
            "Vault $vaultName does not allow creating items"
        }
    }

    @Given("I have a vault {string} where I can only view items")
    fun vaultWhereICanOnlyViewItems(vaultName: String) {
        val permissions = vaultNamed(vaultName).role.toPermissions()
        check(permissions.hasFlag(SharePermissionFlag.Read)) { "Vault $vaultName does not allow viewing items" }
        check(!permissions.hasFlag(SharePermissionFlag.Create)) { "Vault $vaultName allows creating items" }
    }

    @Given("my plan allows folders")
    fun planAllowsFolders() {
        check(planAllowsFolderCreation()) { "Plan does not allow folders" }
    }

    @Given("my plan does not allow folders")
    fun planDoesNotAllowFolders() {
        check(!planAllowsFolderCreation()) { "Plan allows folders" }
    }

    private fun planAllowsFolderCreation(): Boolean = runBlocking {
        val vault = observeVaults(includeHidden = false).first().firstOrNull()
        requireNotNull(vault) { "No vault available to check the plan" }
        canCreateFolder(vault.shareId).first().planAllows
    }

    @Given("the vault {string} contains a folder {string}")
    fun vaultContainsFolder(vaultName: String, folderName: String) {
        val vault = runBlocking { observeVaults(includeHidden = false).first() }
            .first { it.name == vaultName }
        runBlocking {
            deleteFoldersNamed(vault.shareId, folderName)
            createdFolders += vault.shareId to createFolder(vault.shareId, null, folderName).folderId
        }
    }

    @Given("the vault {string} already has the maximum number of folders at its root")
    fun vaultHasMaximumRootFolders(vaultName: String) {
        val shareId = vaultNamed(vaultName).shareId
        runBlocking {
            val limits = observeFolderLimits().first()
            fillRootFolders(shareId, limits.maxChildren)
        }
    }

    @Given("the vault {string} already contains the maximum number of folders in total")
    fun vaultHasMaximumFoldersInTotal(vaultName: String) {
        val shareId = vaultNamed(vaultName).shareId
        runBlocking {
            val maxCount = observeFolderLimits().first().maxCount
            val total = countFolders(shareId, null)
            check(total == maxCount) { "Vault $vaultName has $total folders, expected $maxCount" }
        }
    }

    @Given("the vault {string} contains folders nested to the maximum depth")
    fun vaultContainsFoldersNestedToMaximumDepth(vaultName: String) {
        val shareId = vaultNamed(vaultName).shareId
        runBlocking {
            deleteFoldersNamed(shareId, "$NESTED_PREFIX 1")
            val maxDepth = observeFolderLimits().first().maxDepth
            var parentId: FolderId? = null
            val names = (1..maxDepth).map { "$NESTED_PREFIX $it" }
            names.forEach { name -> parentId = createTracked(shareId, parentId, name) }
            nestedFolderVault = vaultName
            nestedFolderNames = names
        }
    }

    @Given("the folder {string} already contains the maximum number of subfolders")
    fun folderHasMaximumSubfolders(folderName: String) {
        val shareId = vaultNamed(DEFAULT_VAULT_NAME).shareId
        runBlocking {
            deleteFoldersNamed(shareId, folderName)
            val parentId = createTracked(shareId, null, folderName)
            repeat(observeFolderLimits().first().maxChildren) { index ->
                createTracked(shareId, parentId, "$FILLER_PREFIX${index + 1}")
            }
        }
    }

    private suspend fun deleteInBatches(shareId: ShareId, folderIds: List<FolderId>) {
        folderIds.chunked(DELETE_BATCH_SIZE).forEach { batch -> retryWhenUnavailable { deleteFolders(shareId, batch) } }
    }

    private suspend fun fillRootFolders(shareId: ShareId, maxChildren: Int): List<FolderId> {
        val leftovers = observeFoldersByParentId(shareId, null).first().filter { it.name.startsWith(FILLER_PREFIX) }
        deleteInBatches(shareId, leftovers.map { it.folderId })
        val existing = observeFoldersByParentId(shareId, null).first().size
        return List((maxChildren - existing).coerceAtLeast(0)) { index ->
            createTracked(shareId, null, "$FILLER_PREFIX${index + 1}")
        }
    }

    private suspend fun createTracked(
        shareId: ShareId,
        parentId: FolderId?,
        name: String
    ): FolderId {
        var attempted = false
        val folderId = retryWhenUnavailable {
            val alreadyCreated = if (attempted) existingFolderId(shareId, parentId, name) else null
            attempted = true
            alreadyCreated ?: createFolder(shareId, parentId, name).folderId
        }
        createdFolders += shareId to folderId
        return folderId
    }

    private suspend fun existingFolderId(
        shareId: ShareId,
        parentId: FolderId?,
        name: String
    ): FolderId? {
        safeRunCatching { refreshFolders(observeCurrentUser().first().userId, setOf(shareId)) }
        return observeFoldersByParentId(shareId, parentId).first().firstOrNull { it.name == name }?.folderId
    }

    private suspend fun <T> retryWhenUnavailable(block: suspend () -> T): T {
        var backoffMillis = RETRY_INITIAL_BACKOFF_MILLIS
        repeat(RETRY_ATTEMPTS - 1) {
            try {
                return block()
            } catch (exception: RemoteApiException) {
                val status = (exception.error as? RemoteResult.Error.Http)?.status
                if (status !in RETRYABLE_HTTP_STATUSES) throw exception
            }
            delay(backoffMillis)
            backoffMillis *= 2
        }
        return block()
    }

    private suspend fun allFolderIds(shareId: ShareId, parentId: FolderId?): Set<FolderId> =
        observeFoldersByParentId(shareId, parentId).first()
            .flatMap { setOf(it.folderId) + allFolderIds(shareId, it.folderId) }
            .toSet()

    private suspend fun folderNamesAnywhere(shareId: ShareId, parentId: FolderId?): Set<String> =
        observeFoldersByParentId(shareId, parentId).first()
            .flatMap { setOf(it.name) + folderNamesAnywhere(shareId, it.folderId) }
            .toSet()

    private suspend fun rootFolderNamed(shareId: ShareId, name: String): FolderId = rootFolders.getOrPut(name) {
        deleteFoldersNamed(shareId, name)
        createTracked(shareId, null, name)
    }

    private suspend fun addItems(
        shareId: ShareId,
        folderId: FolderId,
        folderName: String,
        count: Int
    ) {
        repeat(count) { index ->
            val item = createItem(
                shareId = shareId,
                folderId = folderId,
                itemContents = ItemContents.Note(
                    title = "$folderName item ${index + 1}",
                    note = "",
                    customFields = emptyList()
                )
            )
            itemsByFolder.getOrPut(folderName) { mutableListOf() } += shareId to item.id
        }
    }

    private suspend fun countFolders(shareId: ShareId, parentId: FolderId?): Int =
        observeFoldersByParentId(shareId, parentId).first().sumOf { 1 + countFolders(shareId, it.folderId) }

    @After
    fun deleteCreatedFolders() {
        runBlocking {
            createdFolders
                .asReversed()
                .groupBy({ it.first }, { it.second })
                .forEach { (shareId, folderIds) ->
                    val remaining = allFolderIds(shareId, null)
                    deleteInBatches(shareId, folderIds.filter { it in remaining })
                }
        }
        createdFolders.clear()
        rootFolders.clear()
        itemsByFolder.clear()
        folderPendingDeletion = null
        uiFolderVault?.let { shareId ->
            uiFolderNames.forEach { name -> runBlocking { deleteFoldersNamed(shareId, name) } }
        }
        uiFolderVault = null
        uiFolderNames.clear()
        nestedFolderVault = null
        nestedFolderNames = emptyList()
    }

    private suspend fun deleteFoldersNamed(shareId: ShareId, name: String) {
        val leftovers = observeFoldersByParentId(shareId, null).first().filter { it.name == name }
        if (leftovers.isNotEmpty()) {
            retryWhenUnavailable { deleteFolders(shareId, leftovers.map { it.folderId }) }
        }
    }

    @When("I open the options of the vault {string}")
    fun openOptionsOfVault(vaultName: String) {
        HomeRobot.openDrawer().openOptionsOfVault(vaultName)
    }

    @Then("I cannot add a folder to it")
    fun cannotAddFolder() {
        FolderRobot.assertCannotAddFolder()
    }

    @When("I choose to add a folder to the vault {string}")
    fun chooseToAddFolder(vaultName: String) {
        uiFolderVault = vaultNamed(vaultName).shareId
        FolderRobot.chooseAddFolderTo(vaultName)
    }

    @Then("I see the message {string}")
    fun seeMessage(message: String) {
        FolderRobot.assertMessageShown(message)
    }

    @Then("the vault {string} contains a folder {string} at its root")
    fun vaultContainsFolderAtRoot(vaultName: String, folderName: String) {
        val shareId = vaultNamed(vaultName).shareId
        composeTestRule.waitUntil(TIMEOUT_MILLIS) {
            runBlocking { observeFoldersByParentId(shareId, null).first() }.any { it.name == folderName }
        }
    }

    @Then("the create button is disabled")
    fun createButtonDisabled() {
        FolderRobot.assertCreateDisabled()
    }

    @Then("I see a {string} error")
    fun seeError(error: String) {
        FolderRobot.assertErrorShown(error)
    }

    @When("I confirm the creation")
    fun confirmCreation() {
        FolderRobot.confirmCreation()
    }

    @Then("folder creation is offered as an upgrade")
    fun folderCreationOfferedAsUpgrade() {
        FolderRobot.assertFolderCreationOfferedAsUpgrade()
    }

    @When("I choose to delete the folder {string}")
    fun chooseToDeleteFolder(folderName: String) {
        folderPendingDeletion = folderName
        HomeRobot.openDrawer().openOptionsOf(folderName).chooseDeletePermanently()
    }

    @When("I choose to rename the folder {string}")
    fun chooseToRenameFolder(folderName: String) {
        HomeRobot.openDrawer().openOptionsOf(folderName).chooseRename()
    }

    @When("I confirm the change")
    fun confirmTheChange() {
        FolderRobot.confirmRename()
    }

    @Then("the vault {string} does not contain a folder {string} at its root")
    fun vaultDoesNotContainFolderAtRoot(vaultName: String, folderName: String) {
        val shareId = vaultNamed(vaultName).shareId
        composeTestRule.waitUntil(TIMEOUT_MILLIS) {
            runBlocking { observeFoldersByParentId(shareId, null).first() }.none { it.name == folderName }
        }
    }

    @When("I choose to add a subfolder to the folder {string}")
    fun chooseToAddSubfolder(folderName: String) {
        FolderRobot.chooseAddSubfolderTo(folderName)
    }

    @Then("the folder {string} contains a subfolder {string}")
    fun folderContainsSubfolder(folderName: String, subfolderName: String) {
        val shareId = vaultNamed(DEFAULT_VAULT_NAME).shareId
        composeTestRule.waitUntil(TIMEOUT_MILLIS) {
            runBlocking {
                observeFoldersByParentId(shareId, null).first()
                    .filter { it.name == folderName }
                    .any { parent ->
                        observeFoldersByParentId(shareId, parent.folderId).first().any { it.name == subfolderName }
                    }
            }
        }
    }

    @When("I open the options of the folder at the deepest level")
    fun openOptionsOfDeepestFolder() {
        val vaultName = requireNotNull(nestedFolderVault) { "No nested folders were created" }
        HomeRobot.openDrawer().openOptionsOfNested(vaultName, nestedFolderNames)
    }

    @When("I open the options of the folder {string}")
    fun openOptionsOfFolder(folderName: String) {
        HomeRobot.openDrawer().openOptionsOf(folderName)
    }

    @Then("I cannot add a subfolder to it")
    fun cannotAddSubfolder() {
        FolderRobot.assertCannotAddSubfolder()
    }

    @Then("I am asked to enter the folder name to confirm deletion")
    fun askedToEnterFolderName() {
        DeleteFolderDialogRobot.assertAsksForFolderName()
    }

    @When("I enter the folder name {string}")
    fun enterFolderName(folderName: String) {
        if (uiFolderVault != null) uiFolderNames += folderName
        DeleteFolderDialogRobot.enterFolderName(folderName)
    }

    @Then("the delete button is disabled")
    fun deleteButtonDisabled() {
        DeleteFolderDialogRobot.assertDeleteDisabled()
    }

    @Then("the delete button is enabled")
    fun deleteButtonEnabled() {
        DeleteFolderDialogRobot.assertDeleteEnabled()
    }

    @Given("the vault {string} contains an empty folder {string}")
    fun vaultContainsEmptyFolder(vaultName: String, folderName: String) {
        val shareId = vaultNamed(vaultName).shareId
        runBlocking { rootFolderNamed(shareId, folderName) }
    }

    @Given("the folder {string} contains {int} items")
    fun folderContainsItems(folderName: String, count: Int) {
        val shareId = vaultNamed(DEFAULT_VAULT_NAME).shareId
        runBlocking { addItems(shareId, rootFolderNamed(shareId, folderName), folderName, count) }
    }

    @Given("the folder {string} contains a subfolder {string} with {int} item")
    fun folderContainsSubfolderWithItems(
        folderName: String,
        subfolderName: String,
        count: Int
    ) {
        val shareId = vaultNamed(DEFAULT_VAULT_NAME).shareId
        runBlocking {
            val subfolderId = createTracked(shareId, rootFolderNamed(shareId, folderName), subfolderName)
            addItems(shareId, subfolderId, subfolderName, count)
        }
    }

    @Given("the vault {string} contains a folder {string} with {int} item")
    fun vaultContainsFolderWithItems(
        vaultName: String,
        folderName: String,
        count: Int
    ) {
        val shareId = vaultNamed(vaultName).shareId
        runBlocking { addItems(shareId, rootFolderNamed(shareId, folderName), folderName, count) }
    }

    @Given("I am browsing the contents of the folder {string}")
    fun browsingFolder(folderName: String) {
        val shareId = vaultNamed(DEFAULT_VAULT_NAME).shareId
        runBlocking { rootFolderNamed(shareId, folderName) }
        HomeRobot.openDrawer().openFolder(folderName).assertViewing(folderName)
    }

    @Then("I am warned that the folder and all its contents will be permanently deleted")
    fun warnedFolderContentsDeleted() {
        DeleteFolderDialogRobot.assertWarnsAllContentsDeleted(pendingFolderName())
    }

    @Then("I am warned that {int} items will be permanently deleted")
    fun warnedItemsDeleted(count: Int) {
        DeleteFolderDialogRobot.assertWarnsItemsDeleted(count, pendingFolderName())
    }

    @When("I confirm the deletion")
    fun confirmDeletion() {
        DeleteFolderDialogRobot.confirmDeletion()
    }

    @When("I delete the folder {string} by entering its name")
    fun deleteFolderByEnteringItsName(folderName: String) {
        chooseToDeleteFolder(folderName)
        DeleteFolderDialogRobot.enterFolderName(folderName).confirmDeletion()
    }

    @Then("the vault {string} does not contain a folder {string}")
    fun vaultDoesNotContainFolder(vaultName: String, folderName: String) {
        val shareId = vaultNamed(vaultName).shareId
        composeTestRule.waitUntil(TIMEOUT_MILLIS) {
            runBlocking { folderName !in folderNamesAnywhere(shareId, null) }
        }
    }

    @Then("the item in {string} no longer exists")
    fun itemNoLongerExists(folderName: String) {
        val items = requireNotNull(itemsByFolder[folderName]) { "No items were created in $folderName" }
        composeTestRule.waitUntil(TIMEOUT_MILLIS) {
            items.none { (shareId, itemId) -> itemExists(shareId, itemId) }
        }
    }

    @Then("the folder {string} still contains its item")
    fun folderStillContainsItem(folderName: String) {
        val items = requireNotNull(itemsByFolder[folderName]) { "No items were created in $folderName" }
        items.forEach { (shareId, itemId) ->
            check(itemExists(shareId, itemId)) { "The item in $folderName no longer exists" }
        }
    }

    @Then("I am no longer viewing the folder {string}")
    fun noLongerViewingFolder(folderName: String) {
        FolderRobot.assertNotViewing(folderName)
    }

    private fun itemExists(shareId: ShareId, itemId: ItemId): Boolean = runBlocking {
        try {
            observeItemById(shareId = shareId, itemId = itemId).first() != null
        } catch (_: ItemNotFoundError) {
            false
        }
    }

    private fun pendingFolderName(): String = requireNotNull(folderPendingDeletion) { "No folder is being deleted" }

    private fun vaultNamed(vaultName: String) = requireNotNull(
        runBlocking { observeVaults(includeHidden = false).first() }.firstOrNull { it.name == vaultName }
    ) {
        "No vault named $vaultName"
    }

    private fun isSignedInAs(user: User): Boolean {
        val account = runBlocking {
            accountManager.getPrimaryUserId().first()?.let { accountManager.getAccount(it).first() }
        } ?: return false
        return account.username == user.name || account.email == user.email
    }

    private fun nodeExists(matcher: SemanticsMatcher): Boolean =
        composeTestRule.onAllNodes(matcher, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()

    private companion object {
        const val DELETE_BATCH_SIZE = 20
        const val HTTP_BAD_GATEWAY = 502
        const val HTTP_SERVICE_UNAVAILABLE = 503
        val RETRYABLE_HTTP_STATUSES = setOf(HTTP_BAD_GATEWAY, HTTP_SERVICE_UNAVAILABLE)
        const val RETRY_ATTEMPTS = 4
        const val RETRY_INITIAL_BACKOFF_MILLIS = 2_000L
        const val FILLER_PREFIX = "Filler "
        const val NESTED_PREFIX = "Level"
        const val DEFAULT_VAULT_NAME = "Personal"
        const val PLUS_TAG = "@user-plus"
        const val FREE_TAG = "@user-free"
        const val FOLDER_LIMIT_TAG = "@user-folderlimit"
        const val SIGNED_OUT_TAG = "@signed-out"
    }
}
