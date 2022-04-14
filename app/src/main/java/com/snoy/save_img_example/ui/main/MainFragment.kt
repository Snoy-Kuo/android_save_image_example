package com.snoy.save_img_example.ui.main

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import coil.drawable.CrossfadeDrawable
import coil.load
import com.snoy.save_img_example.R
import com.snoy.save_img_example.databinding.MainFragmentBinding
import com.snoy.save_img_example.util.RequestPermissionResultContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.io.*
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
            onClickSaveImg(it.id)
        }

        binding.btnDownload.setOnClickListener {
            onClickSaveImg(it.id)
        }

        binding.btnGallery.setOnClickListener {
            onClickSaveImg(it.id)
        }
    }

    private fun onClickSaveImg(id: Int) {
        val fileName: String = getFileName(binding.rgImgSrc.checkedRadioButtonId)
        val bitmap = getBitmap(binding.image)
        val result: Flow<Boolean?>
        val action: String
        when (id) {
            R.id.btn_app_folder -> {
                result = flowOf(null != saveToAppFileFolder(bitmap, fileName))
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
    private fun saveToAppFolder(bitmap: Bitmap?, parentDir: File, fileName: String): String? {
        if (bitmap == null) {
            return null
        }
        // save bitmap to directory
        try {
            //cacheDir = /data/app/app_folder/cache/
            //filesDir = /data/app/app_folder/files/
            //externalCacheDir = /sdcard/Android/data/app_folder/cache/
            //getExternalFilesDir(null) = /sdcard/Android/data/app_folder/files/

            val path = File(parentDir, "images")
            path.mkdirs() // don't forget to make the directory
            val file = File(path, "$fileName.png")
            val stream = FileOutputStream(file) // overwrites this image every time
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()
            return file.absolutePath
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return null
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    private fun saveToAppFileFolder(bitmap: Bitmap?, fileName: String): String? {
        return saveToAppFolder(bitmap, requireContext().getExternalFilesDir(null)!!, fileName)
    }

    private fun saveToAppCacheFolder(bitmap: Bitmap?, fileName: String): String? {
        return saveToAppFolder(bitmap, requireContext().externalCacheDir!!, fileName)
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
                    result.value = saveFileToExternalStorageBeforeQ(
                        bitmap,
                        fileName,
                        Environment.DIRECTORY_PICTURES
                    )
                    Log.d("RDTest", "saveFileToExternalStorageBeforeQ onPermissionGranted")
                }
                return result
            }
            result.value = saveFileToExternalStorageBeforeQ(
                bitmap,
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
                    saveToAppCacheFolder(bitmap, fileName) ?: return result.apply { value = false }
                val cachedImgFile = File(cachePath)
                val cachedImgUrl: URL = cachedImgFile.toURI().toURL()
                saveFileUsingMediaStore(requireContext(), cachedImgUrl, fileName)
                cachedImgFile.delete()
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestWritePermission {
                        result.value = saveFileToExternalStorageBeforeQ(
                            bitmap,
                            fileName,
                            Environment.DIRECTORY_DOWNLOADS
                        )
                        Log.d("RDTest", "saveFileToExternalStorageBeforeQ onPermissionGranted")
                    }
                    return result
                }
                result.value = saveFileToExternalStorageBeforeQ(
                    bitmap,
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

    //ref = https://medium.com/@thuat26/how-to-save-file-to-external-storage-in-android-10-and-above-a644f9293df2
    private fun saveFileToExternalStorageBeforeQ(
        bitmap: Bitmap?,
        fileName: String,
        type: String
    ): Boolean {
        try {
            val cachePath = saveToAppCacheFolder(bitmap, fileName) ?: return false
            val cachedImgFile = File(cachePath)
            val cachedImgUrl: URL = cachedImgFile.toURI().toURL()
            saveFileToExternalStorageBeforeQ(cachedImgUrl, "$fileName.png", type)
            cachedImgFile.delete()
        } catch (e: Exception) {
            Log.e("RDTest", "e= $e")
            return false
        }
        return true
    }

    private fun saveFileToExternalStorageBeforeQ(url: URL, fileName: String, type: String) {
        @Suppress("DEPRECATION") //for target < Q
        val target = File(
            Environment.getExternalStoragePublicDirectory(type),
            fileName
        )
        url.openStream().use { input ->
            FileOutputStream(target).use { output ->
                input.copyTo(output)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveFileUsingMediaStore(context: Context, url: URL, fileName: String) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS) //Q
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues) //Q
        if (uri != null) {
            url.openStream().use { input ->
                resolver.openOutputStream(uri).use { output ->
                    input.copyTo(output!!, DEFAULT_BUFFER_SIZE)
                }
            }
        }
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

    //ref = https://ithelp.ithome.com.tw/articles/10205635
    // https://stackoverflow.com/a/66552678
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

}