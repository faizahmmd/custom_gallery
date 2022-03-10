package me.rail.customgallery.screens.viewpager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import me.rail.customgallery.databinding.FragmentImageViewPagerBinding

class ImageViewPagerFragment: Fragment() {
    private lateinit var binding: FragmentImageViewPagerBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentImageViewPagerBinding.inflate(inflater, container, false)

        binding.root.isClickable = true

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.takeIf { it.containsKey(ARG_POSITION) }?.apply {
            binding.pager.post {
                binding.pager.setCurrentItem(getInt(ARG_POSITION), false)
            }
        }

        val albumName = arguments?.getString(ARG_ALBUM_NAME)

        binding.pager.adapter = ImageAdapter(requireActivity(), albumName)
    }

    companion object {
        private const val ARG_POSITION = "position"
        private const val ARG_ALBUM_NAME = "album_name"

        @JvmStatic
        fun newInstance(position: Int, albumName: String? = null) =
            ImageViewPagerFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_POSITION, position)
                    putString(ARG_ALBUM_NAME, albumName)
                }
            }
    }
}