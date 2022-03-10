package me.rail.customgallery.screens.medialist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import me.rail.customgallery.R
import me.rail.customgallery.databinding.FragmentMediaListBinding
import me.rail.customgallery.main.Navigator
import me.rail.customgallery.media.MediaStorage
import me.rail.customgallery.screens.viewpager.ImageViewPagerFragment
import javax.inject.Inject

@AndroidEntryPoint
class MediaListFragment : Fragment() {
    private lateinit var binding: FragmentMediaListBinding

    @Inject
    lateinit var navigator: Navigator

    private var albumName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMediaListBinding.inflate(inflater, container, false)

        binding.root.isClickable = true

        binding.mediaList.layoutManager = GridLayoutManager(requireContext(), 3)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        albumName = arguments?.getString(ARG_ALBUM_NAME)

        val medias = if (albumName == null) {
            MediaStorage.getMedias()
        } else {
            MediaStorage.getMediasByAlbum(albumName!!)
        }

        binding.mediaList.adapter = MediaAdapter(
            medias,
            onImageClick = ::onImageClick,
        )
    }

    private fun onImageClick(position: Int) {
        navigator.replaceFragment(
            R.id.container,
            ImageViewPagerFragment.newInstance(position, albumName),
            true
        )
    }

    companion object {
        private const val ARG_ALBUM_NAME = "album_name"

        @JvmStatic
        fun newInstance(albumName: String) =
            MediaListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ALBUM_NAME, albumName)
                }
            }
    }
}