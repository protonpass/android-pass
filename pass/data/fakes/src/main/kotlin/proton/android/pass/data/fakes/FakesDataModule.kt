/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.data.fakes

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import proton.android.pass.data.api.core.repositories.SentinelRepository
import proton.android.pass.data.api.crypto.GetShareAndItemKey
import proton.android.pass.data.api.repositories.AliasRepository
import proton.android.pass.data.api.repositories.AssetLinkRepository
import proton.android.pass.data.api.repositories.BulkInviteRepository
import proton.android.pass.data.api.repositories.BulkMoveToVaultRepository
import proton.android.pass.data.api.repositories.DraftAttachmentRepository
import proton.android.pass.data.api.repositories.DraftRepository
import proton.android.pass.data.api.repositories.InAppMessagesRepository
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.repositories.ItemSyncStatusRepository
import proton.android.pass.data.api.repositories.MetadataResolver
import proton.android.pass.data.api.repositories.PasswordHistoryEntryRepository
import proton.android.pass.data.api.repositories.PendingAttachmentLinkRepository
import proton.android.pass.data.api.repositories.PendingAttachmentUpdaterRepository
import proton.android.pass.data.api.repositories.UserAccessDataRepository
import proton.android.pass.data.api.repositories.UserInviteRepository
import proton.android.pass.data.api.usecases.AcceptInvite
import proton.android.pass.data.api.usecases.ApplyPendingEvents
import proton.android.pass.data.api.usecases.BatchChangeShareVisibility
import proton.android.pass.data.api.usecases.CanDisplayTotp
import proton.android.pass.data.api.usecases.CanPerformPaidAction
import proton.android.pass.data.api.usecases.CancelShareInvite
import proton.android.pass.data.api.usecases.ChangeAliasStatus
import proton.android.pass.data.api.usecases.CheckCanAddressesBeInvited
import proton.android.pass.data.api.usecases.CheckMasterPassword
import proton.android.pass.data.api.usecases.CheckPin
import proton.android.pass.data.api.usecases.ClearPin
import proton.android.pass.data.api.usecases.ClearTrash
import proton.android.pass.data.api.usecases.ClearUserData
import proton.android.pass.data.api.usecases.ConfirmNewUserInvite
import proton.android.pass.data.api.usecases.CreateAlias
import proton.android.pass.data.api.usecases.CreateItem
import proton.android.pass.data.api.usecases.CreateLoginAndAlias
import proton.android.pass.data.api.usecases.CreatePin
import proton.android.pass.data.api.usecases.CreateVault
import proton.android.pass.data.api.usecases.DeleteItems
import proton.android.pass.data.api.usecases.DeleteVault
import proton.android.pass.data.api.usecases.GetAllKeysByAddress
import proton.android.pass.data.api.usecases.GetDefaultBrowser
import proton.android.pass.data.api.usecases.GetInviteUserMode
import proton.android.pass.data.api.usecases.GetItemActions
import proton.android.pass.data.api.usecases.GetItemByAliasEmail
import proton.android.pass.data.api.usecases.GetItemById
import proton.android.pass.data.api.usecases.GetShareById
import proton.android.pass.data.api.usecases.GetSuggestedAutofillItems
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.GetVaultByShareId
import proton.android.pass.data.api.usecases.GetVaultMembers
import proton.android.pass.data.api.usecases.GetVaultWithItemCountById
import proton.android.pass.data.api.usecases.InitialWorkerLauncher
import proton.android.pass.data.api.usecases.InviteToVault
import proton.android.pass.data.api.usecases.LeaveShare
import proton.android.pass.data.api.usecases.MigrateItems
import proton.android.pass.data.api.usecases.MigrateVault
import proton.android.pass.data.api.usecases.ObserveAddressesByUserId
import proton.android.pass.data.api.usecases.ObserveAliasDetails
import proton.android.pass.data.api.usecases.ObserveAliasOptions
import proton.android.pass.data.api.usecases.ObserveAllShares
import proton.android.pass.data.api.usecases.ObserveAppNeedsUpdate
import proton.android.pass.data.api.usecases.ObserveConfirmedInviteToken
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.ObserveCurrentUserSettings
import proton.android.pass.data.api.usecases.ObserveEncryptedItems
import proton.android.pass.data.api.usecases.ObserveGlobalMonitorState
import proton.android.pass.data.api.usecases.ObserveGroupMembersByGroup
import proton.android.pass.data.api.usecases.ObserveInviteRecommendations
import proton.android.pass.data.api.usecases.ObserveInvites
import proton.android.pass.data.api.usecases.ObserveItemById
import proton.android.pass.data.api.usecases.ObserveItemCount
import proton.android.pass.data.api.usecases.ObserveItems
import proton.android.pass.data.api.usecases.ObserveMFACount
import proton.android.pass.data.api.usecases.ObservePinnedItems
import proton.android.pass.data.api.usecases.ObserveUpgradeInfo
import proton.android.pass.data.api.usecases.ObserveUserAccessData
import proton.android.pass.data.api.usecases.ObserveUserEmail
import proton.android.pass.data.api.usecases.ObserveVaultWithItemCountById
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.android.pass.data.api.usecases.PerformSync
import proton.android.pass.data.api.usecases.PinItem
import proton.android.pass.data.api.usecases.PinItems
import proton.android.pass.data.api.usecases.PromoteNewInviteToInvite
import proton.android.pass.data.api.usecases.RefreshBreaches
import proton.android.pass.data.api.usecases.RefreshContent
import proton.android.pass.data.api.usecases.RefreshGroupInvites
import proton.android.pass.data.api.usecases.RefreshSharesAndEnqueueSync
import proton.android.pass.data.api.usecases.RefreshUserAccess
import proton.android.pass.data.api.usecases.RefreshUserInvites
import proton.android.pass.data.api.usecases.RejectInvite
import proton.android.pass.data.api.usecases.RemoveShareMember
import proton.android.pass.data.api.usecases.ResendShareInvite
import proton.android.pass.data.api.usecases.ResetAppToDefaults
import proton.android.pass.data.api.usecases.RestoreAllItems
import proton.android.pass.data.api.usecases.RestoreItems
import proton.android.pass.data.api.usecases.TransferVaultOwnership
import proton.android.pass.data.api.usecases.TrashItems
import proton.android.pass.data.api.usecases.UnpinItem
import proton.android.pass.data.api.usecases.UnpinItems
import proton.android.pass.data.api.usecases.UpdateAlias
import proton.android.pass.data.api.usecases.UpdateAliasName
import proton.android.pass.data.api.usecases.UpdateAutofillItem
import proton.android.pass.data.api.usecases.UpdateItem
import proton.android.pass.data.api.usecases.UpdateVault
import proton.android.pass.data.api.usecases.aliascontact.ObserveAliasContacts
import proton.android.pass.data.api.usecases.attachments.ClearAttachments
import proton.android.pass.data.api.usecases.attachments.DownloadAttachment
import proton.android.pass.data.api.usecases.attachments.LinkAttachmentsToItem
import proton.android.pass.data.api.usecases.attachments.ObserveAllItemRevisionAttachments
import proton.android.pass.data.api.usecases.attachments.ObserveDetailItemAttachments
import proton.android.pass.data.api.usecases.attachments.ObserveUpdateItemAttachments
import proton.android.pass.data.api.usecases.attachments.RenameAttachments
import proton.android.pass.data.api.usecases.attachments.UploadAttachment
import proton.android.pass.data.api.usecases.breach.AddBreachCustomEmail
import proton.android.pass.data.api.usecases.breach.MarkEmailBreachAsResolved
import proton.android.pass.data.api.usecases.breach.ObserveAllBreachByUserId
import proton.android.pass.data.api.usecases.breach.ObserveBreachAliasEmails
import proton.android.pass.data.api.usecases.breach.ObserveHasBreaches
import proton.android.pass.data.api.usecases.breach.ObserveBreachCustomEmails
import proton.android.pass.data.api.usecases.breach.ObserveBreachEmailReport
import proton.android.pass.data.api.usecases.breach.ObserveBreachProtonEmails
import proton.android.pass.data.api.usecases.breach.ObserveBreachesForAliasEmail
import proton.android.pass.data.api.usecases.breach.ObserveBreachesForCustomEmail
import proton.android.pass.data.api.usecases.breach.ObserveBreachesForEmail
import proton.android.pass.data.api.usecases.breach.ObserveBreachesForProtonEmail
import proton.android.pass.data.api.usecases.breach.ObserveCustomEmailSuggestions
import proton.android.pass.data.api.usecases.breach.RemoveCustomEmail
import proton.android.pass.data.api.usecases.breach.ResendVerificationCode
import proton.android.pass.data.api.usecases.breach.UpdateGlobalAliasAddressesMonitorState
import proton.android.pass.data.api.usecases.breach.UpdateGlobalProtonAddressesMonitorState
import proton.android.pass.data.api.usecases.breach.UpdateProtonAddressMonitorState
import proton.android.pass.data.api.usecases.breach.VerifyBreachCustomEmail
import proton.android.pass.data.api.usecases.capabilities.CanCreateItemInVault
import proton.android.pass.data.api.usecases.capabilities.CanCreateVault
import proton.android.pass.data.api.usecases.capabilities.CanManageVaultAccess
import proton.android.pass.data.api.usecases.capabilities.CanMigrateVault
import proton.android.pass.data.api.usecases.capabilities.CanOrganiseVaults
import proton.android.pass.data.api.usecases.capabilities.CanShareShare
import proton.android.pass.data.api.usecases.credentials.passkeys.GetPasskeyCredentialItems
import proton.android.pass.data.api.usecases.credentials.passwords.GetPasswordCredentialItems
import proton.android.pass.data.api.usecases.defaultvault.ObserveDefaultVault
import proton.android.pass.data.api.usecases.defaultvault.SetDefaultVault
import proton.android.pass.data.api.usecases.extrapassword.AuthWithExtraPassword
import proton.android.pass.data.api.usecases.extrapassword.CheckLocalExtraPassword
import proton.android.pass.data.api.usecases.extrapassword.HasExtraPassword
import proton.android.pass.data.api.usecases.extrapassword.RemoveExtraPassword
import proton.android.pass.data.api.usecases.extrapassword.SetupExtraPassword
import proton.android.pass.data.api.usecases.inappmessages.ObserveDeliverableMinimizedPromoInAppMessages
import proton.android.pass.data.api.usecases.inappmessages.ObserveDeliverableModalInAppMessages
import proton.android.pass.data.api.usecases.inappmessages.ObserveDeliverablePromoInAppMessages
import proton.android.pass.data.api.usecases.invites.InviteToItem
import proton.android.pass.data.api.usecases.invites.ObserveInvite
import proton.android.pass.data.api.usecases.items.GetItemCategory
import proton.android.pass.data.api.usecases.items.GetItemOptions
import proton.android.pass.data.api.usecases.items.GetMigrationItemsSelection
import proton.android.pass.data.api.usecases.items.ObserveCanCreateItems
import proton.android.pass.data.api.usecases.items.ObserveEncryptedSharedItems
import proton.android.pass.data.api.usecases.items.ObserveItemRevisions
import proton.android.pass.data.api.usecases.items.ObserveMonitoredItems
import proton.android.pass.data.api.usecases.items.ObserveSharedItemCountSummary
import proton.android.pass.data.api.usecases.items.OpenItemRevision
import proton.android.pass.data.api.usecases.items.UpdateItemFlag
import proton.android.pass.data.api.usecases.organization.ObserveAnyAccountHasEnforcedLock
import proton.android.pass.data.api.usecases.organization.ObserveOrganizationSettings
import proton.android.pass.data.api.usecases.organization.ObserveOrganizationSharingPolicy
import proton.android.pass.data.api.usecases.organization.RefreshOrganizationSettings
import proton.android.pass.data.api.usecases.passkeys.GetPasskeyById
import proton.android.pass.data.api.usecases.passkeys.ObserveItemsWithPasskeys
import proton.android.pass.data.api.usecases.passwordHistoryEntry.AddOnePasswordHistoryEntryToUser
import proton.android.pass.data.api.usecases.passwordHistoryEntry.DeleteOldPasswordHistoryEntry
import proton.android.pass.data.api.usecases.passwordHistoryEntry.DeleteOnePasswordHistoryEntryForUser
import proton.android.pass.data.api.usecases.passwordHistoryEntry.DeletePasswordHistoryEntryForUser
import proton.android.pass.data.api.usecases.passwordHistoryEntry.GetPasswordHistoryEntryForUser
import proton.android.pass.data.api.usecases.passwordHistoryEntry.ObservePasswordHistoryEntryForUser
import proton.android.pass.data.api.usecases.passwords.ObservePasswordConfig
import proton.android.pass.data.api.usecases.passwords.UpdatePasswordConfig
import proton.android.pass.data.api.usecases.plan.ObservePlansWithPrice
import proton.android.pass.data.api.usecases.searchentry.AddSearchEntry
import proton.android.pass.data.api.usecases.searchentry.DeleteAllSearchEntry
import proton.android.pass.data.api.usecases.searchentry.DeleteSearchEntry
import proton.android.pass.data.api.usecases.searchentry.ObserveSearchEntry
import proton.android.pass.data.api.usecases.securelink.DeleteInactiveSecureLinks
import proton.android.pass.data.api.usecases.securelink.DeleteSecureLink
import proton.android.pass.data.api.usecases.securelink.GenerateSecureLink
import proton.android.pass.data.api.usecases.securelink.ObserveActiveSecureLinks
import proton.android.pass.data.api.usecases.securelink.ObserveHasAssociatedSecureLinks
import proton.android.pass.data.api.usecases.securelink.ObserveInactiveSecureLinks
import proton.android.pass.data.api.usecases.securelink.ObserveSecureLink
import proton.android.pass.data.api.usecases.securelink.ObserveSecureLinks
import proton.android.pass.data.api.usecases.securelink.ObserveSecureLinksCount
import proton.android.pass.data.api.usecases.shares.ObserveAutofillShares
import proton.android.pass.data.api.usecases.shares.ObserveHasShares
import proton.android.pass.data.api.usecases.shares.ObserveShare
import proton.android.pass.data.api.usecases.shares.ObserveShareItemMembers
import proton.android.pass.data.api.usecases.shares.ObserveShareItemsCount
import proton.android.pass.data.api.usecases.shares.ObserveSharePendingInvites
import proton.android.pass.data.api.usecases.shares.ObserveSharesByType
import proton.android.pass.data.api.usecases.shares.ObserveSharesItemsCount
import proton.android.pass.data.api.usecases.shares.UpdateShareMemberRole
import proton.android.pass.data.api.usecases.simplelogin.CreateSimpleLoginAliasMailbox
import proton.android.pass.data.api.usecases.simplelogin.DeleteSimpleLoginAliasMailbox
import proton.android.pass.data.api.usecases.simplelogin.DisableSimpleLoginSyncPreference
import proton.android.pass.data.api.usecases.simplelogin.EnableSimpleLoginSync
import proton.android.pass.data.api.usecases.simplelogin.ObserveSimpleLoginAliasDomains
import proton.android.pass.data.api.usecases.simplelogin.ObserveSimpleLoginAliasMailbox
import proton.android.pass.data.api.usecases.simplelogin.ObserveSimpleLoginAliasMailboxes
import proton.android.pass.data.api.usecases.simplelogin.ObserveSimpleLoginAliasSettings
import proton.android.pass.data.api.usecases.simplelogin.ObserveSimpleLoginSyncStatus
import proton.android.pass.data.api.usecases.simplelogin.ResendSimpleLoginAliasMailboxVerificationCode
import proton.android.pass.data.api.usecases.simplelogin.SyncSimpleLoginPendingAliases
import proton.android.pass.data.api.usecases.simplelogin.UpdateSimpleLoginAliasDomain
import proton.android.pass.data.api.usecases.simplelogin.UpdateSimpleLoginAliasMailbox
import proton.android.pass.data.api.usecases.simplelogin.VerifySimpleLoginAliasMailbox
import proton.android.pass.data.api.usecases.tooltips.DisableTooltip
import proton.android.pass.data.api.usecases.tooltips.ObserveTooltipEnabled
import proton.android.pass.data.api.usecases.vaults.ObserveVaultsGroupedByShareId
import proton.android.pass.data.api.usecases.vaults.ObserveVaultsGroupedByVisibility
import proton.android.pass.data.api.work.WorkManagerFacade
import proton.android.pass.data.api.work.WorkerLauncher
import proton.android.pass.data.fakes.crypto.FakeGetShareAndItemKey
import proton.android.pass.data.fakes.repositories.FakeAliasRepository
import proton.android.pass.data.fakes.repositories.FakeAssetLinkRepository
import proton.android.pass.data.fakes.repositories.FakeDraftAttachmentRepository
import proton.android.pass.data.fakes.repositories.FakeInAppMessagesRepository
import proton.android.pass.data.fakes.repositories.FakeMetadataResolver
import proton.android.pass.data.fakes.repositories.FakePasswordHistoryEntryRepository
import proton.android.pass.data.fakes.repositories.FakePendingAttachmentLinkRepository
import proton.android.pass.data.fakes.repositories.FakePendingAttachmentUpdaterRepository
import proton.android.pass.data.fakes.repositories.FakeSentinelRepository
import proton.android.pass.data.fakes.repositories.FakeBulkInviteRepository
import proton.android.pass.data.fakes.repositories.FakeBulkMoveToVaultRepository
import proton.android.pass.data.fakes.repositories.FakeDraftRepository
import proton.android.pass.data.fakes.repositories.FakeItemRepository
import proton.android.pass.data.fakes.repositories.FakeUserAccessDataRepository
import proton.android.pass.data.fakes.repositories.FakeUserInviteRepository
import proton.android.pass.data.fakes.usecases.FakeCanOrganiseVaults
import proton.android.pass.data.fakes.usecases.FakeChangeAliasStatus
import proton.android.pass.data.fakes.usecases.FakeGetItemById
import proton.android.pass.data.fakes.usecases.FakeInitialWorkerLauncher
import proton.android.pass.data.fakes.usecases.FakeObserveAddressesByUserId
import proton.android.pass.data.fakes.usecases.FakeObserveEncryptedItems
import proton.android.pass.data.fakes.usecases.FakeObserveGroupMembersByGroup
import proton.android.pass.data.fakes.usecases.FakeObserveInviteRecommendations
import proton.android.pass.data.fakes.usecases.FakePinItem
import proton.android.pass.data.fakes.usecases.FakePromoteNewInviteToInvite
import proton.android.pass.data.fakes.usecases.FakeRefreshGroupInvites
import proton.android.pass.data.fakes.usecases.FakeRefreshSharesAndEnqueueSync
import proton.android.pass.data.fakes.usecases.FakeRefreshUserInvites
import proton.android.pass.data.fakes.usecases.FakeUnpinItem
import proton.android.pass.data.fakes.usecases.FakeUpdateAliasName
import proton.android.pass.data.fakes.usecases.FakeAcceptInvite
import proton.android.pass.data.fakes.usecases.FakeAddSearchEntry
import proton.android.pass.data.fakes.usecases.FakeApplyPendingEvents
import proton.android.pass.data.fakes.usecases.FakeCanCreateItemInVault
import proton.android.pass.data.fakes.usecases.FakeCanCreateVault
import proton.android.pass.data.fakes.usecases.FakeCanDisplayTotp
import proton.android.pass.data.fakes.usecases.FakeCanManageVaultAccess
import proton.android.pass.data.fakes.usecases.FakeCanMigrateVault
import proton.android.pass.data.fakes.usecases.FakeCanPerformPaidAction
import proton.android.pass.data.fakes.usecases.FakeCanShareShare
import proton.android.pass.data.fakes.usecases.FakeCancelShareInvite
import proton.android.pass.data.fakes.usecases.FakeCheckAddressesCanBeInvited
import proton.android.pass.data.fakes.usecases.FakeCheckMasterPassword
import proton.android.pass.data.fakes.usecases.FakeCheckPin
import proton.android.pass.data.fakes.usecases.FakeClearPin
import proton.android.pass.data.fakes.usecases.FakeClearTrash
import proton.android.pass.data.fakes.usecases.FakeClearUserData
import proton.android.pass.data.fakes.usecases.FakeConfirmNewUserInvite
import proton.android.pass.data.fakes.usecases.FakeCreateAlias
import proton.android.pass.data.fakes.usecases.FakeCreateItem
import proton.android.pass.data.fakes.usecases.FakeCreateLoginAndAlias
import proton.android.pass.data.fakes.usecases.FakeCreatePin
import proton.android.pass.data.fakes.usecases.FakeCreateVault
import proton.android.pass.data.fakes.usecases.FakeDeleteAllSearchEntry
import proton.android.pass.data.fakes.usecases.FakeDeleteItems
import proton.android.pass.data.fakes.usecases.FakeDeleteSearchEntry
import proton.android.pass.data.fakes.usecases.FakeDeleteVault
import proton.android.pass.data.fakes.usecases.FakeGetAllKeysByAddress
import proton.android.pass.data.fakes.usecases.FakeGetDefaultBrowser
import proton.android.pass.data.fakes.usecases.FakeGetInviteUserMode
import proton.android.pass.data.fakes.usecases.FakeGetItemActions
import proton.android.pass.data.fakes.usecases.FakeGetItemByAliasEmail
import proton.android.pass.data.fakes.usecases.FakeGetPasskeyById
import proton.android.pass.data.fakes.usecases.FakeGetShareById
import proton.android.pass.data.fakes.usecases.FakeGetSuggestedAutofillItems
import proton.android.pass.data.fakes.usecases.FakeGetUserPlan
import proton.android.pass.data.fakes.usecases.FakeGetVaultByShareId
import proton.android.pass.data.fakes.usecases.FakeGetVaultMembers
import proton.android.pass.data.fakes.usecases.FakeGetVaultWithItemCountById
import proton.android.pass.data.fakes.usecases.FakeInviteToVault
import proton.android.pass.data.fakes.usecases.FakeItemSyncStatusRepository
import proton.android.pass.data.fakes.usecases.FakeLeaveShare
import proton.android.pass.data.fakes.usecases.FakeMigrateItems
import proton.android.pass.data.fakes.usecases.FakeMigrateVault
import proton.android.pass.data.fakes.usecases.FakeObserveAliasDetails
import proton.android.pass.data.fakes.usecases.FakeObserveAliasOptions
import proton.android.pass.data.fakes.usecases.FakeObserveAllShares
import proton.android.pass.data.fakes.usecases.FakeObserveAnyAccountHasEnforcedLock
import proton.android.pass.data.fakes.usecases.FakeObserveAppNeedsUpdate
import proton.android.pass.data.fakes.usecases.FakeObserveConfirmedInviteToken
import proton.android.pass.data.fakes.usecases.FakeObserveCurrentUser
import proton.android.pass.data.fakes.usecases.FakeObserveCurrentUserSettings
import proton.android.pass.data.fakes.usecases.FakeObserveDefaultVault
import proton.android.pass.data.fakes.usecases.FakeObserveInvites
import proton.android.pass.data.fakes.usecases.FakeObserveItemById
import proton.android.pass.data.fakes.usecases.FakeObserveItemCount
import proton.android.pass.data.fakes.usecases.FakeObserveItems
import proton.android.pass.data.fakes.usecases.FakeObserveItemsWithPasskeys
import proton.android.pass.data.fakes.usecases.FakeObserveMFACount
import proton.android.pass.data.fakes.usecases.FakeObserveOrganizationSettings
import proton.android.pass.data.fakes.usecases.FakeObservePinnedItems
import proton.android.pass.data.fakes.usecases.FakeObserveSearchEntry
import proton.android.pass.data.fakes.usecases.FakeObserveUpgradeInfo
import proton.android.pass.data.fakes.usecases.FakeObserveUserAccessData
import proton.android.pass.data.fakes.usecases.FakeObserveUserEmail
import proton.android.pass.data.fakes.usecases.FakeObserveVaultWithItemCountById
import proton.android.pass.data.fakes.usecases.FakeObserveVaults
import proton.android.pass.data.fakes.usecases.FakeObserveVaultsWithItemCount
import proton.android.pass.data.fakes.usecases.FakePerformSync
import proton.android.pass.data.fakes.usecases.FakePinItems
import proton.android.pass.data.fakes.usecases.FakeRefreshBreaches
import proton.android.pass.data.fakes.usecases.FakeRefreshContent
import proton.android.pass.data.fakes.usecases.FakeRefreshOrganizationSettings
import proton.android.pass.data.fakes.usecases.FakeRefreshUserAccess
import proton.android.pass.data.fakes.usecases.FakeRejectInvite
import proton.android.pass.data.fakes.usecases.FakeRemoveShareMember
import proton.android.pass.data.fakes.usecases.FakeResendShareInvite
import proton.android.pass.data.fakes.usecases.FakeResetAppToDefaults
import proton.android.pass.data.fakes.usecases.FakeRestoreAllItems
import proton.android.pass.data.fakes.usecases.FakeRestoreItems
import proton.android.pass.data.fakes.usecases.FakeSetDefaultVault
import proton.android.pass.data.fakes.usecases.FakeTransferVaultOwnership
import proton.android.pass.data.fakes.usecases.FakeTrashItems
import proton.android.pass.data.fakes.usecases.FakeUnpinItems
import proton.android.pass.data.fakes.usecases.FakeUpdateAlias
import proton.android.pass.data.fakes.usecases.FakeUpdateAutofillItem
import proton.android.pass.data.fakes.usecases.FakeUpdateItem
import proton.android.pass.data.fakes.usecases.FakeUpdateShareMemberRole
import proton.android.pass.data.fakes.usecases.FakeUpdateVault
import proton.android.pass.data.fakes.usecases.accesskey.FakeAuthWithExtraPassword
import proton.android.pass.data.fakes.usecases.accesskey.FakeCheckLocalExtraPassword
import proton.android.pass.data.fakes.usecases.accesskey.FakeHasExtraPassword
import proton.android.pass.data.fakes.usecases.accesskey.FakeRemoveExtraPassword
import proton.android.pass.data.fakes.usecases.accesskey.FakeSetupExtraPassword
import proton.android.pass.data.fakes.usecases.aliascontact.FakeObserveAliasContacts
import proton.android.pass.data.fakes.usecases.attachments.FakeClearAttachments
import proton.android.pass.data.fakes.usecases.attachments.FakeDownloadAttachment
import proton.android.pass.data.fakes.usecases.attachments.FakeLinkAttachmentsToItem
import proton.android.pass.data.fakes.usecases.attachments.FakeObserveAllItemRevisionAttachments
import proton.android.pass.data.fakes.usecases.attachments.FakeObserveDetailItemAttachments
import proton.android.pass.data.fakes.usecases.attachments.FakeObserveUpdateItemAttachments
import proton.android.pass.data.fakes.usecases.attachments.FakeRenameAttachments
import proton.android.pass.data.fakes.usecases.attachments.FakeUploadAttachment
import proton.android.pass.data.fakes.usecases.breach.FakeAddBreachCustomEmail
import proton.android.pass.data.fakes.usecases.breach.FakeMarkEmailBreachAsResolved
import proton.android.pass.data.fakes.usecases.breach.FakeObserveAllBreachByUserId
import proton.android.pass.data.fakes.usecases.breach.FakeObserveBreachAliasEmails
import proton.android.pass.data.fakes.usecases.breach.FakeObserveHasBreaches
import proton.android.pass.data.fakes.usecases.breach.FakeObserveBreachCustomEmails
import proton.android.pass.data.fakes.usecases.breach.FakeObserveBreachEmailReport
import proton.android.pass.data.fakes.usecases.breach.FakeObserveBreachProtonEmails
import proton.android.pass.data.fakes.usecases.breach.FakeObserveBreachesForAliasEmail
import proton.android.pass.data.fakes.usecases.breach.FakeObserveBreachesForCustomEmail
import proton.android.pass.data.fakes.usecases.breach.FakeObserveBreachesForEmail
import proton.android.pass.data.fakes.usecases.breach.FakeObserveBreachesForProtonEmail
import proton.android.pass.data.fakes.usecases.breach.FakeObserveCustomEmailSuggestions
import proton.android.pass.data.fakes.usecases.breach.FakeObserveGlobalMonitorState
import proton.android.pass.data.fakes.usecases.breach.FakeRemoveCustomEmail
import proton.android.pass.data.fakes.usecases.breach.FakeResendVerificationCode
import proton.android.pass.data.fakes.usecases.breach.FakeUpdateGlobalAliasAddressesMonitorState
import proton.android.pass.data.fakes.usecases.breach.FakeUpdateGlobalProtonAddressesMonitorState
import proton.android.pass.data.fakes.usecases.breach.FakeUpdateProtonAddressMonitorState
import proton.android.pass.data.fakes.usecases.breach.FakeVerifyBreachCustomEmail
import proton.android.pass.data.fakes.usecases.credentials.passkeys.FakeGetPasskeyCredentialItems
import proton.android.pass.data.fakes.usecases.credentials.passwords.FakeGetPasswordCredentialItems
import proton.android.pass.data.fakes.usecases.inappmessages.FakeObserveDeliverableMinimizedPromoInAppMessage
import proton.android.pass.data.fakes.usecases.inappmessages.FakeObserveDeliverableModalInAppMessages
import proton.android.pass.data.fakes.usecases.inappmessages.FakeObserveDeliverablePromoInAppMessages
import proton.android.pass.data.fakes.usecases.invites.FakeInviteToItem
import proton.android.pass.data.fakes.usecases.invites.FakeObserveInvite
import proton.android.pass.data.fakes.usecases.items.FakeGetItemCategory
import proton.android.pass.data.fakes.usecases.items.FakeGetItemOptions
import proton.android.pass.data.fakes.usecases.items.FakeGetMigrationItemsSelection
import proton.android.pass.data.fakes.usecases.items.FakeObserveCanCreateItems
import proton.android.pass.data.fakes.usecases.items.FakeObserveItemRevisions
import proton.android.pass.data.fakes.usecases.items.FakeObserveMonitoredItems
import proton.android.pass.data.fakes.usecases.items.FakeObserveSharedItemCountSummary
import proton.android.pass.data.fakes.usecases.items.FakeOpenItemRevision
import proton.android.pass.data.fakes.usecases.items.FakeUpdateItemFlag
import proton.android.pass.data.fakes.usecases.organizations.FakeObserveOrganizationSharingPolicy
import proton.android.pass.data.fakes.usecases.passwordHistoryEntry.FakeAddOnePasswordHistoryEntryToUser
import proton.android.pass.data.fakes.usecases.passwordHistoryEntry.FakeDeleteOldPasswordHistoryEntry
import proton.android.pass.data.fakes.usecases.passwordHistoryEntry.FakeDeleteOnePasswordHistoryEntryForUser
import proton.android.pass.data.fakes.usecases.passwordHistoryEntry.FakeDeletePasswordHistoryEntryForUser
import proton.android.pass.data.fakes.usecases.passwordHistoryEntry.FakeGetPasswordHistoryEntryForUser
import proton.android.pass.data.fakes.usecases.passwordHistoryEntry.FakeObservePasswordHistoryEntryForUser
import proton.android.pass.data.fakes.usecases.passwords.FakeObservePasswordConfig
import proton.android.pass.data.fakes.usecases.passwords.FakeUpdatePasswordConfig
import proton.android.pass.data.fakes.usecases.plan.FakeObservePlanWithPrices
import proton.android.pass.data.fakes.usecases.securelink.FakeDeleteInactiveSecureLinks
import proton.android.pass.data.fakes.usecases.securelink.FakeDeleteSecureLink
import proton.android.pass.data.fakes.usecases.securelink.FakeGenerateSecureLink
import proton.android.pass.data.fakes.usecases.securelink.FakeObserveActiveSecureLinks
import proton.android.pass.data.fakes.usecases.securelink.FakeObserveHasAssociatedSecureLinks
import proton.android.pass.data.fakes.usecases.securelink.FakeObserveInactiveSecureLinks
import proton.android.pass.data.fakes.usecases.securelink.FakeObserveSecureLink
import proton.android.pass.data.fakes.usecases.securelink.FakeObserveSecureLinks
import proton.android.pass.data.fakes.usecases.securelink.FakeObserveSecureLinksCount
import proton.android.pass.data.fakes.usecases.shares.FakeObserveAutofillShares
import proton.android.pass.data.fakes.usecases.shares.FakeObserveEncryptedSharedItems
import proton.android.pass.data.fakes.usecases.shares.FakeObserveHasShares
import proton.android.pass.data.fakes.usecases.shares.FakeObserveShare
import proton.android.pass.data.fakes.usecases.shares.FakeObserveShareItemMembers
import proton.android.pass.data.fakes.usecases.shares.FakeObserveShareItemsCount
import proton.android.pass.data.fakes.usecases.shares.FakeObserveSharePendingInvites
import proton.android.pass.data.fakes.usecases.shares.FakeObserveSharesByType
import proton.android.pass.data.fakes.usecases.shares.FakeObserveSharesItemsCount
import proton.android.pass.data.fakes.usecases.simplelogin.FakeCreateSimpleLoginAliasMailbox
import proton.android.pass.data.fakes.usecases.simplelogin.FakeDeleteSimpleLoginAliasMailbox
import proton.android.pass.data.fakes.usecases.simplelogin.FakeDisableSimpleLoginSyncPreference
import proton.android.pass.data.fakes.usecases.simplelogin.FakeEnableSimpleLoginSyncImpl
import proton.android.pass.data.fakes.usecases.simplelogin.FakeObserveSimpleLoginAliasDomains
import proton.android.pass.data.fakes.usecases.simplelogin.FakeObserveSimpleLoginAliasMailbox
import proton.android.pass.data.fakes.usecases.simplelogin.FakeObserveSimpleLoginAliasMailboxes
import proton.android.pass.data.fakes.usecases.simplelogin.FakeObserveSimpleLoginAliasSettings
import proton.android.pass.data.fakes.usecases.simplelogin.FakeObserveSimpleLoginSyncStatus
import proton.android.pass.data.fakes.usecases.simplelogin.FakeResendSimpleLoginAliasMailboxVerificationCode
import proton.android.pass.data.fakes.usecases.simplelogin.FakeSyncSimpleLoginPendingAliases
import proton.android.pass.data.fakes.usecases.simplelogin.FakeUpdateSimpleLoginAliasDomain
import proton.android.pass.data.fakes.usecases.simplelogin.FakeUpdateSimpleLoginAliasMailbox
import proton.android.pass.data.fakes.usecases.simplelogin.FakeVerifySimpleLoginAliasMailbox
import proton.android.pass.data.fakes.usecases.tooltips.FakeDisableTooltip
import proton.android.pass.data.fakes.usecases.tooltips.FakeObserveTooltipEnabled
import proton.android.pass.data.fakes.usecases.vaults.FakeBatchChangeShareVisibility
import proton.android.pass.data.fakes.usecases.vaults.FakeObserveVaultsGroupedByShareId
import proton.android.pass.data.fakes.usecases.vaults.FakeObserveVaultsGroupedByVisibility
import proton.android.pass.data.fakes.work.FakeWorkManagerFacade
import proton.android.pass.data.fakes.work.FakeWorkerLauncher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
@Suppress("TooManyFunctions", "LargeClass")
abstract class FakesDataModule {

