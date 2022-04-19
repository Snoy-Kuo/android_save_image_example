package com.snoy.save_img_example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

// ref = https://medium.com/tech-takeaways/how-to-share-an-image-from-your-android-app-without-exposing-it-to-the-gallery-e9a7a214eb2c
// ref = https://developer.android.com/training/sharing/send
fun Bitmap.createSharingIntent(context: Context, fileName: String): Intent {
    val cachePath = this.saveToAppCacheFolder(context, fileName)
    val cachedImgFile = File(cachePath!!).apply {
        deleteOnExit()
    }
    val shareImageFileUri: Uri = FileProvider.getUriForFile(
        context,
        context.applicationContext.packageName + ".provider",
        cachedImgFile
    )
    val shareMessage = "Share image using"
    val sharingIntent = Intent(Intent.ACTION_SEND).apply {
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        type = "image/*"
        putExtra(Intent.EXTRA_STREAM, shareImageFileUri)
        putExtra(Intent.EXTRA_TEXT, shareMessage)
        putExtra(Intent.EXTRA_TITLE, shareMessage)
    }
    return sharingIntent
}

@Suppress("SameParameterValue")
fun Bitmap.createChooserIntent(context: Context, fileName: String): Intent {
    val sharingIntent = this.createSharingIntent(context, fileName)
    val shareMessage = "Share image using"
    return Intent.createChooser(sharingIntent, shareMessage)
}