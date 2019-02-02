package xyz.hisname.fireflyiii.util

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import okhttp3.OkHttpClient
import java.io.InputStream
import java.util.concurrent.TimeUnit

@GlideModule
class FireflyGlideModule: AppGlideModule() {

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        val calculator = MemorySizeCalculator.Builder(context)
                .setMemoryCacheScreens(3F)
                .build()
        val memoryCache = calculator.memoryCacheSize.toLong()
        builder.setDiskCache(InternalCacheDiskCacheFactory(context,memoryCache))
        builder.setMemoryCache(LruResourceCache(memoryCache))
        builder.setDefaultRequestOptions(requestOptions(context))
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        val client = OkHttpClient.Builder()
                .readTimeout(20, TimeUnit.SECONDS)
                .connectTimeout(20, TimeUnit.SECONDS)
                .build()
        val factory = OkHttpUrlLoader.Factory(client)
        glide.registry.replace(GlideUrl::class.java, InputStream::class.java, factory)
    }

    companion object {
        private fun requestOptions(context: Context): RequestOptions {
            return RequestOptions()
                    .signature(ObjectKey(
                            System.currentTimeMillis() / (24 * 60 * 60 * 1000)))
                    .placeholder(IconicsDrawable(context)
                            .icon(GoogleMaterial.Icon.gmd_file_download)
                            .sizeDp(24))
                    .error(IconicsDrawable(context)
                            .icon(GoogleMaterial.Icon.gmd_error)
                            .sizeDp(24))
                    .encodeFormat(Bitmap.CompressFormat.WEBP)
                    .encodeQuality(70)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .format(DecodeFormat.PREFER_ARGB_8888)
                    .skipMemoryCache(false)
        }
    }
}