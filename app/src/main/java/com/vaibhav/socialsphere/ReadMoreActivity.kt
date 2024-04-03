/*package com.vaibhav.socialsphere

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.vaibhav.socialsphere.Model.BlogItemModel
import com.vaibhav.socialsphere.databinding.ActivityReadMoreBinding

class ReadMoreActivity : AppCompatActivity() {
    private val binding: ActivityReadMoreBinding by lazy {
        ActivityReadMoreBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            finish()
        }

        val blogs = intent.getParcelableExtra<BlogItemModel>("postItems")
        if (blogs != null) {
            //Retrive user related data here title and all that
            binding.titleText.text = blogs.heading
            binding.userName.text = blogs.userName
            binding.date.text = blogs.date
            binding.blogDescription.text = blogs.post
            val userImageUrl = blogs.profileImage
            Glide.with(this)
                .load(userImageUrl)
                .apply (RequestOptions.centerCropTransform())
                .into(binding.profileImage)
        } else {
            Toast.makeText(this, "Failed to load Post", Toast.LENGTH_SHORT).show()
        }
    }
}*/

/*
package com.vaibhav.socialsphere

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.bumptech.glide.Glide
import com.vaibhav.socialsphere.Model.BlogItemModel
import com.vaibhav.socialsphere.databinding.ActivityReadMoreBinding

class ReadMoreActivity : AppCompatActivity() {
    private val binding: ActivityReadMoreBinding by lazy {
        ActivityReadMoreBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.backButton.setOnClickListener {
            finish()
        }

        val blogs = intent.getParcelableExtra<BlogItemModel>("postItems")
        if ( blogs != null){
            //Retrive user related data here title and all that
            binding.titleText.text = blogs.heading
            binding.userName.text = blogs.userName
            binding.date.text = blogs.date
            binding.blogDescription.text = blogs.userId //post

           }
        else{
            Toast.makeText(this, "Failed to load Post", Toast.LENGTH_SHORT).show()
        }
    }
}*/

package com.vaibhav.socialsphere

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.bumptech.glide.Glide
import com.vaibhav.socialsphere.Model.BlogItemModel
import com.vaibhav.socialsphere.databinding.ActivityReadMoreBinding

class ReadMoreActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReadMoreBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadMoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            finish()
        }

        val blogItem = intent.getParcelableExtra<BlogItemModel>("postItems")
        if (blogItem != null) {
            // Load image using Glide
            Glide.with(this)
                .load(blogItem.profileImage)
                .into(binding.profileImage)

            // Retrieve other data
            binding.titleText.text = blogItem.heading
            binding.userName.text = blogItem.userName
            binding.date.text = blogItem.date
            binding.blogDescription.text = blogItem.userId // Assuming userId is the description
        } else {
            Toast.makeText(this, "Failed to load Post", Toast.LENGTH_SHORT).show()
        }
    }
}
