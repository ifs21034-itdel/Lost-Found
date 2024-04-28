package com.ifs21034.lostandfound.presentation.lostfound

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.ifs21034.lostandfound.R
import com.ifs21034.lostandfound.data.local.entity.DelcomLostFoundEntity
import com.ifs21034.lostandfound.data.model.DelcomLostFound
import com.ifs21034.lostandfound.data.remote.MyResult
import com.ifs21034.lostandfound.data.remote.response.LostFoundResponse
import com.ifs21034.lostandfound.databinding.ActivityLostFoundDetailBinding
import com.ifs21034.lostandfound.helper.Utils.Companion.observeOnce
import com.ifs21034.lostandfound.presentation.ViewModelFactory

class LostFoundDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLostFoundDetailBinding
    private val viewModel by viewModels<LostFoundViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private var isChanged: Boolean = false
    private var isFavorite: Boolean = false
    private var delcomLostFound: DelcomLostFoundEntity? = null

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == LostFoundManageActivity.RESULT_CODE) {
            recreate()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLostFoundDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupAction()
    }

    private fun setupView() {
        showComponent(false)
        showLoading(false)
    }

    private fun setupAction() {
        val lostFoundId = intent.getIntExtra(KEY_LOST_FOUND_ID, 0)
        if (lostFoundId == 0) {
            finish()
            return
        }

        observeGetLostFound(lostFoundId)

        binding.appbarLostFoundDetail.setNavigationOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra(KEY_IS_CHANGED, isChanged)
            setResult(RESULT_CODE, resultIntent)
            finishAfterTransition()
        }
    }

    private fun observeGetLostFound(lostFoundId: Int) {
        viewModel.getLostFound(lostFoundId).observeOnce { result ->
            when (result) {
                is MyResult.Loading -> {
                    showLoading(true)
                }

                is MyResult.Success -> {
                    showLoading(false)
                    loadLostFound(result.data.data.lostFound)
                }

                is MyResult.Error -> {
                    Toast.makeText(
                        this@LostFoundDetailActivity,
                        result.error,
                        Toast.LENGTH_SHORT
                    ).show()
                    showLoading(false)
                    finishAfterTransition()
                }
            }
        }
    }

    private fun loadLostFound(lostFound: LostFoundResponse) {
        showComponent(true)

        binding.apply {
            tvLostFoundDetailTitle.text = lostFound.title
            tvLostFoundDetailDate.text = "Dibuat pada: ${lostFound.createdAt}"
            tvLostFoundDetailDesc.text = lostFound.description

            if(lostFound.cover != null){
                ivLostFoundDetailCover.visibility = View.VISIBLE

                Glide.with(this@LostFoundDetailActivity)
                    .load(lostFound.cover)
                    .placeholder(R.drawable.ic_image_24)
                    .into(ivLostFoundDetailCover)

            }else{
                ivLostFoundDetailCover.visibility = View.GONE
            }

            val status = if(lostFound.status.equals("found", ignoreCase = true)){
                highlightText("FOUND", Color.GREEN)
            } else {
                highlightText("LOST", Color.RED)
            }

            tvLostFoundDetailStatus.text = status

            viewModel.getLocalLostFound(lostFound.id).observeOnce {
                if(it != null){
                    delcomLostFound = it
                    setFavorite(true)
                }else{
                    setFavorite(false)
                }
            }

            cbLostFoundDetailIsFinished.isChecked = lostFound.isCompleted == 1

            cbLostFoundDetailIsFinished.setOnCheckedChangeListener { _, isChecked ->
                viewModel.putLostFound(
                    lostFound.id,
                    lostFound.title,
                    lostFound.description,
                    lostFound.status,
                    isChecked
                ).observeOnce {
                    when (it) {
                        is MyResult.Error -> {
                            if (isChecked) {
                                Toast.makeText(
                                    this@LostFoundDetailActivity,
                                    "Gagal menyelesaikan lost and found: " + lostFound.title,
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    this@LostFoundDetailActivity,
                                    "Gagal batal menyelesaikan lost and found: " + lostFound.title,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        is MyResult.Success -> {
                            if (isChecked) {
                                Toast.makeText(
                                    this@LostFoundDetailActivity,
                                    "Berhasil menyelesaikan lost and found: " + lostFound.title,
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    this@LostFoundDetailActivity,
                                    "Berhasil batal menyelesaikan lost and found: " + lostFound.title,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            if ((lostFound.isCompleted == 1) != isChecked) {
                                isChanged = true
                            }
                        }

                        else -> {}
                    }
                }
            }

            ivLostFoundDetailActionFavorite.setOnClickListener {
                if(isFavorite){
                    setFavorite(false)
                    if(delcomLostFound != null){
                        viewModel.deleteLocalLostFound(delcomLostFound!!)
                    }
                    Toast.makeText(
                        this@LostFoundDetailActivity,
                        "LostFound berhasil dihapus dari daftar favorite",
                        Toast.LENGTH_SHORT
                    ).show()
                }else{
                    delcomLostFound = DelcomLostFoundEntity(
                        id = lostFound.id,
                        title = lostFound.title,
                        description = lostFound.description,
                        isCompleted = lostFound.isCompleted,
                        cover = lostFound.cover,
                        createdAt = lostFound.createdAt,
                        updatedAt = lostFound.updatedAt,
                        status = "",
                        userId = 0
                    )

                    setFavorite(true)
                    viewModel.insertLocalLostFound(delcomLostFound!!)
                    Toast.makeText(
                        this@LostFoundDetailActivity,
                        "LostFound berhasil ditambahkan ke daftar favorite",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            ivLostFoundDetailActionDelete.setOnClickListener {
                val builder = AlertDialog.Builder(this@LostFoundDetailActivity)

                builder.setTitle("Konfirmasi Hapus Lost and Found")
                    .setMessage("Anda yakin ingin menghapus lost and found ini?")

                builder.setPositiveButton("Ya") { _, _ ->
                    observeDeleteLostFound(lostFound.id)
                }

                builder.setNegativeButton("Tidak") { dialog, _ ->
                    dialog.dismiss() // Menutup dialog
                }

                val dialog = builder.create()
                dialog.show()
            }

            ivLostFoundDetailActionEdit.setOnClickListener {
                val delcomLostFound = DelcomLostFound(
                    lostFound.id,
                    lostFound.title,
                    lostFound.description,
                    lostFound.status,
                    lostFound.isCompleted == 1,
                    lostFound.cover
                )

                val intent = Intent(
                    this@LostFoundDetailActivity,
                    LostFoundManageActivity::class.java
                )
                intent.putExtra(LostFoundManageActivity.KEY_IS_ADD, false)
                intent.putExtra(LostFoundManageActivity.KEY_LOST_FOUND, delcomLostFound)
                launcher.launch(intent)
            }
        }
    }

    private fun setFavorite(status: Boolean){
        isFavorite = status
        if(status){
            binding.ivLostFoundDetailActionFavorite
                .setImageResource(R.drawable.ic_favorite_24)
        }else{
            binding.ivLostFoundDetailActionFavorite
                .setImageResource(R.drawable.ic_favorite_border_24)
        }
    }

    private fun highlightText(text: String, color: Int): SpannableString {
        val spannableString = SpannableString(text)
        spannableString.setSpan(ForegroundColorSpan(color), 0, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannableString
    }

    private fun observeDeleteLostFound(lostFoundId: Int) {
        showComponent(false)
        showLoading(true)
        viewModel.deleteLostFound(lostFoundId).observeOnce {
            when (it) {
                is MyResult.Error -> {
                    showComponent(true)
                    showLoading(false)
                    Toast.makeText(
                        this@LostFoundDetailActivity,
                        "Gagal menghapus lost and found: ${it.error}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is MyResult.Success -> {
                    showLoading(false)

                    Toast.makeText(
                        this@LostFoundDetailActivity,
                        "Berhasil menghapus lost and found",
                        Toast.LENGTH_SHORT
                    ).show()

                    val resultIntent = Intent()
                    resultIntent.putExtra(KEY_IS_CHANGED, true)
                    setResult(RESULT_CODE, resultIntent)
                    finishAfterTransition()
                }

                else -> {}
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.pbLostFoundDetail.visibility =
            if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showComponent(status: Boolean) {
        binding.llLostFoundDetail.visibility =
            if (status) View.VISIBLE else View.GONE
    }

    companion object {
        const val KEY_LOST_FOUND_ID = "lost_found_id"
        const val KEY_IS_CHANGED = "is_changed"
        const val RESULT_CODE = 1001
    }
}