    @Binds
    abstract fun bindGetSuggestedAutofillItems(impl: FakeGetSuggestedAutofillItems): GetSuggestedAutofillItems

    @Binds
    abstract fun bindItemRepository(impl: FakeItemRepository): ItemRepository

    @Binds
    abstract fun bindAliasRepository(impl: FakeAliasRepository): AliasRepository

    @Binds
    abstract fun bindDraftRepository(impl: FakeDraftRepository): DraftRepository

    @Binds
    abstract fun bindCreateAlias(impl: FakeCreateAlias): CreateAlias

    @Binds
    abstract fun bindCreateItem(impl: FakeCreateItem): CreateItem

    @Binds
    abstract fun bindObserveVaults(impl: FakeObserveVaults): ObserveVaults

    @Binds
    abstract fun bindObserveCurrentUser(impl: FakeObserveCurrentUser): ObserveCurrentUser

    @Binds
    abstract fun bindObserveCurrentUserSettings(impl: FakeObserveCurrentUserSettings): ObserveCurrentUserSettings

    @Binds
    abstract fun bindObserveAliasOptions(impl: FakeObserveAliasOptions): ObserveAliasOptions

    @Binds
    abstract fun bindUpdateAlias(impl: FakeUpdateAlias): UpdateAlias

    @Binds
    abstract fun bindUpdateAutofillItem(impl: FakeUpdateAutofillItem): UpdateAutofillItem

