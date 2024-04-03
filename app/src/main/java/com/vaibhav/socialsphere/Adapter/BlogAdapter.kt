package com.vaibhav.socialsphere.Adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.vaibhav.socialsphere.Model.BlogItemModel
import com.vaibhav.socialsphere.R
import com.vaibhav.socialsphere.ReadMoreActivity
import com.vaibhav.socialsphere.databinding.BlogItemBinding

class BlogAdapter(private val items: MutableList<BlogItemModel>) :
    RecyclerView.Adapter<BlogAdapter.BlogViewHolder>() {

    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val currentUser = FirebaseAuth.getInstance().currentUser
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlogViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = BlogItemBinding.inflate(inflater, parent, false)
        return BlogViewHolder(binding)
    }


    override fun onBindViewHolder(holder: BlogViewHolder, position: Int) {
        val blogItems = items[position]
        holder.bind(blogItems)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class BlogViewHolder(private val binding: BlogItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(blogItemModel: BlogItemModel) {
            val postId = blogItemModel.postId
            val context = binding.root.context
            binding.heading.text = blogItemModel.heading
            Glide.with(binding.profile.context)
                .load(blogItemModel.profileImage)
                .into(binding.profile)
            binding.userName.text = blogItemModel.userName
            binding.date.text = blogItemModel.date
            binding.post.text = blogItemModel.post
            binding.likeCount.text = blogItemModel.likeCount.toString()

            //set on click listner
            binding.root.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, ReadMoreActivity::class.java)
                intent.putExtra("postItems", blogItemModel)
                context.startActivity(intent)
            }


            //Check if the current user has liked the post and update the like button
            val postlikeReference: DatabaseReference =
                databaseReference.child("Posts").child(postId).child("likes")
            val currentUserLiked = currentUser?.uid?.let { uid ->
                postlikeReference.child(uid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                binding.likeButton.setImageResource(R.drawable.like_red)
                            } else {
                                binding.likeButton.setImageResource(R.drawable.like_black)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }

                    })
            }
            //handle like button clicks
            binding.likeButton.setOnClickListener {
                if (currentUser != null) {
                    handleLikedButtonCliced(postId, blogItemModel, binding)
                } else {
                    Toast.makeText(context, "You have to Login First", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            //set the initial icon based on the saved status
            val userReference = databaseReference.child("users").child(currentUser?.uid?:"")
            val postSaveReference = userReference.child("savePost").child(postId)
            postSaveReference.addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        //if post already saved
                        binding.postSaveButton.setImageResource(R.drawable.save_red_2)
                    }else{
                        //if post not saved yet
                        binding.postSaveButton.setImageResource(R.drawable.save_red)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
            //handle save button clicks
            binding.postSaveButton.setOnClickListener {
                if (currentUser != null) {
                    handleSaveButtonCliced(postId, blogItemModel, binding)
                } else {
                    Toast.makeText(context, "You have to Login First", Toast.LENGTH_SHORT)

                }
            }

        }

        private fun handleLikedButtonCliced(
            postId: String,
            blogItemModel: BlogItemModel,
            binding: BlogItemBinding
        ) {
            val userReference: DatabaseReference =
                databaseReference.child("users").child(currentUser!!.uid)
            val postLikeReference: DatabaseReference =
                databaseReference.child("Posts").child(postId).child("likes")

            //check has already liked the post, so unlike the post
            postLikeReference.child(currentUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            userReference.child("likes").child(postId).removeValue()
                                .addOnSuccessListener {
                                    postLikeReference.child(currentUser.uid).removeValue()
                                    blogItemModel.likedBy?.remove(currentUser.uid)
                                    updateLikebuttonImage(binding, false)
                                    //decrement the like in the  database
                                    val newLikeCount = blogItemModel.likeCount - 1
                                    blogItemModel.likeCount = newLikeCount
                                    databaseReference.child("Posts").child(postId)
                                        .child("likeCount")
                                        .setValue(newLikeCount)
                                    notifyDataSetChanged()
                                }
                                .addOnFailureListener { e ->
                                    Log.e("LikedClicked", "onDataChange: Failed to unlike post$e")
                                }
                        } else {
                            //User has not like the post, so like it
                            userReference.child("likes").child(postId).setValue(true)
                                .addOnSuccessListener {
                                    postLikeReference.child(currentUser.uid).setValue(true)
                                    blogItemModel.likedBy?.add(currentUser.uid)
                                    updateLikebuttonImage(binding, true)
                                    //Increment fo like count in data base
                                    val newLikeCount: Int = blogItemModel.likeCount + 1
                                    blogItemModel.likeCount = newLikeCount

                                    databaseReference.child("Posts").child(postId)
                                        .child("likeCount")
                                        .setValue(newLikeCount)
                                           notifyDataSetChanged()
                                }
                                .addOnFailureListener { e ->
                                    Log.e("LikedClicked", "onDataChange: Failed to unlike post$e")
                                }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })

        }

        private fun updateLikebuttonImage(binding: BlogItemBinding, liked: Boolean) {
            if (liked) {
                binding.likeButton.setImageResource(R.drawable.like_black)
            } else {
                binding.likeButton.setImageResource(R.drawable.like_red)
            }
        }
    }

    private fun handleSaveButtonCliced(
        postId: String,
        blogItemModel: BlogItemModel,
        binding: BlogItemBinding
    ) {
        val userReference = databaseReference.child("users").child(currentUser!!.uid)
        userReference.child("savePost").child(postId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        //the blog is currently saved , so unsaved it
                        userReference.child("savePost").child(postId).removeValue()
                            .addOnSuccessListener {
                                //update the ui

                                val clickedPostItem = items.find { it.postId == postId }
                                clickedPostItem?.isSaved = false
                                notifyDataSetChanged()

                                val context = binding.root.context
                                Toast.makeText(context, "Post Unsaved...!", Toast.LENGTH_SHORT)
                                    .show()
                            }.addOnFailureListener {
                                val context = binding.root.context
                                Toast.makeText(
                                    context,
                                    "Failed to Unsaved the post",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        binding.postSaveButton.setImageResource(R.drawable.save_red)
                    } else {
                            // the post is not save so save it
                        userReference.child("savePost").child(postId).setValue(true)
                            .addOnSuccessListener {
                                //update the ui
                                val clickedPostItem = items.find { it.postId ==postId }
                                clickedPostItem?.isSaved = true


                                val context = binding.root.context
                                Toast.makeText(context, "Post Saved...!", Toast.LENGTH_SHORT)
                                    .show()
                            }.addOnFailureListener {
                                val context = binding.root.context
                                Toast.makeText(context, "Failed to save the Post", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        //change the save ui button
                        binding.postSaveButton.setImageResource(R.drawable.save_red_2)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    fun updateData(savedPostArticles:List<BlogItemModel>) {
        items.clear()
        items.addAll(savedPostArticles)
        notifyDataSetChanged()
    }
}