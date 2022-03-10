package me.rail.customgallery.screens.albumlist

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.FileProvider
import androidx.core.view.marginTop
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import me.rail.customgallery.R
import me.rail.customgallery.databinding.ItemAlbumBinding
import me.rail.customgallery.databinding.ItemMediaBinding
import me.rail.customgallery.main.MainActivity
import me.rail.customgallery.models.Image
import me.rail.customgallery.models.Media
import java.io.File

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

            var countUnit: String = if (count > 1) {
                "photos"
            } else {
                "photo"
            }

            if (thumbnail is Image) {
                val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
                glide.load(thumbnail.uri).placeholder(R.drawable.ic_place_holder_24)
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