    @Binds
    abstract fun bindUpdateItem(impl: FakeUpdateItem): UpdateItem

    @Binds
    abstract fun bindGetShareById(impl: FakeGetShareById): GetShareById

    @Binds
    abstract fun bindTrashItem(impl: FakeTrashItems): TrashItems

    @Binds
    abstract fun bindUpdateVault(impl: FakeUpdateVault): UpdateVault

    @Binds
    abstract fun bindGetVaultByShareId(impl: FakeGetVaultByShareId): GetVaultByShareId

    @Binds
    abstract fun bindObserveVaultsWithItemCount(impl: FakeObserveVaultsWithItemCount): ObserveVaultsWithItemCount

    @Binds
    abstract fun bindObserveItemCount(impl: FakeObserveItemCount): ObserveItemCount

    @Binds
    abstract fun bindMigrateItem(impl: FakeMigrateItems): MigrateItems

    @Binds
    abstract fun bindGetVaultWithItemCountById(impl: FakeGetVaultWithItemCountById): GetVaultWithItemCountById

    @Binds
    abstract fun bindCreateItemAndAlias(impl: FakeCreateLoginAndAlias): CreateLoginAndAlias

    @Binds
    abstract fun bindDeleteVault(impl: FakeDeleteVault): DeleteVault

