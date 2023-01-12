package proton.android.pass.data.api.usecases

interface GetPublicSuffixList {
    operator fun invoke(): List<String>
}
