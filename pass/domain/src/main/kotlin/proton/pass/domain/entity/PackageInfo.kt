package proton.pass.domain.entity

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class PackageName(val value: String)

@Serializable
@JvmInline
value class AppName(val value: String)

@Serializable
data class PackageInfo(
    val packageName: PackageName,
    val appName: AppName
)