    @Binds
    abstract fun bindGetUserPlan(impl: FakeGetUserPlan): GetUserPlan


    @Binds
    abstract fun bindGetItemById(impl: FakeGetItemById): GetItemById

    @Binds
    abstract fun bindObserveItemById(impl: FakeObserveItemById): ObserveItemById

    @Binds
    abstract fun bindDeleteItem(impl: FakeDeleteItems): DeleteItems

    @Binds
    abstract fun bindRestoreItem(impl: FakeRestoreItems): RestoreItems

    @Binds
    abstract fun bindRefreshContent(impl: FakeRefreshContent): RefreshContent

    @Binds
    abstract fun bindApplyPendingEvents(impl: FakeApplyPendingEvents): ApplyPendingEvents

    @Binds
    abstract fun bindRestoreItems(impl: FakeRestoreAllItems): RestoreAllItems

    @Binds
    abstract fun bindClearTrash(impl: FakeClearTrash): ClearTrash

    @Binds
    abstract fun bindAddSearchEntry(impl: FakeAddSearchEntry): AddSearchEntry

    @Binds
    abstract fun bindDeleteSearchEntry(impl: FakeDeleteSearchEntry): DeleteSearchEntry

    @Binds
    abstract fun bindDeleteAllSearchEntry(impl: FakeDeleteAllSearchEntry): DeleteAllSearchEntry

