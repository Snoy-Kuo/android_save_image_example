package com.snoy.save_img_example.util

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import coil.drawable.CrossfadeDrawable
import coil.load
import coil.request.ImageRequest

fun ImageView.getBitmap(): Bitmap? {
    val drawable = this.drawable
    if (drawable is BitmapDrawable) {
        return drawable.bitmap
    } else if (drawable is CrossfadeDrawable) {
        return if (drawable.end is BitmapDrawable) {
            (drawable.end as BitmapDrawable).bitmap
        } else null
    }
    return null
}

fun ImageView.loadUsingCoil(data: Any?, onStart: (request: ImageRequest) -> Unit) {
    this.load(data) {
        crossfade(1000)
        placeholder(this@loadUsingCoil.drawable)
        listener(onStart = onStart)
    }
}