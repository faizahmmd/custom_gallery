package me.rail.customgallery.screens.medialist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import me.rail.customgallery.R
import me.rail.customgallery.databinding.ItemMediaBinding
import me.rail.customgallery.models.Image
import me.rail.customgallery.models.Media

class MediaAdapter(
    private val medias: ArrayList<Media>,
    private val onImageClick: ((Int) -> Unit)? = null,
    private val glide: RequestManager
) :
    RecyclerView.Adapter<MediaAdapter.ImageViewHolder>() {
    var checkboxVisible = false

    class ImageViewHolder(val binding: ItemMediaBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ImageViewHolder {
        val inflater = LayoutInflater.from(viewGroup.context)
        val binding = ItemMediaBinding.inflate(inflater, viewGroup, false)

        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val item = medias[position]

        if (item is Image) {
            val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
            glide.load(item.uri).placeholder(R.drawable.ic_place_holder_24).apply(requestOptions)
                .into(holder.binding.image)
        }
        holder.binding.checkBox.visibility = if (checkboxVisible) View.VISIBLE else View.GONE
        holder.binding.image.setOnClickListener {
            if (checkboxVisible) {
                if (holder.binding.checkBox.isChecked) {
                    holder.binding.checkBox.isChecked = false
                    medias[position].selected = false
                } else {
                    holder.binding.checkBox.isChecked = true
                    medias[position].selected = true
                }
            } else {
                var correctPosition = position
                if (item is Image) {
                    onImageClick?.invoke(correctPosition)
                } else {
                    for (i in 0..position) {
                        if (medias[i] is Image) {
                            correctPosition--
                        }
                    }
                }
            }
        }

        holder.binding.image.setOnLongClickListener {
            holder.binding.checkBox.visibility = View.VISIBLE
            checkboxVisible = true
            notifyDataSetChanged()
            true
        }
        holder.binding.checkBox.setOnClickListener {
            medias[position].selected = holder.binding.checkBox.isChecked
        }
    }

    override fun getItemCount(): Int {
        return medias.size
    }
}