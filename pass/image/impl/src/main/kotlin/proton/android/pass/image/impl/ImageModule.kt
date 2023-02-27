package proton.android.pass.image.impl

import android.content.Context
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ImageModule {

    @Provides
    @Singleton
    fun providesImageLoader(
        @ApplicationContext context: Context,
        fetcher: ImageFetcherFactory
    ): ImageLoader = ImageLoader.Builder(context)
        .components {
            add(fetcher)
            add(SvgDecoder.Factory())
            if (SDK_INT >= Build.VERSION_CODES.P) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ImageBindingModule {

    @Binds
    abstract fun bindClearIconCache(impl: ClearIconCacheImpl): ClearIconCache
}