    @Binds
    abstract fun bindObserveSearchEntry(impl: FakeObserveSearchEntry): ObserveSearchEntry

    @Binds
    abstract fun bindObserveItems(impl: FakeObserveItems): ObserveItems

    @Binds
    abstract fun bindObserveEncryptedItems(impl: FakeObserveEncryptedItems): ObserveEncryptedItems

    @Binds
    abstract fun bindObservePinnedItems(impl: FakeObservePinnedItems): ObservePinnedItems

    @Binds
    abstract fun bindItemSyncStatusRepository(impl: FakeItemSyncStatusRepository): ItemSyncStatusRepository

    @Binds
    abstract fun bindClearUserData(impl: FakeClearUserData): ClearUserData

    @Binds
    abstract fun bindGetUpgradeInfo(impl: FakeObserveUpgradeInfo): ObserveUpgradeInfo

    @Binds
    abstract fun bindMigrateVault(impl: FakeMigrateVault): MigrateVault

    @Binds
    abstract fun bindObserveMFACount(impl: FakeObserveMFACount): ObserveMFACount

    @Binds
    abstract fun bindCreateVault(impl: FakeCreateVault): CreateVault

    @Binds
    abstract fun bindCanPerformPaidAction(impl: FakeCanPerformPaidAction): CanPerformPaidAction

