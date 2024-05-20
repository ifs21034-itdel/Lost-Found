package com.ifs21034.lostandfound.presentation.profile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ifs21034.lostandfound.R
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.ifs21034.lostandfound.data.remote.MyResult
import com.ifs21034.lostandfound.data.remote.response.DataUserResponse
import com.ifs21034.lostandfound.databinding.ActivityProfileBinding
import com.ifs21034.lostandfound.helper.Utils.Companion.observeOnce
import com.ifs21034.lostandfound.helper.getImageUri
import com.ifs21034.lostandfound.presentation.ViewModelFactory
import com.ifs21034.lostandfound.presentation.login.LoginActivity
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private var currentImageUri: Uri? = null
    private val viewModel by viewModels<ProfileViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupAction()
        observeSaveProfileImage()
    }

    private fun setupView(){
        showLoading(true)
        observeGetMe()
    }

    private fun setupAction(){
        binding.apply {
            ivProfileBack.setOnClickListener {
                finish()
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.pbProfile.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.llProfile.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun observeGetMe(){
        viewModel.getMe().observe(this){ result ->
            if (result != null) {
                when (result) {
                    is MyResult.Loading -> {
                        showLoading(true)
                    }

                    is MyResult.Success -> {
                        showLoading(false)
                        loadProfileData(result.data)
                    }

                    is MyResult.Error -> {
                        showLoading(false)
                        Toast.makeText(
                            applicationContext, result.error, Toast.LENGTH_LONG
                        ).show()
                        viewModel.logout()
                        openLoginActivity()
                    }
                }
            }
        }
    }

    private fun observeSaveProfileImage() {
        viewModel.saveProfileImageResult.observe(this) { result ->
            when (result) {
                is MyResult.Success -> {
                    Toast.makeText(applicationContext, result.data, Toast.LENGTH_SHORT).show()
                    observeGetMe()
                }
                is MyResult.Error -> {
                    Toast.makeText(applicationContext, result.error, Toast.LENGTH_SHORT).show()
                }
                is MyResult.Loading -> {

                }
            }
        }
    }

    private fun loadProfileData(profile: DataUserResponse){
        binding.apply {

            if(profile.user.photo != null){
                val urlImg = "https://public-api.delcom.org/${profile.user.photo}"
                Glide.with(this@ProfileActivity)
                    .load(urlImg)
                    .placeholder(R.drawable.ic_person)
                    .into(ivProfile)
            }
            tvProfileName.text = profile.user.name
            tvProfileEmail.text = profile.user.email

            btnCamera.setOnClickListener {
                startCamera()
            }
            btnGallery.setOnClickListener {
                startGallery()
            }

            btnSave.setOnClickListener {
                if (currentImageUri != null) {
                    val imageFile = currentImageUri?.let {uri ->
                        contentResolver.openInputStream(uri)?.use { inputStream ->
                            val imageRequestBody = inputStream.readBytes().toRequestBody("image/*".toMediaType())
                            MultipartBody.Part.createFormData("photo", "photo.jpg", imageRequestBody)
                        }
                    }
                    if (imageFile != null){
                        editPhoto(imageFile)
                    }
                } else {
                    AlertDialog.Builder(this@ProfileActivity).apply {
                        setTitle("Alert!")
                        setMessage("Please choose one picture!")
                        setPositiveButton("OK") {_, _-> }
                        create()
                        show()
                    }
                }
            }

            tvProfileName.text = profile.user.name
            tvProfileEmail.text = profile.user.email
        }
    }

    private fun editPhoto(cover: MultipartBody.Part) {
        viewModel.editPhoto(cover).observeOnce{ result->
            when (result) {
                is MyResult.Loading -> {
                    showLoading(true)
                }
                is MyResult.Success -> {
                    showLoading(false)
                    Toast.makeText(
                        applicationContext,
                        "Congrats! your profile has been updated!",
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(this@ProfileActivity,ProfileActivity::class.java))
                    finish()
                }
                is MyResult.Error -> {
                    // Handle error
                    showLoading(false)
                    AlertDialog.Builder(this@ProfileActivity).apply {
                        setTitle("Oh No!")
                        setMessage(result.error)
                        setPositiveButton("Oke") { _, _ -> }
                        create()
                        show()
                    }
                }
            }
        }
    }

    private fun startCamera() {
        currentImageUri = getImageUri(this)
        launcherIntentCamera.launch(currentImageUri)
    }
    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            showImage()
        }
    }

    private fun startGallery() {
        launcherGallery.launch(
            PickVisualMediaRequest(
                ActivityResultContracts.PickVisualMedia.ImageOnly
            )
        )
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
        } else {
            Toast.makeText(
                applicationContext,
                "Tidak ada media yang dipilih!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            binding.ivProfile.setImageURI(it)
        }
    }

    private fun openLoginActivity() {
        val intent = Intent(applicationContext, LoginActivity::class.java)
        intent.flags =
            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}
