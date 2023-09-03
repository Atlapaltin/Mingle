package com.iksanova.mingle.utils

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import com.iksanova.mingle.R
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.assist.ImageScaleType
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener

class UniversalImageLoderClass(private val mContext: Context) {

    private val defaultImage = R.color.gray

    fun getConfig(): ImageLoaderConfiguration {
        val displayImageOptions = DisplayImageOptions.Builder()
            .showImageOnLoading(defaultImage)
            .showImageForEmptyUri(defaultImage)
            .showImageOnFail(defaultImage)
            .cacheOnDisk(true)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .resetViewBeforeLoading(true)
            .imageScaleType(ImageScaleType.EXACTLY)
            .displayer(FadeInBitmapDisplayer(300))
            .build()

        return ImageLoaderConfiguration.Builder(mContext)
            .defaultDisplayImageOptions(displayImageOptions)
            .memoryCache(WeakMemoryCache())
            .diskCacheSize(100 * 1024 * 1024)
            .build()
    }

    companion object {
        fun setImage(imgUrl: String?, imageView: ImageView, mProgressBar: ProgressBar?) {
            val imageLoader = ImageLoader.getInstance()
            imageLoader.displayImage(imgUrl, imageView, object : ImageLoadingListener {
                override fun onLoadingStarted(imageUri: String?, view: View?) {
                    mProgressBar?.visibility = View.VISIBLE
                }

                override fun onLoadingFailed(imageUri: String?, view: View?, failReason: FailReason?) {
                    mProgressBar?.visibility = View.GONE
                }

                override fun onLoadingComplete(imageUri: String?, view: View?, loadedImage: Bitmap?) {
                    mProgressBar?.visibility = View.GONE
                }

                override fun onLoadingCancelled(imageUri: String?, view: View?) {
                    mProgressBar?.visibility = View.GONE
                }
            })
        }
    }
}