    @Binds
    abstract fun bindRefreshPlan(impl: FakeRefreshUserAccess): RefreshUserAccess

    @Binds
    abstract fun bindObserveAliasDetails(impl: FakeObserveAliasDetails): ObserveAliasDetails

    @Binds
    abstract fun bindGetItemByAliasEmail(impl: FakeGetItemByAliasEmail): GetItemByAliasEmail

    @Binds
    abstract fun bindCanDisplayTotp(impl: FakeCanDisplayTotp): CanDisplayTotp

    @Binds
    abstract fun bindCheckMasterPassword(impl: FakeCheckMasterPassword): CheckMasterPassword

    @Binds
    abstract fun bindObserveUserEmail(impl: FakeObserveUserEmail): ObserveUserEmail

    @Binds
    abstract fun bindCheckPin(impl: FakeCheckPin): CheckPin

    @Binds
    abstract fun bindCreatePin(impl: FakeCreatePin): CreatePin

    @Binds
    abstract fun bindClearAppData(impl: FakeResetAppToDefaults): ResetAppToDefaults

    @Binds
    abstract fun bindClearPin(impl: FakeClearPin): ClearPin

    @Binds
    abstract fun bindInviteToVault(impl: FakeInviteToVault): InviteToVault

    @Binds
    abstract fun bindUserInviteRepository(impl: FakeUserInviteRepository): UserInviteRepository

    @Binds
    abstract fun bindObserveInvites(impl: FakeObserveInvites): ObserveInvites

    @Binds
    abstract fun bindObserveInviteRecommendations(impl: FakeObserveInviteRecommendations): ObserveInviteRecommendations

    @Binds
    abstract fun bindRefreshUserInvites(impl: FakeRefreshUserInvites): RefreshUserInvites

    @Binds
    abstract fun bindRefreshGroupInvites(impl: FakeRefreshGroupInvites): RefreshGroupInvites

    @Binds
    abstract fun bindRefreshBreaches(impl: FakeRefreshBreaches): RefreshBreaches

    @Binds
    abstract fun bindRefreshOrganizationSettings(impl: FakeRefreshOrganizationSettings): RefreshOrganizationSettings

    @Binds
    abstract fun bindRefreshSharesAndEnqueueSync(impl: FakeRefreshSharesAndEnqueueSync): RefreshSharesAndEnqueueSync

    @Binds
    abstract fun bindPromoteNewInviteToInvite(impl: FakePromoteNewInviteToInvite): PromoteNewInviteToInvite

    @Binds
    abstract fun bindPerformSync(impl: FakePerformSync): PerformSync

    @Binds
    abstract fun bindAcceptInvite(impl: FakeAcceptInvite): AcceptInvite

    @Binds
    abstract fun bindRejectInvite(impl: FakeRejectInvite): RejectInvite

    @Binds
    abstract fun bindLeaveVault(impl: FakeLeaveShare): LeaveShare

    @Binds
    abstract fun bindCanShareShare(impl: FakeCanShareShare): CanShareShare

    @Binds
    abstract fun bindCanMigrateVault(impl: FakeCanMigrateVault): CanMigrateVault

    @Binds
    abstract fun bindCanManageVaultAccess(impl: FakeCanManageVaultAccess): CanManageVaultAccess

    @Binds
    abstract fun bindGetVaultMembers(impl: FakeGetVaultMembers): GetVaultMembers

    @Binds
    abstract fun bindRemoveMemberFromVault(impl: FakeRemoveShareMember): RemoveShareMember

    @Binds
    abstract fun bindSetVaultMemberPermission(impl: FakeUpdateShareMemberRole): UpdateShareMemberRole

    @Binds
    abstract fun bindResendInvite(impl: FakeResendShareInvite): ResendShareInvite

    @Binds
    abstract fun bindCancelInvite(impl: FakeCancelShareInvite): CancelShareInvite

    @Binds
    abstract fun bindTransferOwnership(impl: FakeTransferVaultOwnership): TransferVaultOwnership

    @Binds
    abstract fun bindCanCreateVault(impl: FakeCanCreateVault): CanCreateVault

    @Binds
    abstract fun bindCanOrganiseVaults(impl: FakeCanOrganiseVaults): CanOrganiseVaults

    @Binds
    abstract fun bindCanCreateItemInVault(impl: FakeCanCreateItemInVault): CanCreateItemInVault

    @Binds
    abstract fun bindGetInviteUserMode(impl: FakeGetInviteUserMode): GetInviteUserMode

    @Binds
    abstract fun bindConfirmNewUserInvite(impl: FakeConfirmNewUserInvite): ConfirmNewUserInvite

    @Binds
    abstract fun bindUserAccessDataRepository(impl: FakeUserAccessDataRepository): UserAccessDataRepository

    @Binds
    abstract fun bindObserveUserAccessData(impl: FakeObserveUserAccessData): ObserveUserAccessData

    @Binds
    abstract fun bindGetAllKeysByAddress(impl: FakeGetAllKeysByAddress): GetAllKeysByAddress

    @Binds
    abstract fun bindObserveHasConfirmedInvite(impl: FakeObserveConfirmedInviteToken): ObserveConfirmedInviteToken

    @Binds
    abstract fun bindGetItemActions(impl: FakeGetItemActions): GetItemActions

    @Binds
    abstract fun bindGetDefaultBrowser(impl: FakeGetDefaultBrowser): GetDefaultBrowser

    @Binds
    abstract fun bindObserveDefaultVault(impl: FakeObserveDefaultVault): ObserveDefaultVault

    @Binds
    abstract fun bindSetDefaultVault(impl: FakeSetDefaultVault): SetDefaultVault

    @Binds
    abstract fun bindBulkMoveToVaultRepository(impl: FakeBulkMoveToVaultRepository): BulkMoveToVaultRepository

    @Binds
    abstract fun bindPinItem(impl: FakePinItem): PinItem

    @Binds
    abstract fun bindUnpinItem(impl: FakeUnpinItem): UnpinItem

    @Binds
    abstract fun bindPinItems(impl: FakePinItems): PinItems

    @Binds
    abstract fun bindUnpinItems(impl: FakeUnpinItems): UnpinItems

    @Binds
    abstract fun bindBulkInviteRepository(impl: FakeBulkInviteRepository): BulkInviteRepository

    @Binds
    abstract fun bindObserveAppNeedsUpdate(impl: FakeObserveAppNeedsUpdate): ObserveAppNeedsUpdate

    @Binds
    abstract fun bindObserveVaultWithItemCountById(
        impl: FakeObserveVaultWithItemCountById
    ): ObserveVaultWithItemCountById

    @Binds
    abstract fun bindObserveItemRevisions(impl: FakeObserveItemRevisions): ObserveItemRevisions

    @Binds
    abstract fun bindGetItemCategory(impl: FakeGetItemCategory): GetItemCategory

    @Binds
    abstract fun bindObserveOrganizationSettings(impl: FakeObserveOrganizationSettings): ObserveOrganizationSettings

    @Binds
    abstract fun bindObserveAnyAccountHasEnforcedLock(
        impl: FakeObserveAnyAccountHasEnforcedLock
    ): ObserveAnyAccountHasEnforcedLock

    @Binds
    abstract fun bindCheckCanAddressesBeInvited(impl: FakeCheckAddressesCanBeInvited): CheckCanAddressesBeInvited

    @Binds
    abstract fun bindObserveItemsWithPasskeys(impl: FakeObserveItemsWithPasskeys): ObserveItemsWithPasskeys

    @Binds
    abstract fun bindGetPasskeyById(impl: FakeGetPasskeyById): GetPasskeyById

    @Binds
    abstract fun bindOpenItemRevision(impl: FakeOpenItemRevision): OpenItemRevision

    @Binds
    abstract fun bindSentinelRepository(impl: FakeSentinelRepository): SentinelRepository

    @[Binds Singleton]
    abstract fun bindObserveBreach(impl: FakeObserveAllBreachByUserId): ObserveAllBreachByUserId

    @[Binds Singleton]
    abstract fun bindObserveHasBreaches(impl: FakeObserveHasBreaches): ObserveHasBreaches

