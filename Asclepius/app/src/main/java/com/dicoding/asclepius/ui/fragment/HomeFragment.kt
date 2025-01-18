package com.dicoding.asclepius.ui.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.FragmentHomeBinding
import com.dicoding.asclepius.ui.activity.HistoryActivity
import com.dicoding.asclepius.ui.activity.ResultActivity
import com.dicoding.asclepius.ui.viewModel.MainViewModel
import com.dicoding.asclepius.ui.viewModel.MainViewModelFactory
import com.yalantis.ucrop.UCrop
import java.io.File
import java.util.Calendar

@Suppress("DEPRECATION")
class HomeFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory.getInstance(requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        observeViewModel()
        setupActionBar()
    }

    private fun setupActionBar() {
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            title = getString(R.string.title_home)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_item, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.history_item -> {
                startActivity(Intent(requireContext(), HistoryActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupUI() {
        binding.apply {
            galleryButton.setOnClickListener(this@HomeFragment)
            analyzeButton.setOnClickListener(this@HomeFragment)
        }
    }

    private fun observeViewModel() {
        viewModel.croppedImageUri.observe(viewLifecycleOwner) { uri ->
            uri?.let {
                binding.previewImageView.setImageURI(it)
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.galleryButton -> startGallery()
            R.id.analyzeButton -> viewModel.croppedImageUri.value?.let {
                analyzeImage(it)
            } ?: showToast(getString(R.string.empty_image_warning))
        }
    }

    private fun analyzeImage(uri: Uri) {
        val intent = Intent(requireContext(), ResultActivity::class.java)
        intent.putExtra(ResultActivity.EXTRA_IMAGE_URI, uri.toString())
        startActivity(intent)
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            startCrop(uri)
        } else {
            Log.d("Photo Picker", "No media selected, keeping the current image")
        }
    }

    private val cropImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {result ->
        if (result.resultCode == RESULT_OK) {
            val resultUri = UCrop.getOutput(result.data!!)
           if (resultUri != null){
               viewModel.setCroppedImageUri(resultUri)
           }else {
               showToast("Failed to crop image")
           }
        } else {
            Log.d("Crop Activity", "Crop canceled, keeping the current image")
        }
    }

    private fun startCrop(imageUri: Uri) {
        val currentDate = Calendar.getInstance().time
        val destinationUri = Uri.fromFile(File(requireContext().cacheDir, "UCropImage $currentDate"))
        val cropIntent = UCrop.of(imageUri, destinationUri)
            .withAspectRatio(1f, 1f)
            .getIntent(requireContext())
        cropImageLauncher.launch(cropIntent)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
