package proton.android.pass.image.impl

import android.content.Context
import coil.ImageLoader
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
            add(ImageDecoderDecoder.Factory())
        }
        .build()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ImageBindingModule {

    @Binds
    abstract fun bindClearIconCache(impl: ClearIconCacheImpl): ClearIconCache
}
