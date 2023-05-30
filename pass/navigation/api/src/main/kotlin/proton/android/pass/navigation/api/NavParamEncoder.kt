package proton.android.pass.navigation.api

import java.net.URLDecoder
import java.net.URLEncoder

object NavParamEncoder {

    private const val ENCODING = "UTF-8"

    fun encode(value: String): String = URLEncoder.encode(value, ENCODING)

    fun decode(value: String): String = URLDecoder.decode(value, ENCODING)
}
