package com.snoy.save_img_example.ui.main

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import coil.drawable.CrossfadeDrawable
import coil.load
import com.snoy.save_img_example.R
import com.snoy.save_img_example.databinding.MainFragmentBinding
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
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
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        // TODO: Use the ViewModel

        binding.imgResult.visibility = View.GONE
        binding.rgImgSrc.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rb_res -> {
                    binding.image.loadViaCoil(R.drawable.fhd_img)
                }
                R.id.rb_assets -> {
                    //ref= https://github.com/coil-kt/coil/issues/10
                    binding.image.loadViaCoil(Uri.parse("file:///android_asset/fhd_asset_img.png"))
                }
                R.id.rb_internet -> {
                    binding.image.loadViaCoil(
                        "https://i.imgur.com/diABbG1.png"
                    )
                }
            }
        }

        binding.btnAppFolder.setOnClickListener {
            val fileName: String = getFileName(binding.rgImgSrc.checkedRadioButtonId)
            val bitmap = getBitmap(binding.image)
            val success = savoToAppFolder(bitmap, fileName)
            Log.d("RDTest", "savoToAppFolder ${if (success) "success" else "fail"}.")
            Toast.makeText(
                requireContext(),
                "savoToAppFolder ${if (success) "success" else "fail"}.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun getBitmap(imageView: ImageView): Bitmap? {
        val drawable = imageView.drawable
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        } else if (drawable is CrossfadeDrawable) {
            return if (drawable.end is BitmapDrawable) {
                (drawable.end as BitmapDrawable).bitmap
            } else null
        }
        return null
    }

    //ref= https://microeducate.tech/android-share-image-in-imageview-without-saving-in-sd-card/
    private fun savoToAppFolder(bitmap: Bitmap?, fileName: String): Boolean {
        if (bitmap == null) {
            return false
        }
        // save bitmap to cache directory
        try {
            //cacheDir = /data/app/app_folder/cache/
            //filesDir = /data/app/app_folder/files/
            //externalCacheDir = /sdcard/Android/data/app_folder/cache/
            //getExternalFilesDir(null) = /sdcard/Android/data/app_folder/files/

            val path = File(requireContext().externalCacheDir, "images")
            path.mkdirs() // don't forget to make the directory
            val stream =
                FileOutputStream(File(path, "$fileName.png")) // overwrites this image every time
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return false
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
        return true
    }

    private fun getFileName(checkedRadioButtonId: Int): String {
        return when (checkedRadioButtonId) {
            R.id.rb_internet -> {
                "img_from_internet"
            }
            R.id.rb_assets -> {
                "img_from_assets"
            }
            R.id.rb_res -> {
                "img_from_res"
            }
            else -> {
                ""
            }
        }
    }

    private fun ImageView.loadViaCoil(data: Any?) {
        this.load(data) {
            crossfade(1000)
            placeholder(binding.image.drawable)
            listener(onStart = {
                binding.imgResult.visibility = View.VISIBLE
            })
        }
    }
}