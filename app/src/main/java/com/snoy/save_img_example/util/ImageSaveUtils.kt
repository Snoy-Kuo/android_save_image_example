package com.snoy.save_img_example.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL

//ref = https://microeducate.tech/android-share-image-in-imageview-without-saving-in-sd-card/
fun Bitmap?.saveToAppFolder(parentDir: File, fileName: String): String? {
    if (this == null) {
        return null
    }
    // save bitmap to directory
    try {
        //cacheDir = /data/app/app_folder/cache/
        //filesDir = /data/app/app_folder/files/
        //externalCacheDir = /sdcard/Android/data/app_folder/cache/
        //getExternalFilesDir(null) = /sdcard/Android/data/app_folder/files/

        val path = File(parentDir, "images")
        path.mkdirs() // don't forget to make the directory
        val file = File(path, "$fileName.png")
        val stream = FileOutputStream(file) // overwrites this image every time
        this.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()
        return file.absolutePath
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
        return null
    } catch (e: IOException) {
        e.printStackTrace()
        return null
    }
}

fun Bitmap?.saveToAppFileFolder(context: Context, fileName: String): String? {
    return this.saveToAppFolder(context.getExternalFilesDir(null)!!, fileName)
}

fun Bitmap?.saveToAppCacheFolder(context: Context, fileName: String): String? {
    return this.saveToAppFolder(context.externalCacheDir!!, fileName)
}

//ref = https://medium.com/@thuat26/how-to-save-file-to-external-storage-in-android-10-and-above-a644f9293df2
fun Bitmap?.saveFileToExternalStorageBeforeQ(
    context: Context,
    fileName: String,
    type: String
): Boolean {
    try {
        val cachePath = this.saveToAppCacheFolder(context, fileName) ?: return false
        val cachedImgFile = File(cachePath)
        val cachedImgUrl: URL = cachedImgFile.toURI().toURL()
        saveFileToExternalStorageBeforeQ(cachedImgUrl, "$fileName.png", type)
        cachedImgFile.delete()
    } catch (e: Exception) {
        Log.e("RDTest", "e= $e")
        return false
    }
    return true
}

private fun saveFileToExternalStorageBeforeQ(url: URL, fileName: String, type: String) {
    @Suppress("DEPRECATION") //for target < Q
    val target = File(
        Environment.getExternalStoragePublicDirectory(type),
        fileName
    )
    url.openStream().use { input ->
        FileOutputStream(target).use { output ->
            input.copyTo(output)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
fun saveFileUsingMediaStore(context: Context, url: URL, fileName: String) {
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS) //Q
    }
    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues) //Q
    if (uri != null) {
        url.openStream().use { input ->
            resolver.openOutputStream(uri).use { output ->
                input.copyTo(output!!, DEFAULT_BUFFER_SIZE)
            }
        }
    }
}