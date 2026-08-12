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

package proton.android.pass.autofill.autofillhealth.troubleshooting.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import proton.android.pass.autofill.service.R
import javax.inject.Inject

class InstalledBrowsersProviderImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val workingBrowsersStore: AutofillWorkingBrowsersStore
) : InstalledBrowsersProvider {

    override suspend fun getInstalledBrowsers(): List<BrowserInfo> = withContext(Dispatchers.IO) {
        val packageManager = context.packageManager
        val compatVersionCaps = readCompatVersionCaps()
        val workingBrowsers = workingBrowsersStore.getWorkingBrowsers()
        val intent = Intent(Intent.ACTION_VIEW, Uri.fromParts(SCHEME_HTTP, "", null))
            .addCategory(Intent.CATEGORY_BROWSABLE)
        val queryFlags = PackageManager.GET_RESOLVED_FILTER or PackageManager.MATCH_ALL

        packageManager.queryIntentActivities(intent, queryFlags)
            .filter { resolveInfo -> resolveInfo.isFullBrowser() }
            .mapNotNull { resolveInfo ->
                val packageName = resolveInfo.activityInfo?.packageName
                    ?: return@mapNotNull null
                BrowserInfo(
                    packageName = packageName,
                    label = resolveInfo.loadLabel(packageManager).toString(),
                    coverage = BrowserAutofillCoverageResolver.resolve(
                        browserVersionCode = versionCodeOf(packageManager, packageName),
                        compatMaxVersionCode = compatVersionCaps[packageName],
                        hasBeenSeenWorking = packageName in workingBrowsers
                    )
                )
            }
            .distinctBy { browser -> browser.packageName }
            // Browsers that need setup first, then alphabetical.
            .sortedWith(
                compareBy(
                    { browser -> browser.coverage != BrowserAutofillCoverage.NeedsSetup },
                    { browser -> browser.label.lowercase() }
                )
            )
    }

    private fun ResolveInfo.isFullBrowser(): Boolean {
        val resolvedFilter = filter ?: return false
        return FullBrowserResolver.isFullBrowser(
            hasAuthorities = resolvedFilter.authoritiesIterator() != null,
            schemes = resolvedFilter.schemesIterator()?.asSequence()?.toSet().orEmpty()
        )
    }

    private fun versionCodeOf(packageManager: PackageManager, packageName: String): Long = runCatching {
        val info = packageManager.getPackageInfo(packageName, 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            info.versionCode.toLong()
        }
    }.getOrDefault(Long.MAX_VALUE)

    private fun readCompatVersionCaps(): Map<String, Long> {
        val caps = mutableMapOf<String, Long>()
        val parser = context.resources.getXml(R.xml.autofill_service)
        try {
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == COMPAT_PACKAGE_TAG) {
                    val name = parser.getAttributeValue(ANDROID_NAMESPACE, ATTR_NAME)
                    val maxVersion = parser
                        .getAttributeValue(ANDROID_NAMESPACE, ATTR_MAX_LONG_VERSION_CODE)
                        ?.toLongOrNull()
                    if (name != null && maxVersion != null) {
                        caps[name] = maxVersion
                    }
                }
                eventType = parser.next()
            }
        } finally {
            parser.close()
        }
        return caps
    }

    private companion object {
        private const val SCHEME_HTTP = "http"
        private const val ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android"
        private const val COMPAT_PACKAGE_TAG = "compatibility-package"
        private const val ATTR_NAME = "name"
        private const val ATTR_MAX_LONG_VERSION_CODE = "maxLongVersionCode"
    }
}
