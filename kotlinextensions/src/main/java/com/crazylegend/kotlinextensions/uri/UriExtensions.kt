package com.crazylegend.kotlinextensions.uri

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import okhttp3.internal.closeQuietly


/**
 * Created by hristijan on 6/13/19 to long live and prosper !
 */

fun Uri.toBytes(context: Context): ByteArray? {
     context.contentResolver.openInputStream(this).use {
       return it?.readBytes()
    }
}


fun Context.uriToBytes(uri: Uri): ByteArray? {
    contentResolver.openInputStream(uri).use {
        return it?.readBytes()
    }
}

fun ContentResolver.uriToBytes(uri: Uri): ByteArray? {
    openInputStream(uri).use {
        return it?.readBytes()
    }
}

fun ContentResolver.toBitmap(uri: Uri) : Bitmap?{
    val pfd = openFileDescriptor(uri , "w")
    val fd = pfd?.fileDescriptor
    val image = BitmapFactory.decodeFileDescriptor(fd)
    pfd?.closeQuietly()
    return image
}

fun Uri.toBitmap(context: Context) : Bitmap?{
    val pfd = context.contentResolver.openFileDescriptor(this , "w")
    val fd = pfd?.fileDescriptor
    val image = BitmapFactory.decodeFileDescriptor(fd)
    pfd?.closeQuietly()
    return image
}

