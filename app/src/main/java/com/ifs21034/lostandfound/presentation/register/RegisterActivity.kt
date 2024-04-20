package com.ifs21034.lostandfound.presentation.register

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import com.ifs21034.lostandfound.data.remote.MyResult
import com.ifs21034.lostandfound.databinding.ActivityRegisterBinding
import com.ifs21034.lostandfound.presentation.ViewModelFactory
import com.ifs21034.lostandfound.presentation.login.LoginActivity

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val viewModel by viewModels<RegisterViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
        setupAction()
    }

    private fun setupView(){
        showLoading(false)
    }

    private fun setupAction(){
        binding.apply {

            // Memberikan aksis jika text ke tampilan login dipilih
            tvRegisterToLogin.setOnClickListener {
                openLoginActivity()
            }

            // Memberikan aksi jika tombol Register dipilih
            btnRegister.setOnClickListener {
                val name = etRegisterName.text.toString()
                val email = etRegisterEmail.text.toString()
                val password = etRegisterPassword.text.toString()

                if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    AlertDialog.Builder(this@RegisterActivity).apply {
                        setTitle("Oh No!")
                        setMessage("Tidak boleh ada data yang kosong!")
                        setPositiveButton("Oke") { _, _ -> }
                        create()
                        show()
                    }
                    return@setOnClickListener
                }

                observeRegister(name, email, password)
            }

        }
    }

    private fun observeRegister(name: String, email: String, password: String){
        viewModel.register(
            name,
            email,
            password
        ).observe(this) { result ->
            if (result != null) {
                when (result) {
                    is MyResult.Loading -> {
                        showLoading(true)
                    }

                    is MyResult.Success -> {
                        showLoading(false)

                        AlertDialog.Builder(this).apply {
                            setTitle("Yeah!")
                            setMessage(result.data.message)
                            setPositiveButton("Lanjut") { _, _ ->
                                openLoginActivity()
                            }
                            setCancelable(false)
                            create()
                            show()
                        }
                    }

                    is MyResult.Error -> {
                        showLoading(false)

                        AlertDialog.Builder(this).apply {
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
    }

    private fun showLoading(isLoading: Boolean) {
        binding.pbRegister.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnRegister.isActivated = !isLoading
        binding.btnRegister.text = if (isLoading) "" else "Register"
    }

    private fun openLoginActivity(){
        val intent = Intent(applicationContext, LoginActivity::class.java)
        intent.flags =
            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

}