    @Binds
    abstract fun bindObserveBreachCustomEmails(impl: FakeObserveBreachCustomEmails): ObserveBreachCustomEmails

    @Binds
    abstract fun bindAddBreachCustomEmail(impl: FakeAddBreachCustomEmail): AddBreachCustomEmail

    @Binds
    abstract fun bindVerifyBreachCustomEmail(impl: FakeVerifyBreachCustomEmail): VerifyBreachCustomEmail

    @Binds
    abstract fun bindObserveBreachesForProtonEmail(
        impl: FakeObserveBreachesForProtonEmail
    ): ObserveBreachesForProtonEmail

    @Binds
    abstract fun bindObserveBreachesForCustomEmail(
        impl: FakeObserveBreachesForCustomEmail
    ): ObserveBreachesForCustomEmail

    @Binds
    abstract fun bindObserveBreachesForAlias(impl: FakeObserveBreachesForAliasEmail): ObserveBreachesForAliasEmail

    @Binds
    abstract fun bindObserveCustomEmailSuggestions(
        impl: FakeObserveCustomEmailSuggestions
    ): ObserveCustomEmailSuggestions

    @Binds
    abstract fun bindObserveAddressesByUserId(impl: FakeObserveAddressesByUserId): ObserveAddressesByUserId

    @Binds
    abstract fun bindMarkEmailBreachAsResolved(impl: FakeMarkEmailBreachAsResolved): MarkEmailBreachAsResolved

    @Binds
    abstract fun bindUpdateItemFlag(impl: FakeUpdateItemFlag): UpdateItemFlag

    @[Binds Singleton]
    abstract fun bindObserveMonitoredItems(impl: FakeObserveMonitoredItems): ObserveMonitoredItems

    @Binds
    abstract fun bindResendVerificationCode(impl: FakeResendVerificationCode): ResendVerificationCode

    @Binds
    abstract fun bindRemoveCustomEmail(impl: FakeRemoveCustomEmail): RemoveCustomEmail

    @Binds
    abstract fun bindUpdateAliasAddressesMonitorState(
        impl: FakeUpdateGlobalAliasAddressesMonitorState
    ): UpdateGlobalAliasAddressesMonitorState

    @Binds
    abstract fun bindUpdateProtonAddressesMonitorState(
        impl: FakeUpdateGlobalProtonAddressesMonitorState
    ): UpdateGlobalProtonAddressesMonitorState

    @Binds
    abstract fun bindUpdateProtonAddressMonitorState(
        impl: FakeUpdateProtonAddressMonitorState
    ): UpdateProtonAddressMonitorState

    @Binds
    abstract fun bindObserveGlobalMonitorState(impl: FakeObserveGlobalMonitorState): ObserveGlobalMonitorState

    @Binds
    abstract fun bindObserveBreachesForEmail(impl: FakeObserveBreachesForEmail): ObserveBreachesForEmail

    @Binds
    abstract fun bindObserveBreachEmailReport(impl: FakeObserveBreachEmailReport): ObserveBreachEmailReport

    @Binds
    abstract fun bindObserveBreachProtonEmails(impl: FakeObserveBreachProtonEmails): ObserveBreachProtonEmails

    @Binds
    abstract fun bindObserveVaultsGroupedByShareId(
        impl: FakeObserveVaultsGroupedByShareId
    ): ObserveVaultsGroupedByShareId

    @Binds
    abstract fun bindObserveVaultsGroupedByVisibility(
        impl: FakeObserveVaultsGroupedByVisibility
    ): ObserveVaultsGroupedByVisibility

    @Binds
    abstract fun bindGenerateSecureLink(impl: FakeGenerateSecureLink): GenerateSecureLink

    @Binds
    abstract fun bindObserveBreachAliasEmails(impl: FakeObserveBreachAliasEmails): ObserveBreachAliasEmails

    @Binds
    abstract fun bindObserveTooltipEnabled(impl: FakeObserveTooltipEnabled): ObserveTooltipEnabled

    @Binds
    abstract fun bindDisableTooltip(impl: FakeDisableTooltip): DisableTooltip

    @Binds
    abstract fun bindAuthWithAccessKey(impl: FakeAuthWithExtraPassword): AuthWithExtraPassword

    @Binds
    abstract fun bindCheckLocalAccessKey(impl: FakeCheckLocalExtraPassword): CheckLocalExtraPassword

    @Binds
    abstract fun bindRemoveAccessKey(impl: FakeRemoveExtraPassword): RemoveExtraPassword

    @Binds
    abstract fun bindSetupAccessKey(impl: FakeSetupExtraPassword): SetupExtraPassword

    @Binds
    abstract fun bindHasExtraPassword(impl: FakeHasExtraPassword): HasExtraPassword

    @Binds
    abstract fun bindObserveSecureLink(impl: FakeObserveSecureLink): ObserveSecureLink

    @Binds
    abstract fun bindObserveSecureLinks(impl: FakeObserveSecureLinks): ObserveSecureLinks

    @Binds
    abstract fun bindObserveActiveSecureLinks(impl: FakeObserveActiveSecureLinks): ObserveActiveSecureLinks

    @Binds
    abstract fun bindObserveInactiveSecureLinks(impl: FakeObserveInactiveSecureLinks): ObserveInactiveSecureLinks

    @Binds
    abstract fun bindDeleteSecureLink(impl: FakeDeleteSecureLink): DeleteSecureLink

    @Binds
    abstract fun bindDeleteInactiveSecureLinks(impl: FakeDeleteInactiveSecureLinks): DeleteInactiveSecureLinks

    @Binds
    abstract fun bindObserveSecureLinksCount(impl: FakeObserveSecureLinksCount): ObserveSecureLinksCount

    @Binds
    abstract fun bindObserveHasAssociatedSecureLinks(
        impl: FakeObserveHasAssociatedSecureLinks
    ): ObserveHasAssociatedSecureLinks

    @Binds
    abstract fun bindChangeAliasStatus(impl: FakeChangeAliasStatus): ChangeAliasStatus

    @Binds
    abstract fun bindObserveSimpleLoginSyncStatus(impl: FakeObserveSimpleLoginSyncStatus): ObserveSimpleLoginSyncStatus

    @Binds
    abstract fun bindDisableSimpleLoginSyncPreference(
        impl: FakeDisableSimpleLoginSyncPreference
    ): DisableSimpleLoginSyncPreference

    @Binds
    abstract fun bindEnableSimpleLoginSync(impl: FakeEnableSimpleLoginSyncImpl): EnableSimpleLoginSync

    @Binds
    abstract fun bindObserveSimpleLoginAliasDomains(
        impl: FakeObserveSimpleLoginAliasDomains
    ): ObserveSimpleLoginAliasDomains

    @Binds
    abstract fun bindUpdateSimpleLoginAliasDomain(impl: FakeUpdateSimpleLoginAliasDomain): UpdateSimpleLoginAliasDomain

    @Binds
    abstract fun bindObserveSimpleLoginAliasMailboxes(
        impl: FakeObserveSimpleLoginAliasMailboxes
    ): ObserveSimpleLoginAliasMailboxes

    @Binds
    abstract fun bindUpdateSimpleLoginAliasMailbox(
        impl: FakeUpdateSimpleLoginAliasMailbox
    ): UpdateSimpleLoginAliasMailbox

    @Binds
    abstract fun bindObserveSimpleLoginAliasSettings(
        impl: FakeObserveSimpleLoginAliasSettings
    ): ObserveSimpleLoginAliasSettings

    @Binds
    abstract fun bindSyncSimpleLoginPendingAliases(
        impl: FakeSyncSimpleLoginPendingAliases
    ): SyncSimpleLoginPendingAliases

    @Binds
    abstract fun bindWorkerLauncher(impl: FakeWorkerLauncher): WorkerLauncher

    @Binds
    abstract fun bindWorkManagerFacade(impl: FakeWorkManagerFacade): WorkManagerFacade

    @Binds
    abstract fun bindAssetLinkRepository(impl: FakeAssetLinkRepository): AssetLinkRepository

    @Binds
    abstract fun bindGetItemOptions(impl: FakeGetItemOptions): GetItemOptions

    @Binds
    abstract fun bindObserveAliasContacts(impl: FakeObserveAliasContacts): ObserveAliasContacts

