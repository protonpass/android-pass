package me.proton.pass.autofill

import android.app.assist.AssistStructure
import android.content.Context
import android.content.pm.PackageManager
import android.service.autofill.FillContext
import me.proton.pass.autofill.entities.AssistInfo
import me.proton.pass.common.api.None
import me.proton.pass.common.api.Result
import me.proton.pass.common.api.Some
import me.proton.android.pass.data.api.UrlSanitizer

object Utils {

    fun getApplicationPackageName(windowNode: AssistStructure.WindowNode): String {
        val wholePackageName = windowNode.title
        val packageComponents = wholePackageName.split("/")
        return packageComponents.first()
    }

    fun getApplicationName(context: Context, packageName: String): String {
        val packageManager = context.packageManager
        val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        return packageManager.getApplicationLabel(appInfo).toString()
    }

    fun getWindowNodes(fillContext: FillContext): List<AssistStructure.WindowNode> {
        val structure: AssistStructure = fillContext.structure
        return if (structure.windowNodeCount > 0)
            (0 until structure.windowNodeCount).map { structure.getWindowNodeAt(it) } else
            emptyList()
    }

    fun getTitle(context: Context, assistInfo: AssistInfo, packageName: String): String =
        when (assistInfo.url) {
            None -> getApplicationName(context, packageName)
            is Some -> when (val res = UrlSanitizer.getDomain(assistInfo.url.value)) {
                Result.Loading -> ""
                is Result.Error -> ""
                is Result.Success -> res.data
            }
        }
}
