package me.rail.customgallery.screens.image

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import me.rail.customgallery.data.DataStorage

class ImageAdapter(fragment: FragmentActivity, private val albumName: String?) :
    FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = DataStorage.getImageCount(albumName)

    override fun createFragment(position: Int): Fragment {

        return ImageFragment.newInstance(
            DataStorage.getImageByPosition(position, albumName).uri.toString(),
        )
    }
}