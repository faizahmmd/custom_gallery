package me.rail.customgallery.screens.medialist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import me.rail.customgallery.R
import me.rail.customgallery.data.DataStorage
import me.rail.customgallery.databinding.ItemMediaBinding
import me.rail.customgallery.main.PermissionActivity
import me.rail.customgallery.models.Image
import me.rail.customgallery.models.Media
import me.rail.customgallery.models.Video

class MediaAdapter(
    private val medias: ArrayList<Media>,
    private val onImageClick: ((Int) -> Unit)? = null,
    private val onVideoClick: ((Int) -> Unit)? = null,
    private val glide: RequestManager,
    private val context: PermissionActivity
) :
    RecyclerView.Adapter<MediaAdapter.ImageViewHolder>() {
    private var checkboxVisible = false

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
            glide.load(item.uri).placeholder(R.drawable.ic_image_placeholder_24)
                .apply(requestOptions)
                .into(holder.binding.image)
        } else if (item is Video) {
            holder.binding.videoPlayIcon.visibility = View.VISIBLE
            holder.binding.durationTextView.text = item.duration
            holder.binding.durationTextView.visibility = View.VISIBLE

            val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
            glide.load(item.thumbnail).placeholder(R.drawable.ic_video_placeholder_24)
                .apply(requestOptions)
                .into(holder.binding.image)
        }
        if (context.multipleSelection) {
            holder.binding.checkBox.visibility =
                if (checkboxVisible) View.VISIBLE else View.INVISIBLE
        }
        holder.binding.image.setOnClickListener {
            context.showTickOnToolBar()

            if (checkboxVisible && context.multipleSelection) {
                if (holder.binding.checkBox.isChecked) {
                    holder.binding.checkBox.isChecked = false
                    medias[position].selected = false
                } else {
                    if (context.selectionLimit && context.selectionLimitCount != null) {
                        if (DataStorage.getSelectedMedias().size < context.selectionLimitCount!!) {
                            holder.binding.checkBox.isChecked = true
                            medias[position].selected = true
                        } else {
                            Toast.makeText(context, "Maximum selection reached", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        holder.binding.checkBox.isChecked = true
                        medias[position].selected = true
                    }
                }
                context.updateCountValueInToolBar()
            } else {
                medias[position].selected = true
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
            println(
                "<-------------${DataStorage.getSelectedMedias()}---------->"
            )
        }

        holder.binding.image.setOnLongClickListener {
            holder.binding.checkBox.visibility = View.VISIBLE
            checkboxVisible = true
            notifyDataSetChanged()
            true
        }

        holder.binding.checkBox.setOnClickListener {
            if (context.selectionLimit && context.selectionLimitCount != null) {
                if (DataStorage.getSelectedMedias().size < context.selectionLimitCount!!) {
                    medias[position].selected = holder.binding.checkBox.isChecked
                } else {
                    Toast.makeText(context, "Maximum selection reached", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                medias[position].selected = holder.binding.checkBox.isChecked
            }
            context.updateCountValueInToolBar()
            println(
                "<-------------${DataStorage.getSelectedMedias()}---------->"
            )
        }
    }

    override fun getItemCount(): Int {
        return medias.size
    }
}