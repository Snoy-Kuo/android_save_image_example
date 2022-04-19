package com.snoy.save_img_example.ui.main

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.snoy.save_img_example.R
import com.snoy.save_img_example.databinding.MainFragmentBinding
import com.snoy.save_img_example.util.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.io.BufferedOutputStream
import java.io.File
import java.net.URL


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
                    binding.image.loadUsingCoil(R.drawable.fhd_img) {
                        binding.imgResult.visibility = View.VISIBLE
                    }
                }
                R.id.rb_assets -> {
                    //ref = https://github.com/coil-kt/coil/issues/10
                    binding.image.loadUsingCoil(Uri.parse("file:///android_asset/fhd_asset_img.png")) {
                        binding.imgResult.visibility = View.VISIBLE
                    }
                }
                R.id.rb_internet -> {
                    binding.image.loadUsingCoil("https://i.imgur.com/diABbG1.png") {
                        binding.imgResult.visibility = View.VISIBLE
                    }
                }
            }
        }

        binding.btnAppFolder.setOnClickListener { onClickSaveImg(it.id) }

        binding.btnDownload.setOnClickListener { onClickSaveImg(it.id) }

        binding.btnGallery.setOnClickListener { onClickSaveImg(it.id) }

        binding.btnShareIntentResolver.setOnClickListener {
            val fileName = "shareToAppIntentResolver"
            val bitmap = binding.image.getBitmap() ?: return@setOnClickListener
            val intent = bitmap.createSharingIntent(requireContext(), fileName)
            shareResult.launch(intent)
        }

        binding.btnShareSharesheet.setOnClickListener {
            val fileName = "shareToAppSharesheet"
            val bitmap = binding.image.getBitmap() ?: return@setOnClickListener
            val intent = bitmap.createChooserIntent(requireContext(), fileName)
            shareResult.launch(intent)
        }
    }

    private fun onClickSaveImg(id: Int) {
        val fileName: String = getFileName(binding.rgImgSrc.checkedRadioButtonId)
        val bitmap = binding.image.getBitmap()
        val result: Flow<Boolean?>
        val action: String
        when (id) {
            R.id.btn_app_folder -> {
                result = flowOf(null != bitmap.saveToAppFileFolder(requireContext(), fileName))
                action = "savoToAppFolder"
            }
            R.id.btn_download -> {
                result = saveToDownload(bitmap, fileName)
                action = "savoToDownload"
            }
            R.id.btn_gallery -> {
                result = saveToGallery(bitmap, fileName)
                action = "savoToGallery"
            }
            else -> {
                return
            }
        }
        lifecycleScope.launch {
            result.collect {
                it?.let {
                    Log.d("RDTest", "$action ${if (it) "success" else "fail"}.")
                    Toast.makeText(
                        requireContext(),
                        "$action ${if (it) "success" else "fail"}.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    //ref = https://stackoverflow.com/a/63812257
    private fun saveToGallery(bitmap: Bitmap?, fileName: String): Flow<Boolean?> {
        val result = MutableStateFlow<Boolean?>(null)
        if (bitmap == null) {
            result.value = false
            return result
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestWritePermission {
                    result.value = bitmap.saveFileToExternalStorageBeforeQ(
                        requireContext(),
                        fileName,
                        Environment.DIRECTORY_PICTURES
                    )
                    Log.d("RDTest", "saveFileToExternalStorageBeforeQ onPermissionGranted")
                }
                return result
            }
            result.value = bitmap.saveFileToExternalStorageBeforeQ(
                requireContext(),
                fileName,
                Environment.DIRECTORY_PICTURES
            )
            return result
        }
        val contentValues = ContentValues()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
            contentValues.put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES
            ) //or DIRECTORY_DCIM
        }
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        contentValues.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, fileName)
        contentValues.put(MediaStore.Images.ImageColumns.TITLE, fileName)
        try {
            val uri: Uri? = requireContext().contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            uri?.let {
                requireContext().contentResolver.openOutputStream(uri)?.let { stream ->
                    val oStream = BufferedOutputStream(stream)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, oStream)
                    oStream.close()
                    result.value = true
                    return result
                }
            }
        } catch (e: Exception) {
            Log.e("RDTest", "e= $e")
        }
        result.value = false
        return result
    }

    private fun saveToDownload(bitmap: Bitmap?, fileName: String): Flow<Boolean?> {
        val result = MutableStateFlow<Boolean?>(null)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val cachePath =
                    bitmap.saveToAppCacheFolder(requireContext(), fileName)
                        ?: return result.apply { value = false }
                val cachedImgFile = File(cachePath)
                val cachedImgUrl: URL = cachedImgFile.toURI().toURL()
                saveFileUsingMediaStore(requireContext(), cachedImgUrl, fileName)
                cachedImgFile.delete()
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestWritePermission {
                        result.value = bitmap.saveFileToExternalStorageBeforeQ(
                            requireContext(),
                            fileName,
                            Environment.DIRECTORY_DOWNLOADS
                        )
                        Log.d("RDTest", "saveFileToExternalStorageBeforeQ onPermissionGranted")
                    }
                    return result
                }
                result.value = bitmap.saveFileToExternalStorageBeforeQ(
                    requireContext(),
                    fileName,
                    Environment.DIRECTORY_DOWNLOADS
                )
                return result
            }
        } catch (e: Exception) {
            Log.e("RDTest", "e= $e")
            return result.apply { value = false }
        }
        return result.apply { value = true }
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

    // ref = https://stackoverflow.com/a/66552678
    private val reqLauncher =
        registerForActivityResult(RequestPermissionResultContract()) { result ->
            if (result) {
                Log.d("RDTest", "onActivityResult: PERMISSION GRANTED")
                //since we want dynamic callback, we set callback before reqLauncher.launch. And just print log here.
            } else {
                Toast.makeText(requireContext(), "Permission denied!", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    private fun requestWritePermission(onPermissionGranted: () -> Unit) {
        if (checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            (reqLauncher.contract as RequestPermissionResultContract).onPermissionGranted =
                onPermissionGranted
            reqLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            onPermissionGranted()
        }
    }

    private val shareResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // Optional - called as soon as the user selects an option from the system share dialog
            Log.d("RDTest", "shareResult callback triggered result=$it")
        }

    override fun onDestroy() {

        shareResult.unregister()
        super.onDestroy()
    }
}