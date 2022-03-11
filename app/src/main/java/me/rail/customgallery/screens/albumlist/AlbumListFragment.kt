package me.rail.customgallery.screens.albumlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import me.rail.customgallery.R
import me.rail.customgallery.databinding.FragmentAlbumListBinding
import me.rail.customgallery.main.MainActivity
import me.rail.customgallery.main.Navigator
import me.rail.customgallery.media.MediaStorage
import me.rail.customgallery.screens.medialist.MediaListFragment
import javax.inject.Inject

@AndroidEntryPoint
class AlbumListFragment : Fragment() {
    private lateinit var binding: FragmentAlbumListBinding

    @Inject
    lateinit var navigator: Navigator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAlbumListBinding.inflate(inflater, container, false)

        binding.mediaList.layoutManager = GridLayoutManager(requireContext(), 2)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mediaList.adapter = AlbumAdapter(Glide.with(this),{
            (activity as MainActivity).showAlertSwitchToVideo()
        }, MediaStorage.getAlbums()) {
            navigator.replaceFragment(
                R.id.container,
                MediaListFragment.newInstance(it),
                true
            )
        }
    }
}