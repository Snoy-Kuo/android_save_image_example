package com.snoy.save_img_example.ui.main

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import coil.load
import com.snoy.save_img_example.R
import com.snoy.save_img_example.databinding.MainFragmentBinding
import java.io.IOException


class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel
    private var _binding: MainFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MainFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rgImgSrc.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_res -> {
                    //coil way
                    binding.image.load(R.drawable.fhd_img) {
                        crossfade(1000)
                        placeholder(binding.image.drawable)
                    }
                    //original way
//                    binding.image.setImageResource(R.drawable.fhd_img)
                }
                R.id.rb_assets -> {
                    binding.image.loadImgFromAssets("fhd_asset_img.png")
                }
                R.id.rb_internet -> {
                    //coil
                    binding.image.load(
                        "https://i.imgur.com/diABbG1.png"
                    ) {
                        crossfade(1000)
                        placeholder(binding.image.drawable)
                    }
                }
            }
        }

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        // TODO: Use the ViewModel
    }

    //ref = https://stackoverflow.com/a/7645606
    private fun ImageView.loadImgFromAssets(fileName: String) {
        // load image
        try {
            //coil way //ref= https://github.com/coil-kt/coil/issues/10
            this.load(Uri.parse("file:///android_asset/$fileName")) {
                crossfade(1000)
                placeholder(binding.image.drawable)
            }

            //original way
//            // get input stream
//            val ims: InputStream = requireContext().assets.open(fileName)
//            // load image as Drawable
//            val d = Drawable.createFromStream(ims, null)
//            // set image to ImageView
//            this.setImageDrawable(d)
        } catch (e: IOException) {
            Log.e("RDTest", "e= $e")
            return
        }
    }
}