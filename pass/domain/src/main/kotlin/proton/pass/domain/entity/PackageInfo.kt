package proton.pass.domain.entity

@JvmInline
value class PackageName(val value: String)

@JvmInline
value class AppName(val value: String)

data class PackageInfo(
    val packageName: PackageName,
    val appName: AppName
)
