package me.rail.customgallery.screens.video

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import androidx.fragment.app.Fragment
import me.rail.customgallery.databinding.ItemVideoBinding

class VideoFragment: Fragment() {
    private lateinit var binding: ItemVideoBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ItemVideoBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments?.takeIf { it.containsKey(ARG_URI_STRING) }?.apply {
            val mediaController = MediaController(requireContext())
            mediaController.setAnchorView(binding.video)
            binding.video.setMediaController(mediaController)
            binding.video.setVideoURI(Uri.parse(getString(ARG_URI_STRING)))
            binding.video.requestFocus()
            binding.video.start()
        }
    }

    companion object {
        const val ARG_URI_STRING = "uri_string"

        @JvmStatic
        fun newInstance(uriString: String) =
            VideoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_URI_STRING, uriString)
                }
            }
    }
}