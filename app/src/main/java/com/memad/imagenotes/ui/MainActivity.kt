package com.memad.imagenotes.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.memad.imagenotes.R
import com.memad.imagenotes.databinding.ActivityMainBinding
import com.memad.imagenotes.databinding.LoadingDialogBinding
import com.memad.imagenotes.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var mImageUri: Uri
    private lateinit var binding: ActivityMainBinding
    private lateinit var dialog: AlertDialog

    private lateinit var startForSelectImageResult: ActivityResultLauncher<Intent>

    @Inject
    lateinit var imageNoteAdapter: ImageNoteAdapter
    private val mainViewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        dialog = this.loadingDialog(LoadingDialogBinding.inflate(layoutInflater).root)
        initLauncher()
        emptyUi()
        setupObservables()
        setupRecyclerView()
        binding.include.saveButton.setOnClickListener {
            mainViewModel.addImageNote(
                getDataFromUi(),
                getImage()
            )
        }
        binding.include.addImageButton.setOnClickListener {
            requestPermission()
        }

        binding.swipeRefresh.setOnRefreshListener {
            mainViewModel.getAllImageNotes()
            binding.swipeRefresh.isRefreshing = true
        }
    }


    private fun initLauncher() {
        startForSelectImageResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                val resultCode = result.resultCode
                val data = result.data
                when (resultCode) {
                    RESULT_OK -> {
                        val fileUri = data?.data!!
                        mImageUri = fileUri
                        binding.include.imageHeader.setImageURI(fileUri)
                    }
                    ImagePicker.RESULT_ERROR -> {
                        Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun getDataFromUi(): HashMap<String, RequestBody> {
        val description = binding.include.descriptionEditText.text.toString()
        val title = binding.include.titleEditText.text.toString()
        val map = HashMap<String, RequestBody>()
        map["title"] = title.stringToRequestBody()
        map["content"] = description.stringToRequestBody()
        return map
    }

    private fun getImage(): MultipartBody.Part {
        contentResolver.openInputStream(mImageUri)
        Log.d("TAG", "getImage: $mImageUri ${mImageUri.path}")
        return File(mImageUri.getFilePath(this)).fileToMultiPart("image")
    }

    private fun setupRecyclerView() {
        imageNoteAdapter.setDeleteActionClickListener {
            mainViewModel.deleteImageNote(
                imageNoteAdapter.imageNotesList?.get(it)?.id.toString()
            )
        }
        binding.recyclerView.adapter = imageNoteAdapter
    }

    private fun setupObservables() {
        mainViewModel.addDeleteStates.observe(this@MainActivity, {
            when (it) {
                is Resource.Success -> {
                    mainViewModel.getAllImageNotes()
                    emptyUi()
                    Toast.makeText(this@MainActivity, it.data?.data, Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                is Resource.Error -> {
                    Toast.makeText(this@MainActivity, it.data?.data, Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                is Resource.Loading -> {
                    dialog.show()
                }
            }
        })

        lifecycleScope.launchWhenStarted {
            mainViewModel.imageNotes.collect {
                when (it) {
                    is Resource.Success -> {
                        imageNoteAdapter.imageNotesList = it.data?.toMutableList()
                        successUiState()
                        if (it.data?.size == 0) {
                            binding.recyclerView.visibility = View.GONE
                            binding.emptyText.visibility = View.VISIBLE
                        }
                    }
                    is Resource.Error -> {
                        Toast.makeText(
                            this@MainActivity,
                            it.error?.localizedMessage,
                            Toast.LENGTH_SHORT
                        ).show()
                        binding.swipeRefresh.isRefreshing = false
                    }
                    is Resource.Loading -> {
                        loadingUiState()
                    }
                }
            }

        }
    }

    private fun emptyUi() {
        binding.include.titleEditText.clearFocus()
        binding.include.titleEditText.text = null
        binding.include.descriptionEditText.clearFocus()
        binding.include.descriptionEditText.text = null
        binding.include.imageHeader.setImageResource(R.drawable.ic_image)
    }

    private fun loadingUiState() {
        binding.recyclerView.visibility = View.VISIBLE
        binding.emptyText.visibility = View.GONE
        binding.swipeRefresh.isRefreshing = true
    }

    private fun successUiState() {
        binding.swipeRefresh.isRefreshing = false
        binding.recyclerView.visibility = View.VISIBLE
        binding.emptyText.visibility = View.GONE
    }


    /******************************
     * ****************************
     * *******Permission Staff*****
     * ****************************
     * ****************************
     * */

    private var permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                pickImage()
            } else {
                unAvailableFeature()
            }

        }

    private fun pickImage() {
        ImagePicker.with(this)
            .compress(1024)
            .maxResultSize(1080, 1080)
            .createIntent { intent ->
                startForSelectImageResult.launch(intent)
            }
    }

    private fun requestPermission() {

        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED -> {
                pickImage()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) -> {
                permissionExplanation()
            }
            else -> {
                askStoragePermission()
            }

        }
    }

    private fun unAvailableFeature() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Unavailable Feature")
            .setMessage(
                "Uploading image to the Application isn't available. " +
                        "Pleas, grant us the permission so you can upload images!!"
            )
            .setPositiveButton("Ok") { _, _ ->
                askStoragePermission()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun permissionExplanation() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Permission needed")
            .setMessage(
                "This permission is needed to allow us help you use your images in our app."
            )
            .setPositiveButton("OK") { _, _ ->
                askStoragePermission()
            }
            .setNegativeButton("No thanks") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun askStoragePermission() {
        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

}


