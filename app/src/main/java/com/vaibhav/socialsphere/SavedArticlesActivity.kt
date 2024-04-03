/*package com.vaibhav.socialsphere

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.vaibhav.socialsphere.Adapter.BlogAdapter
import com.vaibhav.socialsphere.Model.BlogItemModel
import com.vaibhav.socialsphere.databinding.ActivitySavedArticlesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class SavedArticlesActivity : AppCompatActivity() {
    private val binding:ActivitySavedArticlesBinding by lazy {
        ActivitySavedArticlesBinding.inflate(layoutInflater)
    }
    private val savedPostArticles = mutableListOf<BlogItemModel>()
    private lateinit var blogAdapter: BlogAdapter
    private val auth = FirebaseAuth.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //intialize blogAdapter
        blogAdapter = BlogAdapter(savedPostArticles.filter { it.isSaved }.toMutableList())
        val recyclerView = binding.savedArticlesRecyclerView
        recyclerView.adapter = blogAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        val userId = auth.currentUser?.uid
        if (userId != null){
            val userReference = FirebaseDatabase.getInstance()
                .getReference("users").child(userId).child("savePost")
            userReference.addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (postSnapshot in snapshot.children) {
                        val postId = postSnapshot.key
                        val isSaved = postSnapshot.value as Boolean
                        if (postId != null && isSaved){
                            //Featch the corresponding post  item on postId using a coroutine
                            CoroutineScope(Dispatchers.IO).launch {
                                val blogItem = featchBlogItem(postId)
                                if (blogItem != null){
                                    savedPostArticles.add(blogItem)
                                    launch(Dispatchers.Main){
                                        blogAdapter.updateData(savedPostArticles)
                                    }
                                }
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }
        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private suspend fun featchBlogItem(postId: String): BlogItemModel? {
        val blogReference = FirebaseDatabase.getInstance()
            .getReference("Posts")
        return try {
            val dataSnapshot = blogReference.child(postId).get().await()
            val blogData = dataSnapshot.getValue(BlogItemModel::class.java)
            blogData
        }catch(e :Exception){
            null
        }
    }
}*/

package com.vaibhav.socialsphere

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

import com.vaibhav.socialsphere.Adapter.BlogAdapter
import com.vaibhav.socialsphere.Model.BlogItemModel
import com.vaibhav.socialsphere.databinding.ActivitySavedArticlesBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class SavedArticlesActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySavedArticlesBinding
    private lateinit var blogAdapter: BlogAdapter
    private val savedPostArticles = mutableListOf<BlogItemModel>()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var userReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySavedArticlesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize RecyclerView and Adapter
        initializeRecyclerView()

        // Fetch saved articles
        fetchSavedArticles()
    }

    private fun initializeRecyclerView() {
        blogAdapter = BlogAdapter(savedPostArticles)
        val recyclerView = binding.savedArticlesRecyclerView
        recyclerView.adapter = blogAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun fetchSavedArticles() {
        val userId = auth.currentUser?.uid
        userId?.let { uid ->
            userReference = FirebaseDatabase.getInstance().getReference("users").child(uid)
                .child("savePost")

            userReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    savedPostArticles.clear() // Clear previous data

                    val fetchJobs = mutableListOf<Job>()
                    for (postSnapshot in snapshot.children) {
                        val postId = postSnapshot.key
                        val isSaved = postSnapshot.value as? Boolean ?: false

                        postId?.let {
                            if (isSaved) {
                                val fetchJob = CoroutineScope(Dispatchers.IO).launch {
                                    fetchBlogItem(postId)
                                }
                                fetchJobs.add(fetchJob)
                            }
                        }
                    }

                    CoroutineScope(Dispatchers.Main).launch {
                        fetchJobs.joinAll() // Wait for all fetch operations to complete
                        blogAdapter.notifyDataSetChanged() // Notify adapter after fetching all items
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle onCancelled if needed
                }
            })
        }
    }

    private suspend fun fetchBlogItem(postId: String) {
        try {
            val dataSnapshot =
                FirebaseDatabase.getInstance().getReference("Posts").child(postId).get().await()
            val blogData = dataSnapshot.getValue(BlogItemModel::class.java)
            blogData?.let {
                savedPostArticles.add(blogData)
            }
        } catch (e: Exception) {
            // Handle exception if needed
            e.printStackTrace()
        }
    }
}