    @Binds
    abstract fun bindCreateSimpleLoginAliasMailbox(
        impl: FakeCreateSimpleLoginAliasMailbox
    ): CreateSimpleLoginAliasMailbox

    @Binds
    abstract fun bindVerifySimpleLoginAliasMailbox(
        impl: FakeVerifySimpleLoginAliasMailbox
    ): VerifySimpleLoginAliasMailbox

    @Binds
    abstract fun bindResendSimpleLoginMailboxVerifyCode(
        impl: FakeResendSimpleLoginAliasMailboxVerificationCode
    ): ResendSimpleLoginAliasMailboxVerificationCode

    @Binds
    abstract fun bindObserveSimpleLoginAliasMailbox(
        impl: FakeObserveSimpleLoginAliasMailbox
    ): ObserveSimpleLoginAliasMailbox

    @Binds
    abstract fun bindDeleteSimpleLoginAliasMailbox(
        impl: FakeDeleteSimpleLoginAliasMailbox
    ): DeleteSimpleLoginAliasMailbox

    @Binds
    abstract fun bindObservePasswordConfig(impl: FakeObservePasswordConfig): ObservePasswordConfig

    @Binds
    abstract fun bindUpdatePasswordConfig(impl: FakeUpdatePasswordConfig): UpdatePasswordConfig

    @Binds
    abstract fun bindUpdateAliasName(impl: FakeUpdateAliasName): UpdateAliasName

    @Binds
    abstract fun bindInAppMessagesRepository(impl: FakeInAppMessagesRepository): InAppMessagesRepository

    @Binds
    abstract fun bindInviteToItem(impl: FakeInviteToItem): InviteToItem

    @Binds
    abstract fun bindObserveInvite(impl: FakeObserveInvite): ObserveInvite

    @Binds
    abstract fun bindDraftAttachmentRepository(impl: FakeDraftAttachmentRepository): DraftAttachmentRepository

    @Binds
    abstract fun bindMetadataResolver(impl: FakeMetadataResolver): MetadataResolver

    @Binds
    abstract fun bindObserveAllShares(impl: FakeObserveAllShares): ObserveAllShares

    @Binds
    abstract fun bindObserveShare(impl: FakeObserveShare): ObserveShare

    @Binds
    abstract fun bindObserveSharesByType(impl: FakeObserveSharesByType): ObserveSharesByType

    @Binds
    abstract fun bindUploadAttachment(impl: FakeUploadAttachment): UploadAttachment

    @Binds
    abstract fun bindLinkAttachmentToItem(impl: FakeLinkAttachmentsToItem): LinkAttachmentsToItem

    @Binds
    abstract fun bindRenameAttachments(impl: FakeRenameAttachments): RenameAttachments

    @Binds
    abstract fun bindObserveUpdateItemAttachments(impl: FakeObserveUpdateItemAttachments): ObserveUpdateItemAttachments

    @Binds
    abstract fun bindObserveDetailItemAttachments(impl: FakeObserveDetailItemAttachments): ObserveDetailItemAttachments

    @Binds
    abstract fun bindObserveShareMembers(impl: FakeObserveShareItemMembers): ObserveShareItemMembers

    @Binds
    abstract fun bindObserveSharePendingInvites(impl: FakeObserveSharePendingInvites): ObserveSharePendingInvites

    @Binds
    abstract fun bindClearAttachments(impl: FakeClearAttachments): ClearAttachments

    @Binds
    abstract fun bindGetItemKeys(impl: FakeGetShareAndItemKey): GetShareAndItemKey

    @Binds
    abstract fun bindDownloadAttachment(impl: FakeDownloadAttachment): DownloadAttachment

    @Binds
    abstract fun bindObserveAllItemRevisionAttachments(
        impl: FakeObserveAllItemRevisionAttachments
    ): ObserveAllItemRevisionAttachments

    @Binds
    abstract fun bindObserveShareItemsCount(impl: FakeObserveShareItemsCount): ObserveShareItemsCount

    @Binds
    abstract fun bindObserveSharesItemsCount(impl: FakeObserveSharesItemsCount): ObserveSharesItemsCount

    @Binds
    abstract fun bindObserveEncryptedSharedItems(impl: FakeObserveEncryptedSharedItems): ObserveEncryptedSharedItems

    @Binds
    abstract fun bindPendingAttachmentUpdaterRepository(
        impl: FakePendingAttachmentUpdaterRepository
    ): PendingAttachmentUpdaterRepository

    @Binds
    abstract fun bindPendingAttachmentLinkRepository(
        impl: FakePendingAttachmentLinkRepository
    ): PendingAttachmentLinkRepository

    @Binds
    abstract fun bindGetMigrationItemsSelection(impl: FakeGetMigrationItemsSelection): GetMigrationItemsSelection

    @Binds
    abstract fun bindInitialWorkerLauncher(impl: FakeInitialWorkerLauncher): InitialWorkerLauncher

    @Binds
    abstract fun bindObserveAutofillShares(impl: FakeObserveAutofillShares): ObserveAutofillShares

    @Binds
    abstract fun bindObserveSharedItemCountSummary(
        impl: FakeObserveSharedItemCountSummary
    ): ObserveSharedItemCountSummary

    @Binds
    abstract fun bindObserveCanCreateItems(impl: FakeObserveCanCreateItems): ObserveCanCreateItems

    @Binds
    abstract fun bindObserveHasShares(impl: FakeObserveHasShares): ObserveHasShares

    @Binds
    abstract fun bindObserveOrganizationSharingPolicy(
        impl: FakeObserveOrganizationSharingPolicy
    ): ObserveOrganizationSharingPolicy

    @Binds
    abstract fun bindGetPasswordCredentialItems(impl: FakeGetPasswordCredentialItems): GetPasswordCredentialItems

    @Binds
    abstract fun bindGetPasskeyCredentialItems(impl: FakeGetPasskeyCredentialItems): GetPasskeyCredentialItems

    @Binds
    abstract fun bindObserveMinimizedPromoInAppMessages(
        impl: FakeObserveDeliverableMinimizedPromoInAppMessage
    ): ObserveDeliverableMinimizedPromoInAppMessages

    @Binds
    abstract fun bindObserveModalInAppMessages(
        impl: FakeObserveDeliverableModalInAppMessages
    ): ObserveDeliverableModalInAppMessages

    @Binds
    abstract fun bindObservePromoInAppMessages(
        impl: FakeObserveDeliverablePromoInAppMessages
    ): ObserveDeliverablePromoInAppMessages


    @Binds
    abstract fun bindPasswordHistoryRepository(impl: FakePasswordHistoryEntryRepository): PasswordHistoryEntryRepository

    @Binds
    abstract fun bindAddOnePasswordHistoryEntryToUser(
        impl: FakeAddOnePasswordHistoryEntryToUser
    ): AddOnePasswordHistoryEntryToUser

    @Binds
    abstract fun bindDeletePasswordEntryHistoryForUser(
        impl: FakeDeletePasswordHistoryEntryForUser
    ): DeletePasswordHistoryEntryForUser

    @Binds
    abstract fun bindDeleteOnePasswordHistoryEntryForUser(
        impl: FakeDeleteOnePasswordHistoryEntryForUser
    ): DeleteOnePasswordHistoryEntryForUser

    @Binds
    abstract fun bindGetPasswordHistoryEntryForUser(
        impl: FakeGetPasswordHistoryEntryForUser
    ): GetPasswordHistoryEntryForUser

    @Binds
    abstract fun bindObservePasswordHistoryEntryForUser(
        impl: FakeObservePasswordHistoryEntryForUser
    ): ObservePasswordHistoryEntryForUser

    @Binds
    abstract fun bindDeleteOldPasswordHistoryEntry(
        impl: FakeDeleteOldPasswordHistoryEntry
    ): DeleteOldPasswordHistoryEntry

    @Binds
    abstract fun bindBatchChangeShareVisibility(impl: FakeBatchChangeShareVisibility): BatchChangeShareVisibility

    @Binds
    abstract fun bindObserveGroupMembersByGroup(impl: FakeObserveGroupMembersByGroup): ObserveGroupMembersByGroup

    @Binds
    abstract fun bindGetPlanWithPrices(impl: FakeObservePlanWithPrices): ObservePlansWithPrice
}
