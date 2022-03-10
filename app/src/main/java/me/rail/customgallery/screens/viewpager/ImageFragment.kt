package me.rail.customgallery.screens.viewpager

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import coil.load
import me.rail.customgallery.databinding.ItemImageBinding

class ImageFragment: Fragment() {
    private lateinit var binding: ItemImageBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ItemImageBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments?.takeIf { it.containsKey(ARG_URI_STRING) }?.apply {
            binding.image.load(Uri.parse(getString(ARG_URI_STRING)))
        }
    }

    companion object {
        const val ARG_URI_STRING = "uri_string"

        @JvmStatic
        fun newInstance(uriString: String) =
            ImageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_URI_STRING, uriString)
                }
            }
    }
}