package com.vaibhav.socialsphere.register

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.vaibhav.socialsphere.MainActivity
import com.vaibhav.socialsphere.databinding.ActivityWelcomeBinding

class WelcomeActivity : AppCompatActivity() {
    private val binding: ActivityWelcomeBinding by lazy {
        ActivityWelcomeBinding.inflate(layoutInflater)
    }
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        binding.loginButton.setOnClickListener {
            val intent = Intent(this, SignInRegisterActivity::class.java)
            intent.putExtra("action", "login")
            startActivity(intent)
        }
        binding.registerButton.setOnClickListener {
            val intent = Intent(this, SignInRegisterActivity::class.java)
            intent.putExtra("action", "register")
            startActivity(intent)

        }
    }
    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null){
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }
    }
}