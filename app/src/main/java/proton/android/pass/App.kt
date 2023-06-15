package proton.android.pass

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.runBlocking
import proton.android.pass.initializer.MainInitializer
import proton.android.pass.preferences.HasAuthenticated
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject
import javax.inject.Provider

@HiltAndroidApp
class App : Application(), ImageLoaderFactory {
    @Inject
    lateinit var imageLoader: Provider<ImageLoader>

    @Inject
    lateinit var preferenceRepository: UserPreferencesRepository

    override fun newImageLoader(): ImageLoader = imageLoader.get()

    override fun onCreate() {
        super.onCreate()
        runBlocking {
            preferenceRepository.setHasAuthenticated(HasAuthenticated.NotAuthenticated)
        }
        MainInitializer.init(this)
    }
}
