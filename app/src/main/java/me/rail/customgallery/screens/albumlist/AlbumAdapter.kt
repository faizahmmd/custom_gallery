package me.rail.customgallery.screens.albumlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import me.rail.customgallery.R
import me.rail.customgallery.databinding.ItemAlbumBinding
import me.rail.customgallery.models.Image
import me.rail.customgallery.models.Media
import me.rail.customgallery.models.Video

class AlbumAdapter(
    private val glide: RequestManager,
    private val onCameraClick: ((String) -> Unit)? = null,
    private val albums: LinkedHashMap<String, ArrayList<Media>>,
    private val onAlbumClick: ((String) -> Unit)? = null
) :
    RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>() {

    class AlbumViewHolder(val binding: ItemAlbumBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): AlbumViewHolder {
        val inflater = LayoutInflater.from(viewGroup.context)
        val binding = ItemAlbumBinding.inflate(inflater, viewGroup, false)

        return AlbumViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        if (position == 0) {
            holder.binding.image.load(R.drawable.ic_camera_24)
//            val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
//            glide.load(R.drawable.ic_camera_24).apply(requestOptions)
//                .into(holder.binding.image)
            holder.binding.name.text = ""
            holder.binding.count.text = ""

            holder.binding.image.setOnClickListener {
                onCameraClick?.invoke("camera")
            }
        } else {
            val thumbnail = ArrayList(albums.values)[position][0]
            val name = ArrayList(albums.keys)[position]
            val count = ArrayList(albums.values)[position].size
            var countUnit = ""
            var containsImages = false
            var containsVideos = false

            for (i in 0 until ArrayList(albums.values)[position].size) {
                if (ArrayList(albums.values)[position][i] is Image) {
                    containsImages = true
                } else if (ArrayList(albums.values)[position][i] is Video) {
                    containsVideos = true
                }
            }
            if (containsImages && !containsVideos) {
                countUnit = if (count > 1) {
                    "images"
                } else {
                    "image"
                }
            } else if (!containsImages && containsVideos) {
                countUnit = if (count > 1) {
                    "videos"
                } else {
                    "video"
                }
            } else {
                countUnit = if (count > 1) {
                    "items"
                } else {
                    "item"
                }
            }

            if (thumbnail is Image) {
                val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
                glide.load(thumbnail.uri).placeholder(R.drawable.ic_image_placeholder_24)
                    .apply(requestOptions)
                    .into(holder.binding.image)
            } else if (thumbnail is Video) {
                val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
                glide.load(thumbnail.thumbnail).placeholder(R.drawable.ic_video_placeholder_24)
                    .apply(requestOptions)
                    .into(holder.binding.image)
            }
            holder.binding.name.text = name
            holder.binding.count.text = "$count $countUnit"

            holder.binding.image.setOnClickListener {
                onAlbumClick?.invoke(name)
            }
        }
    }

    override fun getItemCount(): Int {
        return albums.size
    }
}

