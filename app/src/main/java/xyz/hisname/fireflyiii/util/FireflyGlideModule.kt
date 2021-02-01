/*
 * Copyright (c)  2018 - 2021 Daniel Quah
 * Copyright (c)  2021 ASDF Dev Pte. Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.sizeDp
import java.io.InputStream

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
        val factory = OkHttpUrlLoader.Factory()
        glide.registry.replace(GlideUrl::class.java, InputStream::class.java, factory)
    }

    companion object {
        private fun requestOptions(context: Context): RequestOptions {
            return RequestOptions()
                    .signature(ObjectKey(
                            System.currentTimeMillis() / (24 * 60 * 60 * 1000)))
                    .placeholder(IconicsDrawable(context).apply{
                        icon = GoogleMaterial.Icon.gmd_file_download
                        sizeDp = 24
                    })
                    .error(IconicsDrawable(context).apply{
                        icon = GoogleMaterial.Icon.gmd_error
                        sizeDp = 24
                    })
                    .encodeFormat(Bitmap.CompressFormat.WEBP)
                    .encodeQuality(70)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .format(DecodeFormat.PREFER_ARGB_8888)
                    .skipMemoryCache(false)
        }
    }
}