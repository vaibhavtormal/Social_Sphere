package com.vaibhav.socialsphere.register

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.vaibhav.socialsphere.MainActivity
import com.vaibhav.socialsphere.UserData
import com.vaibhav.socialsphere.databinding.ActivitySignInRegisterBinding

class SignInRegisterActivity : AppCompatActivity() {
    private val binding: ActivitySignInRegisterBinding by lazy {
        ActivitySignInRegisterBinding.inflate(layoutInflater)
    }
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.backButton.setOnClickListener {
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        }
        //intialize Firebase  Authentication
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        // for visibility of buttons
        val action = intent.getStringExtra("action")
        //aadjust visibility for login
        if (action == "login") {
            binding.loginButton.visibility = View.VISIBLE
            binding.loginEmailAddress.visibility = View.VISIBLE
            binding.loginPassword.visibility = View.VISIBLE

            binding.registerButton.isEnabled = false
            binding.registerButton.alpha = 0.5f
            binding.registerNewHere.isEnabled = false
            binding.registerNewHere.alpha = 0.5f
            binding.registerName.visibility = View.GONE
            binding.registerPassword.visibility = View.GONE
            binding.registerEmail.visibility = View.GONE
            binding.cardView.visibility = View.GONE

            binding.loginButton.setOnClickListener {
                val loginEmail = binding.loginEmailAddress.text.toString()
                val loginPassword = binding.loginPassword.text.toString()
                if (loginEmail.isEmpty() || loginPassword.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Please Fill All the Details\uD83D\uDE43",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    auth.signInWithEmailAndPassword(loginEmail, loginPassword)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    this,
                                    "Login Successfully\uD83D\uDE0A",
                                    Toast.LENGTH_SHORT
                                ).show()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            } else {
                                Toast.makeText(
                                    this,
                                    "Login Failed, Please Enter Correct Details\uFE0F",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }

            }


        } else if (action == "register") {
            binding.loginButton.isEnabled = false
            binding.loginButton.alpha = 0.5f

            binding.registerButton.setOnClickListener {
                //get data from edit text
                val registerName = binding.registerName.text.toString()
                val registerEmail = binding.registerEmail.text.toString()
                val registerPassword = binding.registerPassword.text.toString()
                if (registerName.isEmpty() || registerEmail.isEmpty() || registerPassword.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Please Fill All the Details\uD83D\uDE43",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    auth.createUserWithEmailAndPassword(registerEmail, registerPassword)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {

                                val user = auth.currentUser
                                auth.signOut()
                                user?.let {
                                    //save userdata in to firebase database
                                    val userReference = database.getReference("users")
                                    val userId = user.uid
                                    val userData = UserData(
                                        registerName,
                                        registerEmail,
                                        registerPassword,
                                    )
                                    userReference.child(userId).setValue(userData)
                                    //upload image of user stored in the Firebase Storage
                                    val storageReference =
                                        storage.reference.child("profile_image/$userId.jpg")
                                    storageReference.putFile(imageUri!!)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                storageReference.downloadUrl.addOnCompleteListener { imageUri ->
                                                    if (imageUri.isSuccessful){
                                                        val imageUrl = imageUri.result.toString()
                                                        //save the image url to the firebase database
                                                        userReference.child(userId)
                                                            .child("profileImage")
                                                            .setValue(imageUrl)
                                                       /* Glide.with(this)
                                                            .load(imageUri)
                                                            .apply { RequestOptions.centerCropTransform() }
                                                            .into(binding.registerUserImage)*/
                                                    }
                                                }
                                            }
                                        }
                                    Toast.makeText(
                                        this,
                                        "Sign In Successfully\uD83D\uDE0A",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    startActivity(Intent(this, WelcomeActivity::class.java))
                                    finish()
                                }
                            } else {
                                Toast.makeText(
                                    this,
                                    "Sign In Failed\uD83D\uDE43",
                                    Toast.LENGTH_SHORT
                                ).show()

                            }
                        }
                }
            }
        }
        //set On ClickListner for the Choose image
        binding.cardView.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT

            startActivityForResult(
                Intent.createChooser(intent, "Select Image"),
                PICK_IMAGE_REQUEST
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null)
            imageUri = data.data
        Glide.with(this)
            .load(imageUri)
            .apply { RequestOptions.centerCropTransform() }
            .into(binding.registerUserImage)
    }
}