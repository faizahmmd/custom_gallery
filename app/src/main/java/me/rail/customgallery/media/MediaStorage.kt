package me.rail.customgallery.media

import android.net.Uri
import me.rail.customgallery.models.Image
import me.rail.customgallery.models.Media
import me.rail.customgallery.models.Video

object  MediaStorage {
    private var mediasCount = 0

    private lateinit var medias: ArrayList<Media>
    private lateinit var mediaAlbums: LinkedHashMap<String, ArrayList<Media>>
    private lateinit var images: ArrayList<Image>
    private lateinit var imageAlbums: LinkedHashMap<String, ArrayList<Image>>
    private lateinit var videos: ArrayList<Video>
    private lateinit var videoAlbums: LinkedHashMap<String, ArrayList<Video>>

    fun setMediasCount(count: Int) {
        mediasCount = count
        medias = ArrayList(mediasCount)
        mediaAlbums = LinkedHashMap()
    }

    fun setImagesCount() {
        images = ArrayList()
        imageAlbums = LinkedHashMap()
    }

    fun setVideosCount() {
        videos = ArrayList()
        videoAlbums = LinkedHashMap()
    }

    fun addMedia(media: Media) {
        medias.add(media)
    }

    fun addImage(image: Image) {
        images.add(image)
    }

    fun addVideo(video: Video) {
        videos.add(video)
    }

    fun addMediaToAlbum(album: String, media: Media) {
        if (!checkAlbum(album)) {
            mediaAlbums[album] = ArrayList()

        }

        mediaAlbums[album]?.add(media)
    }

    fun addImageToAlbum(album: String, image: Image) {
        if (!checkImageAlbum(album)) {
            imageAlbums[album] = ArrayList()

        }

        imageAlbums[album]?.add(image)
    }

    fun addVideoToAlbum(album: String, video: Video) {
        if (!checkVideoAlbum(album)) {
            videoAlbums[album] = ArrayList()

        }

        videoAlbums[album]?.add(video)
    }

    private fun checkAlbum(album: String): Boolean {
        return mediaAlbums.containsKey(album)
    }

    private fun checkImageAlbum(album: String): Boolean {
        return imageAlbums.containsKey(album)
    }

    private fun checkVideoAlbum(album: String): Boolean {
        return videoAlbums.containsKey(album)
    }

    fun getMedias(): ArrayList<Media> {
        return medias
    }

    fun getImages(): ArrayList<Image> {
        return images
    }

    fun getVideos(): ArrayList<Video> {
        return videos
    }

    fun getAlbums(): LinkedHashMap<String, ArrayList<Media>> {
        val arrayList = ArrayList<Media>()
        arrayList.add(Media(uri = Uri.EMPTY, name = ""))
        var mediaAlbumsUpdated = mediaAlbums.clone() as LinkedHashMap<String, ArrayList<Media>>
        mediaAlbums.clear()
        mediaAlbums["camera"] = arrayList
        mediaAlbums.putAll(mediaAlbumsUpdated)
        return mediaAlbums
    }

    fun getImageAlbums(): LinkedHashMap<String, ArrayList<Image>> {
        return imageAlbums
    }

    fun getVideoAlbums(): LinkedHashMap<String, ArrayList<Video>> {
        return videoAlbums
    }

    fun getMediaByPosition(position: Int, album: String?): Media {
        return if (album == null) {
            medias[position]
        } else {
            mediaAlbums[album]!![position]
        }
    }

    fun getImageByPosition(position: Int, album: String?): Image {
        return if (album == null) {
            images[position]
        } else {
            imageAlbums[album]!![position]
        }
    }

    fun getVideoByPosition(position: Int, album: String?): Video {
        return if (album == null) {
            videos[position]
        } else {
            videoAlbums[album]!![position]
        }
    }

    fun getMediasByAlbum(album: String): ArrayList<Media> {
        return mediaAlbums[album]!!
    }

    fun getImagesByAlbum(album: String): ArrayList<Image> {
        return imageAlbums[album]!!
    }

    fun getVideosByAlbum(album: String): ArrayList<Video> {
        return videoAlbums[album]!!
    }

    fun getCount(album: String?): Int {
        return if (album == null) {
            mediasCount
        } else {
            mediaAlbums[album]!!.size
        }
    }

    fun getImageCount(album: String?): Int {
        return if (album == null) {
            images.size
        } else {
            imageAlbums[album]!!.size
        }
    }

    fun getVideoCount(album: String?): Int {
        return if (album == null) {
            videos.size
        } else {
            videoAlbums[album]!!.size
        }
    }
}