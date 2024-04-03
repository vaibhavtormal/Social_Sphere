package com.vaibhav.socialsphere

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import com.vaibhav.socialsphere.Model.BlogItemModel
import com.vaibhav.socialsphere.databinding.ActivityEditPostBinding

class EditPostActivity : AppCompatActivity() {
    private val binding: ActivityEditPostBinding by lazy {
        ActivityEditPostBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.backButton.setOnClickListener {
            finish()
        }
        val BlogItemModel = intent.getParcelableExtra<BlogItemModel>("blogItem")
        binding.postTitle.editText?.setText(BlogItemModel?.heading)
        binding.postDescription.editText?.setText(BlogItemModel?.userId)
        binding.addBlogButton.setOnClickListener {
            val updatedTitle = binding.postTitle.editText?.text.toString().trim()
            val updatedDescription = binding.postDescription.editText?.text.toString().trim()
            if (updatedTitle.isEmpty() || updatedDescription.isEmpty()) {
                Toast.makeText(this, "Please Fill the All Details", Toast.LENGTH_SHORT).show()
            } else {
                BlogItemModel?.heading = updatedTitle
                BlogItemModel?.post = updatedDescription
                if (BlogItemModel != null) {
                    updatedDataInFirebase(BlogItemModel)
                }
            }
        }
    }

    private fun updatedDataInFirebase(blogItemModel: BlogItemModel) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("Posts")
        val postId = blogItemModel.postId
        databaseReference.child(postId).setValue(blogItemModel)
            .addOnSuccessListener {
                Toast.makeText(this, "Post Update Successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Post Update UnSuccessfully", Toast.LENGTH_SHORT).show()
            }
    }
}