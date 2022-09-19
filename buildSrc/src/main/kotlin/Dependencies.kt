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
        add(AndroidX.Compose.foundationLayout)
        add(AndroidX.Compose.iconsExtended)
        add(AndroidX.Compose.material)
        add(AndroidX.Compose.runtime)
        add(AndroidX.Compose.ui)
        add(AndroidX.Compose.uiTooling)
    }

    val composeDebugLibs = mutableListOf<String>().apply {
        add(AndroidX.Compose.uiTooling)
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
        add(AndroidX.Core.splashscreen)
        add(AndroidX.Hilt.compiler)
        add(AndroidX.Hilt.navigationCompose)
        add(AndroidX.Hilt.work)
        add(AndroidX.Navigation.compose)
        add(AndroidX.Paging.compose)
        add(AndroidX.Paging.runtime)
        add(AndroidX.Room.ktx)
        add(AndroidX.Work.runtimeKtx)
        addAll(composeLibs)
        add(Proton.Core.account)
        add(Proton.Core.accountManager)
        add(Proton.Core.accountManagerPresentationCompose)
        add(Proton.Core.auth)
        add(Proton.Core.challenge)
        add(Proton.Core.country)
        add(Proton.Core.crypto)
        add(Proton.Core.cryptoValidator)
        add(Proton.Core.data)
        add(Proton.Core.dataRoom)
        add(Proton.Core.domain)
        add(Proton.Core.eventManager)
        add(Proton.Core.featureFlag)
        add(Proton.Core.humanVerification)
        add(Proton.Core.key)
        add(Proton.Core.network)
        add(Proton.Core.payment)
        // add(Proton.Core.paymentIap)
        add(Proton.Core.plan)
        add(Proton.Core.presentation)
        add(Proton.Core.presentationCompose)
        add(Proton.Core.report)
        add(Proton.Core.user)
        add(Proton.Core.userSettings)
        add(Proton.Core.utilAndroidDagger)
        add(Proton.Core.utilKotlin)
        add(JakeWharton.timber)
        add(KotlinX.serializationJson)
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

    // region Pass Dagger
    val passDaggerLibs = mutableListOf<String>().apply {
        add(AndroidX.Paging.common)
        add(AndroidX.Work.runtimeKtx)
        add(Proton.Core.accountManager)
        add(Proton.Core.crypto)
        add(Proton.Core.domain)
        add(Proton.Core.key)
        add(Proton.Core.network)
        add(Proton.Core.user)
        add(Proton.Core.utilAndroidDagger)
        add(Proton.Core.utilKotlin)
    }
    val passDaggerAnnotationProcessors = mutableListOf<String>().apply {
        add(AndroidX.Room.compiler)
    }
    // endregion

    // region Pass Data
    val passDataLibs = mutableListOf<String>().apply {
        add(AndroidX.Hilt.work)
        add(AndroidX.Paging.common)
        add(AndroidX.Room.ktx)
        add(AndroidX.Work.runtimeKtx)
        add(KotlinX.coroutinesCore)
        add(KotlinX.serializationJson)
        add(Proton.Core.account)
        add(Proton.Core.accountManager)
        add(Proton.Core.crypto)
        add(Proton.Core.data)
        add(Proton.Core.dataRoom)
        add(Proton.Core.domain)
        add(Proton.Core.key)
        add(Proton.Core.network)
        add(Proton.Core.user)
        add(Proton.Core.utilKotlin)
        add(Squareup.retrofit)
    }
    val passDataAnnotationProcessors = mutableListOf<String>().apply {
        add(AndroidX.Room.compiler)
    }
    // endregion

    // region Pass Domain
    val passDomainLibs = mutableListOf<String>().apply {
        add(AndroidX.Paging.common)
        add(KotlinX.coroutinesCore)
        add(Proton.Core.account)
        add(Proton.Core.accountManager)
        add(Proton.Core.crypto)
        add(Proton.Core.domain)
        add(Proton.Core.key)
        add(Proton.Core.user)
        add(Proton.Core.utilKotlin)
    }
    // endregion

    // region Pass Presentation
    val passPresentationLibs = mutableListOf<String>().apply {
        addAll(composeLibs)
        add(Proton.Core.accountManager)
        add(Proton.Core.accountManagerPresentationCompose)
        add(Proton.Core.auth)
        add(Proton.Core.domain)
        add(Proton.Core.key)
        add(Proton.Core.network)
        add(Proton.Core.presentation)
        add(Proton.Core.presentationCompose)
        add(Proton.Core.user)
        add(Proton.Core.utilKotlin)
    }

    val passPresentationDebugLibs = arrayListOf<String>().apply {
        add(AndroidX.Compose.uiTestManifest)
    }
    val passPresentationAnnotationProcessors = arrayListOf<String>()
    // endregion

    // region Autofill Service
    val passAutofillServiceLibs = mutableListOf<String>().apply {
        add(AndroidX.Autofill.autofill)
        add(KotlinX.coroutinesCore)
        add(Proton.Core.domain)
        add(Proton.Core.key)
        add(Proton.Core.user)
        add(Proton.Core.utilKotlin)
    }
    // endregion

    // region Autofill Sample
    // region App
    val passAutofillSampleLibs = mutableListOf<String>().apply {
        add(Accompanist.animationNavigation)
        add(Accompanist.insets)
        add(Accompanist.pager)
        add(Accompanist.systemUiController)
        add(AndroidX.Activity.ktx)
        add(AndroidX.Compose.foundationLayout)
        addAll(composeLibs)
        add(Material.material)
        add(Proton.Core.presentationCompose)
    }
    // endregion

    // region Test
    val testLibs = mutableListOf<String>().apply {
        add(CashApp.turbine)
        add(Google.truth)
        add(Kotlin.test)
        add(KotlinX.coroutinesTest)
        add(Junit.junit)
        add(Mockk.mockk)
        add(Proton.Core.testKotlin)
    }
    val androidTestLibs = mutableListOf<String>().apply {
        add(AndroidX.Compose.uiTest)
        add(AndroidX.Compose.uiTestJUnit)
        add(AndroidX.Test.core)
        add(AndroidX.Test.coreKtx)
        add(AndroidX.Test.runner)
        add(AndroidX.Test.rules)
        add(AndroidX.Test.espresso)
        add(Kotlin.test)
        add(Mockk.mockkAndroid)
        add(Proton.Core.testAndroidInstrumented)
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
