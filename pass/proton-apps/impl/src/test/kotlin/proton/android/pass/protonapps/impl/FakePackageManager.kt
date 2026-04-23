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

@file:Suppress("NotImplementedDeclaration")

package proton.android.pass.protonapps.impl

import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.ChangedPackages
import android.content.pm.FeatureInfo
import android.content.pm.InstallSourceInfo
import android.content.pm.InstrumentationInfo
import android.content.pm.ModuleInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.content.pm.PermissionGroupInfo
import android.content.pm.PermissionInfo
import android.content.pm.ProviderInfo
import android.content.pm.ResolveInfo
import android.content.pm.ServiceInfo
import android.content.pm.SharedLibraryInfo
import android.content.pm.VersionedPackage
import android.content.res.Resources
import android.content.res.XmlResourceParser
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.UserHandle

@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION", "TooManyFunctions")
internal class FakePackageManager(
    private val installedPackages: Set<String> = emptySet(),
    var launchIntentFactory: (String) -> Intent? = { null }
) : PackageManager() {

    @Throws(NameNotFoundException::class)
    override fun getPackageInfo(packageName: String, flags: Int): PackageInfo {
        if (packageName !in installedPackages) {
            throw NameNotFoundException(packageName)
        }
        return PackageInfo().apply { this.packageName = packageName }
    }

    override fun getLaunchIntentForPackage(packageName: String): Intent? = launchIntentFactory(packageName)

    // ---- Everything below is unused noise — required because PackageManager is abstract. ----

    override fun getPackageInfo(versionedPackage: VersionedPackage, flags: Int): PackageInfo =
        throw NotImplementedError()
    override fun currentToCanonicalPackageNames(packageNames: Array<out String>): Array<String> =
        throw NotImplementedError()
    override fun canonicalToCurrentPackageNames(packageNames: Array<out String>): Array<String> =
        throw NotImplementedError()
    override fun getLaunchIntentSenderForPackage(packageName: String): IntentSender = throw NotImplementedError()
    override fun getLeanbackLaunchIntentForPackage(packageName: String): Intent? = throw NotImplementedError()
    override fun getPackageGids(packageName: String): IntArray = throw NotImplementedError()
    override fun getPackageGids(packageName: String, flags: Int): IntArray = throw NotImplementedError()
    override fun getPackageUid(packageName: String, flags: Int): Int = throw NotImplementedError()
    override fun getPermissionInfo(permName: String, flags: Int): PermissionInfo = throw NotImplementedError()
    override fun queryPermissionsByGroup(permissionGroup: String?, flags: Int): MutableList<PermissionInfo> =
        throw NotImplementedError()
    override fun getPermissionGroupInfo(groupName: String, flags: Int): PermissionGroupInfo =
        throw NotImplementedError()
    override fun getAllPermissionGroups(flags: Int): MutableList<PermissionGroupInfo> = throw NotImplementedError()
    override fun getApplicationInfo(packageName: String, flags: Int): ApplicationInfo = throw NotImplementedError()
    override fun getActivityInfo(component: ComponentName, flags: Int): ActivityInfo = throw NotImplementedError()
    override fun getReceiverInfo(component: ComponentName, flags: Int): ActivityInfo = throw NotImplementedError()
    override fun getServiceInfo(component: ComponentName, flags: Int): ServiceInfo = throw NotImplementedError()
    override fun getProviderInfo(component: ComponentName, flags: Int): ProviderInfo = throw NotImplementedError()
    override fun getInstallSourceInfo(packageName: String): InstallSourceInfo = throw NotImplementedError()
    override fun getInstalledPackages(flags: Int): MutableList<PackageInfo> = throw NotImplementedError()
    override fun getPackagesHoldingPermissions(permissions: Array<out String>, flags: Int): MutableList<PackageInfo> =
        throw NotImplementedError()
    override fun checkPermission(permName: String, packageName: String): Int = throw NotImplementedError()
    override fun isPermissionRevokedByPolicy(permName: String, packageName: String): Boolean =
        throw NotImplementedError()
    override fun addPermission(info: PermissionInfo): Boolean = throw NotImplementedError()
    override fun addPermissionAsync(info: PermissionInfo): Boolean = throw NotImplementedError()
    override fun removePermission(permName: String) = throw NotImplementedError()
    override fun checkSignatures(packageName1: String, packageName2: String): Int = throw NotImplementedError()
    override fun checkSignatures(uid1: Int, uid2: Int): Int = throw NotImplementedError()
    override fun getPackagesForUid(uid: Int): Array<String>? = throw NotImplementedError()
    override fun getNameForUid(uid: Int): String? = throw NotImplementedError()
    override fun getInstalledApplications(flags: Int): MutableList<ApplicationInfo> = throw NotImplementedError()
    override fun isInstantApp(): Boolean = throw NotImplementedError()
    override fun isInstantApp(packageName: String): Boolean = throw NotImplementedError()
    override fun getInstantAppCookieMaxBytes(): Int = throw NotImplementedError()
    override fun getInstantAppCookie(): ByteArray = throw NotImplementedError()
    override fun clearInstantAppCookie() = throw NotImplementedError()
    override fun updateInstantAppCookie(cookie: ByteArray?) = throw NotImplementedError()
    override fun getSystemSharedLibraryNames(): Array<String>? = throw NotImplementedError()
    override fun getSharedLibraries(flags: Int): MutableList<SharedLibraryInfo> = throw NotImplementedError()
    override fun getChangedPackages(sequenceNumber: Int): ChangedPackages? = throw NotImplementedError()
    override fun getSystemAvailableFeatures(): Array<FeatureInfo> = throw NotImplementedError()
    override fun hasSystemFeature(featureName: String): Boolean = throw NotImplementedError()
    override fun hasSystemFeature(featureName: String, version: Int): Boolean = throw NotImplementedError()
    override fun resolveActivity(intent: Intent, flags: Int): ResolveInfo? = throw NotImplementedError()
    override fun queryIntentActivities(intent: Intent, flags: Int): MutableList<ResolveInfo> =
        throw NotImplementedError()
    override fun queryIntentActivityOptions(
        caller: ComponentName?,
        specifics: Array<out Intent>?,
        intent: Intent,
        flags: Int
    ): MutableList<ResolveInfo> = throw NotImplementedError()
    override fun queryBroadcastReceivers(intent: Intent, flags: Int): MutableList<ResolveInfo> =
        throw NotImplementedError()
    override fun resolveService(intent: Intent, flags: Int): ResolveInfo? = throw NotImplementedError()
    override fun queryIntentServices(intent: Intent, flags: Int): MutableList<ResolveInfo> = throw NotImplementedError()
    override fun queryIntentContentProviders(intent: Intent, flags: Int): MutableList<ResolveInfo> =
        throw NotImplementedError()
    override fun resolveContentProvider(authority: String, flags: Int): ProviderInfo? = throw NotImplementedError()
    override fun queryContentProviders(
        processName: String?,
        uid: Int,
        flags: Int
    ): MutableList<ProviderInfo> = throw NotImplementedError()
    override fun getInstrumentationInfo(className: ComponentName, flags: Int): InstrumentationInfo =
        throw NotImplementedError()
    override fun queryInstrumentation(targetPackage: String, flags: Int): MutableList<InstrumentationInfo> =
        throw NotImplementedError()
    override fun getDrawable(
        packageName: String,
        resid: Int,
        appInfo: ApplicationInfo?
    ): Drawable? = throw NotImplementedError()
    override fun getActivityIcon(activityName: ComponentName): Drawable = throw NotImplementedError()
    override fun getActivityIcon(intent: Intent): Drawable = throw NotImplementedError()
    override fun getActivityBanner(activityName: ComponentName): Drawable? = throw NotImplementedError()
    override fun getActivityBanner(intent: Intent): Drawable? = throw NotImplementedError()
    override fun getDefaultActivityIcon(): Drawable = throw NotImplementedError()
    override fun getApplicationIcon(info: ApplicationInfo): Drawable = throw NotImplementedError()
    override fun getApplicationIcon(packageName: String): Drawable = throw NotImplementedError()
    override fun getApplicationBanner(info: ApplicationInfo): Drawable? = throw NotImplementedError()
    override fun getApplicationBanner(packageName: String): Drawable? = throw NotImplementedError()
    override fun getActivityLogo(activityName: ComponentName): Drawable? = throw NotImplementedError()
    override fun getActivityLogo(intent: Intent): Drawable? = throw NotImplementedError()
    override fun getApplicationLogo(info: ApplicationInfo): Drawable? = throw NotImplementedError()
    override fun getApplicationLogo(packageName: String): Drawable? = throw NotImplementedError()
    override fun getUserBadgedIcon(drawable: Drawable, user: UserHandle): Drawable = throw NotImplementedError()
    override fun getUserBadgedDrawableForDensity(
        drawable: Drawable,
        user: UserHandle,
        badgeLocation: Rect?,
        badgeDensity: Int
    ): Drawable = throw NotImplementedError()
    override fun getUserBadgedLabel(label: CharSequence, user: UserHandle): CharSequence = throw NotImplementedError()
    override fun getText(
        packageName: String,
        resid: Int,
        appInfo: ApplicationInfo?
    ): CharSequence? = throw NotImplementedError()
    override fun getXml(
        packageName: String,
        resid: Int,
        appInfo: ApplicationInfo?
    ): XmlResourceParser? = throw NotImplementedError()
    override fun getApplicationLabel(info: ApplicationInfo): CharSequence = throw NotImplementedError()
    override fun getResourcesForActivity(activityName: ComponentName): Resources = throw NotImplementedError()
    override fun getResourcesForApplication(app: ApplicationInfo): Resources = throw NotImplementedError()
    override fun getResourcesForApplication(packageName: String): Resources = throw NotImplementedError()
    override fun setInstallerPackageName(targetPackage: String, installerPackageName: String?) =
        throw NotImplementedError()
    override fun getInstallerPackageName(packageName: String): String? = throw NotImplementedError()
    override fun addPackageToPreferred(packageName: String) = throw NotImplementedError()
    override fun removePackageFromPreferred(packageName: String) = throw NotImplementedError()
    override fun getPreferredPackages(flags: Int): MutableList<PackageInfo> = throw NotImplementedError()
    override fun addPreferredActivity(
        filter: IntentFilter,
        match: Int,
        set: Array<out ComponentName>?,
        activity: ComponentName
    ) = throw NotImplementedError()
    override fun clearPackagePreferredActivities(packageName: String) = throw NotImplementedError()
    override fun getPreferredActivities(
        outFilters: MutableList<IntentFilter>,
        outActivities: MutableList<ComponentName>,
        packageName: String?
    ): Int = throw NotImplementedError()
    override fun setComponentEnabledSetting(
        componentName: ComponentName,
        newState: Int,
        flags: Int
    ) = throw NotImplementedError()
    override fun getComponentEnabledSetting(componentName: ComponentName): Int = throw NotImplementedError()
    override fun setApplicationEnabledSetting(
        packageName: String,
        newState: Int,
        flags: Int
    ) = throw NotImplementedError()
    override fun getApplicationEnabledSetting(packageName: String): Int = throw NotImplementedError()
    override fun isSafeMode(): Boolean = throw NotImplementedError()
    override fun setApplicationCategoryHint(packageName: String, categoryHint: Int) = throw NotImplementedError()
    override fun getPackageInstaller(): PackageInstaller = throw NotImplementedError()
    override fun canRequestPackageInstalls(): Boolean = throw NotImplementedError()
    override fun getModuleInfo(packageName: String, flags: Int): ModuleInfo = throw NotImplementedError()
    override fun getInstalledModules(flags: Int): MutableList<ModuleInfo> = throw NotImplementedError()
    override fun verifyPendingInstall(id: Int, verificationCode: Int) = throw NotImplementedError()
    override fun extendVerificationTimeout(
        id: Int,
        verificationCodeAtTimeout: Int,
        millisecondsToDelay: Long
    ) = throw NotImplementedError()
}
