package proton.android.pass

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import dagger.hilt.android.HiltAndroidApp
import proton.android.pass.initializer.MainInitializer
import javax.inject.Inject
import javax.inject.Provider

@HiltAndroidApp
class App : Application(), ImageLoaderFactory {
    @Inject
    lateinit var imageLoader: Provider<ImageLoader>

    override fun newImageLoader(): ImageLoader = imageLoader.get()

    override fun onCreate() {
        super.onCreate()
        MainInitializer.init(this)
    }
}
