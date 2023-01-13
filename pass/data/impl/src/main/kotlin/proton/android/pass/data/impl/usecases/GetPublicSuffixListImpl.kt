package proton.android.pass.data.impl.usecases

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import proton.android.pass.data.api.usecases.GetPublicSuffixList
import proton.android.pass.data.impl.R
import proton.android.pass.log.api.PassLogger
import java.io.IOException
import javax.inject.Inject

class GetPublicSuffixListImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : GetPublicSuffixList {

    private var suffixes: Set<String> = emptySet()

    override fun invoke(): Set<String> {
        if (suffixes.isEmpty()) {
            suffixes = loadSuffixes()
        }
        return suffixes
    }

    private fun loadSuffixes(): Set<String> =
        try {
            val contents = context.resources
                .openRawResource(R.raw.public_suffix_list)
                .bufferedReader()
                .use { it.readText() }
            contents.lines().toHashSet()
        } catch (e: IOException) {
            PassLogger.e(TAG, e, "Error reading public_suffix_list")
            emptySet()
        }

    companion object {
        private const val TAG = "GetPublicSuffixListImpl"
    }
}



