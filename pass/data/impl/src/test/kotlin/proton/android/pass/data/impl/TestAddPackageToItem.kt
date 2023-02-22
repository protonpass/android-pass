package proton.android.pass.data.impl

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import proton.android.pass.data.impl.extensions.hasPackageName
import proton.android.pass.data.impl.extensions.with
import proton.pass.domain.entity.AppName
import proton.pass.domain.entity.PackageInfo
import proton.pass.domain.entity.PackageName
import proton_pass_item_v1.ItemV1

internal class TestAddPackageToItem {

    @Test
    fun `hasPackageName detects if item is not present`() {
        val source = createItemWithPackageName(null)
        assertThat(source.hasPackageName(PackageName("not.present"))).isFalse()
    }

    @Test
    fun `hasPackageName detects if item is present`() {
        val packageName = "abc.def"
        val source = createItemWithPackageName(packageName)
        assertThat(source.hasPackageName(PackageName(packageName))).isTrue()
    }

    @Test
    fun `addPackageName should add package name`() {

        val originalPackageName = "original.package"
        val newPackageInfo = PackageInfo(
            PackageName("new.package.name"),
            AppName("")
        )
        val source = createItemWithPackageName(originalPackageName)
        val updated = source.with(newPackageInfo)

        val allowedApps = updated.platformSpecific.android.allowedAppsList
        assertThat(allowedApps.size).isEqualTo(2)

        assertThat(allowedApps[0].packageName).isEqualTo(originalPackageName)
        assertThat(allowedApps[1].packageName).isEqualTo(newPackageInfo.packageName.value)

        // Ensure source has not been updated
        assertThat(source.platformSpecific.android.allowedAppsList.size).isEqualTo(1)
    }

    private fun createItemWithPackageName(packageName: String?): ItemV1.Item {
        val androidSpecificBuilder = ItemV1.AllowedAndroidApp.newBuilder()
        packageName?.let { androidSpecificBuilder.setPackageName(it) }

        val androidSpecific = androidSpecificBuilder.build()
        return ItemV1.Item.newBuilder()
            .setPlatformSpecific(
                ItemV1.PlatformSpecific.newBuilder()
                    .setAndroid(
                        ItemV1.AndroidSpecific.newBuilder()
                            .addAllowedApps(androidSpecific)
                            .build()
                    )
                    .build()
            )
            .build()
    }
}


