package me.rail.customgallery.models

import android.graphics.Bitmap
import android.net.Uri

data class Video(override val uri: Uri, override val name: String, val thumbnail: Bitmap?): Media(uri, name)
