package me.rail.customgallery.screens.medialist

import android.opengl.Visibility
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import coil.load
import me.rail.customgallery.databinding.ItemMediaBinding
import me.rail.customgallery.models.Image
import me.rail.customgallery.models.Media

class MediaAdapter(
    private val medias: ArrayList<Media>,
    private val onImageClick: ((Int) -> Unit)? = null,
    private val onVideoClick: ((Int) -> Unit)? = null
):
    RecyclerView.Adapter<MediaAdapter.ImageViewHolder>() {

    class ImageViewHolder(val binding: ItemMediaBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ImageViewHolder {
        val inflater = LayoutInflater.from(viewGroup.context)
        val binding = ItemMediaBinding.inflate(inflater, viewGroup, false)

        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val item = medias[position]

        if (item is Image) {
            holder.binding.image.load(item.uri) {
                crossfade(true)
            }
        }

        holder.binding.image.setOnClickListener {
            var correctPosition = position
            if (item is Image) {
                onImageClick?.invoke(correctPosition)
            } else {
                for (i in 0..position) {
                    if (medias[i] is Image) {
                        correctPosition--
                    }
                }
                onVideoClick?.invoke(correctPosition)
            }
        }

        holder.binding.image.setOnLongClickListener{
           holder.binding.checkBox.visibility = View.VISIBLE
            true
        }
    }

    override fun getItemCount(): Int {
        return medias.size
    }
}