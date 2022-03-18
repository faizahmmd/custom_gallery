package me.rail.customgallery.data

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.FileProvider
import me.rail.customgallery.models.Image
import me.rail.customgallery.models.Media
import me.rail.customgallery.models.Video
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.lang.ref.WeakReference


class DataHandler(private val addVideoGallery: Boolean, private val addImageGallery: Boolean) {
    private val mediaMetadataRetriever = MediaMetadataRetriever()

    fun findMedia(context: Context) {
        val bucketFileColumn = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            MediaStore.Files.FileColumns.DATA
        } else {
            MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME
        }

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            bucketFileColumn,
            MediaStore.Files.FileColumns.MEDIA_TYPE
        )

        val sortOrder = "date_added DESC"

        val selection = if (addVideoGallery && addImageGallery) {
            (MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                    + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                    + " OR "
                    + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                    + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)
        } else if (!addImageGallery && addVideoGallery) {
            (MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                    + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)
        } else {
            (MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                    + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE)
        }

        val queryUri = MediaStore.Files.getContentUri("external")

        val cursor = context.contentResolver.query(
            queryUri,
            projection,
            selection,
            null,
            sortOrder
        ) ?: return

        DataStorage.setMediasCount(cursor.count)
        DataStorage.setImagesCount()
        DataStorage.setVideosCount()

        if (cursor.moveToFirst()) {
            var id: Long
            var uri: Uri
            var name: String
            var bucket: String

            val idColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val bucketColumn = cursor.getColumnIndex(bucketFileColumn)
            val typeColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE)

            do {
                id = cursor.getLong(idColumn)
                uri = Uri.withAppendedPath(queryUri, "" + id)
                name = cursor.getString(nameColumn)
                bucket = cursor.getString(bucketColumn)
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    val pathList = bucket.split("/")
                    bucket = pathList[pathList.lastIndex - 1]
                }
                val type = cursor.getInt(typeColumn)

                val media: Media
                if (type == 1) {
                    media = Image(uri, name)
                    DataStorage.addImage(media)
                    DataStorage.addImageToAlbum(bucket, media)
                } else {
                    mediaMetadataRetriever.setDataSource(context, uri)
                    val thumbnail = mediaMetadataRetriever.frameAtTime
                    val imageBitmap: WeakReference<Bitmap> = WeakReference<Bitmap>(thumbnail?.let {
                        Bitmap.createScaledBitmap(
                            it, thumbnail.height, thumbnail.width, false
                        ).copy(Bitmap.Config.RGB_565, true)
                    })
                    val bm: Bitmap? = imageBitmap.get()
                    val uriImage: Uri = saveImage(bm, context)
                    media = Video(uri, name, uriImage)
                    DataStorage.addVideo(media)
                    DataStorage.addVideoToAlbum(bucket, media)
                }
                DataStorage.addMedia(media)
                DataStorage.addMediaToAlbum(bucket, media)
            } while (cursor.moveToNext())
        }

        cursor.close()
    }

    private fun saveImage(bitmap: Bitmap?, context: Context): Uri {
        val imagesFolder = File(context.cacheDir, "images"+ System.currentTimeMillis())
        var uri: Uri? = null
        try {
            imagesFolder.mkdirs()
            val file = File(imagesFolder, "img_" + System.currentTimeMillis() + ".jpeg")
            val stream = FileOutputStream(file)
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
            uri = FileProvider.getUriForFile(
                context.applicationContext,
                "${context.packageName}.library.file.provider",
                file
            )
        } catch (e: FileNotFoundException) {
            println(e.toString())
        } catch (e: IOException) {
            println(e.toString())
        }
        return uri!!
    }
}