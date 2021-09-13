import org.gradle.api.artifacts.dsl.DependencyHandler
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

@Suppress("unused")
object Dependencies {

    // region Used accross modules
    val hiltAnnotationProcessors = mutableListOf<String>().apply {
        add(AndroidX.Hilt.compiler)
        add(Dagger.hiltDaggerCompiler)
    }

    // region Compose
    val composeLibs = mutableListOf<String>().apply {
        add(AndroidX.Activity.compose)
        add(AndroidX.Compose.foundation)
        add(AndroidX.Compose.material)
        add(AndroidX.Compose.runtime)
        add(AndroidX.Compose.ui)
        add(AndroidX.Compose.uiTooling)
    }

    val composeDebugLibs = mutableListOf<String>().apply {
        add(AndroidX.Compose.uiTestManifest)
    }
    // endregion
    // endregion

    // region App
    val appLibs = mutableListOf<String>().apply {
        add(Accompanist.animationNavigation)
        add(Accompanist.insets)
        add(Accompanist.pager)
        add(Accompanist.systemUiController)
        add(AndroidX.Activity.ktx)
        add(AndroidX.Compose.foundationLayout)
        add(AndroidX.Hilt.compiler)
        add(AndroidX.Hilt.navigationCompose)
        add(AndroidX.Hilt.work)
        add(AndroidX.Navigation.compose)
        add(AndroidX.Paging.compose)
        add(AndroidX.Paging.runtime)
        add(AndroidX.Room.ktx)
        add(AndroidX.Work.runtimeKtx)
        addAll(composeLibs)
        add(Core.account)
        add(Core.accountManager)
        add(Core.auth)
        add(Core.country)
        add(Core.crypto)
        add(Core.data)
        add(Core.dataRoom)
        add(Core.domain)
        add(Core.humanVerification)
        add(Core.key)
        add(Core.network)
        add(Core.payment)
        add(Core.plan)
        add(Core.presentation)
        add(Core.user)
        add(Core.userSettings)
        add(Core.utilKotlin)
        add(Gotev.cookieStore)
        add(JakeWharton.timber)
        add(Material.material)
        add(Squareup.okhttp)
        add(Squareup.plumber)
    }
    val appAnnotationProcessors = mutableListOf<String>().apply {
        add(AndroidX.Room.compiler)
        addAll(hiltAnnotationProcessors)
    }
    // endregion

    val appDebug = mutableListOf<String>().apply {
        //add(Squareup.leakCanary)
    }
    // endregion

    // region Drive Dagger
    val driveDaggerLibs = mutableListOf<String>().apply {
        add(AndroidX.Paging.common)
        add(AndroidX.Work.runtimeKtx)
        add(Core.accountManager)
        add(Core.crypto)
        add(Core.domain)
        add(Core.key)
        add(Core.network)
        add(Core.user)
        add(Core.utilKotlin)
    }
    val driveDaggerAnnotationProcessors = mutableListOf<String>().apply {
        add(AndroidX.Room.compiler)
    }
    // endregion

    // region Drive Data
    val driveDataLibs = mutableListOf<String>().apply {
        add(AndroidX.Hilt.work)
        add(AndroidX.Lifecycle.liveDataKtx)
        add(AndroidX.Paging.common)
        add(AndroidX.Room.ktx)
        add(AndroidX.Work.runtimeKtx)
        add(Core.account)
        add(Core.accountManager)
        add(Core.crypto)
        add(Core.data)
        add(Core.dataRoom)
        add(Core.domain)
        add(Core.key)
        add(Core.network)
        add(Core.user)
        add(Core.utilKotlin)
        add(KotlinX.coroutinesCore)
        add(KotlinX.serializationJson)
        add(Squareup.retrofit)
    }
    val driveDataAnnotationProcessors = mutableListOf<String>().apply {
        add(AndroidX.Room.compiler)
    }
    // endregion

    // region Drive Domain
    val driveDomainLibs = mutableListOf<String>().apply {
        add(AndroidX.Paging.common)
        add(Core.accountManager)
        add(Core.crypto)
        add(Core.domain)
        add(Core.key)
        add(Core.utilKotlin)
        add(KotlinX.coroutinesCore)
        add(KotlinX.serializationJson)
    }
    // endregion

    // region Drive Presentation
    val drivePresentationLibs = mutableListOf<String>().apply {
        addAll(composeLibs)
        add(Core.accountManager)
        add(Core.auth)
        add(Core.domain)
        add(Core.key)
        add(Core.network)
        add(Core.presentation)
        add(Core.user)
        add(Core.utilKotlin)
    }

    val drivePresentationDebugLibs = arrayListOf<String>().apply {
        add(AndroidX.Compose.uiTestManifest)
    }
    val drivePresentationAnnotationProcessors = arrayListOf<String>()
    // endregion

    val driveFilesListLibs = mutableListOf<String>().apply {
        addAll(composeLibs)
        add(AndroidX.Paging.compose)
        add(AndroidX.Paging.runtime)
        add(Core.domain)
        add(Core.key)
        add(Core.user)
        add(Core.key)
        add(Core.utilKotlin)
        add(Dagger.hiltAndroid)
    }

    val drivePreviewLibs = mutableListOf<String>().apply {
        addAll(composeLibs)
        add(Coil.compose)
        add(Core.utilKotlin)
    }

    // region Test
    val testLibs = mutableListOf<String>().apply {
        add(Test.junit)
    }
    val androidTestLibs = mutableListOf<String>().apply {
        add(Test.core)
        add(Test.coreKtx)
        add(Test.runner)
        add(Test.rules)
        add(AndroidX.Compose.uiTest)
        add(AndroidX.Compose.uiTestJUnit)
    }
    // endregion
}

// util functions for adding the different type dependencies from build.gradle file
fun DependencyHandler.kapt(list: List<String>) {
    list.forEach { dependency ->
        add("kapt", dependency)
    }
}

fun DependencyHandler.implementation(list: List<String>) {
    list.forEach { dependency ->
        add("implementation", dependency)
    }
}

fun DependencyHandler.debugImplementation(list: List<String>) {
    list.forEach { dependency ->
        add("debugImplementation", dependency)
    }
}

fun DependencyHandler.androidTestImplementation(list: List<String>) {
    list.forEach { dependency ->
        add("androidTestImplementation", dependency)
    }
}

fun DependencyHandler.testImplementation(list: List<String>) {
    list.forEach { dependency ->
        add("testImplementation", dependency)
    }
}

fun String.runCommand(
    workingDir: File = File("."),
    timeoutAmount: Long = 60,
    timeoutUnit: TimeUnit = TimeUnit.SECONDS
): String = ProcessBuilder(split("\\s(?=(?:[^'\"`]*(['\"`])[^'\"`]*\\1)*[^'\"`]*$)".toRegex()))
    .directory(workingDir)
    .redirectOutput(ProcessBuilder.Redirect.PIPE)
    .redirectError(ProcessBuilder.Redirect.PIPE)
    .start()
    .apply { waitFor(timeoutAmount, timeoutUnit) }
    .run {
        val error = errorStream.bufferedReader().readText().trim()
        if (error.isNotEmpty()) {
            throw IOException(error)
        }
        inputStream.bufferedReader().readText().trim()
    }
