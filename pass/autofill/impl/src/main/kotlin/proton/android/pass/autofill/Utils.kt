package proton.android.pass.autofill

import android.app.assist.AssistStructure
import android.content.Context
import android.service.autofill.FillContext
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.AndroidUtils.getApplicationName
import proton.android.pass.data.api.url.UrlSanitizer

object Utils {

    fun getApplicationPackageName(windowNode: AssistStructure.WindowNode): String {
        val wholePackageName = windowNode.title
        val packageComponents = wholePackageName.split("/")
        return packageComponents.first()
    }

    fun getWindowNodes(fillContext: FillContext): List<AssistStructure.WindowNode> {
        val structure: AssistStructure = fillContext.structure
        return if (structure.windowNodeCount > 0)
            (0 until structure.windowNodeCount).map { structure.getWindowNodeAt(it) } else
            emptyList()
    }

    fun getTitle(
        context: Context,
        urlOption: Option<String>,
        packageNameOption: Option<String>
    ): String = when (urlOption) {
        None -> when (packageNameOption) {
            None -> ""
            is Some -> getApplicationName(context, packageNameOption.value).value() ?: ""
        }
        is Some -> when (val res = UrlSanitizer.getDomain(urlOption.value)) {
            LoadingResult.Loading -> ""
            is LoadingResult.Error -> ""
            is LoadingResult.Success -> res.data
        }
    }
}
