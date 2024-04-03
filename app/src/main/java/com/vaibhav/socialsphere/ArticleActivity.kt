package com.vaibhav.socialsphere
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.vaibhav.socialsphere.Adapter.ArticleAdapter
import com.vaibhav.socialsphere.Model.BlogItemModel
import com.vaibhav.socialsphere.databinding.ActivityArticleBinding

class ArticleActivity : AppCompatActivity() {
    private val binding:ActivityArticleBinding by lazy {
        ActivityArticleBinding.inflate(layoutInflater)
    }
    private lateinit var databaseReference: DatabaseReference
    private val auth = FirebaseAuth.getInstance()
    private lateinit var blogAdapter:ArticleAdapter
    private val EDIT_BLOG_REQUEST_CODE = 123
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.backButton.setOnClickListener {
            finish()
        }
        val currentUserId = auth.currentUser?.uid
        val recyclerView = binding.articleRecylerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        if (currentUserId != null){
            blogAdapter = ArticleAdapter(this, emptyList(),object :ArticleAdapter.OnItemClickListener{
                override fun onEditClick(blogItem: BlogItemModel) {
                    val intent = Intent(this@ArticleActivity,EditPostActivity::class.java)
                    intent.putExtra("blogItem",blogItem)
                    startActivityForResult(intent,EDIT_BLOG_REQUEST_CODE)
                }
                override fun onReadMre(blogItem: BlogItemModel) {
                    val intent = Intent(this@ArticleActivity,ReadMoreActivity::class.java)
                    intent.putExtra("blogItem",blogItem)
                    startActivity(intent)
                }
                override fun onDelete(blogItem: BlogItemModel) {
                    deletePost(blogItem)
                }
            })
        }
        recyclerView.adapter = blogAdapter
        //get save post data form database
        databaseReference= FirebaseDatabase.getInstance().getReference("Posts")
        databaseReference.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val blogsavedList = ArrayList<BlogItemModel>()
                for (postSnapShot in snapshot.children) {
                    val blogSaved = postSnapShot.getValue(BlogItemModel::class.java)
                    if (blogSaved != null && currentUserId == blogSaved.userId){           //userid replcae with post
                        blogsavedList.add(blogSaved)
                    }
                }
                blogAdapter.setData(blogsavedList)
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ArticleActivity, " Saved Post Loading Error", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun deletePost(blogItem: BlogItemModel) {
        val postId = blogItem.postId
        val blogPostReference = databaseReference.child(postId)
        //remove the post
        blogPostReference.removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "post delete", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(this, "post delete failed", Toast.LENGTH_SHORT).show()
            }
    }
   override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
       super.onActivityResult(requestCode, resultCode, data)
       if (requestCode == EDIT_BLOG_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
           // Handle the result of the edit blog activity here
       }
   }
}