package com.example.yangstagram.navigation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.yangstagram.R
import com.example.yangstagram.navigation.model.AlarmDTO
import com.example.yangstagram.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CommentActivity : AppCompatActivity() {
    var contentUid: String? = null
    var destinationUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        contentUid = intent.getStringExtra("contentUid")
        destinationUid = intent.getStringExtra("destinationUid")

        findViewById<Button>(R.id.comment_btn_send).setOnClickListener {
            var comment = ContentDTO.Comment()
            comment.userId = FirebaseAuth.getInstance().currentUser?.email
            comment.uid = FirebaseAuth.getInstance().currentUser?.uid
            comment.comment = findViewById<EditText>(R.id.comment_edit_message).text.toString()
            comment.timestamp = System.currentTimeMillis()

            FirebaseFirestore.getInstance().collection("images").document(contentUid!!).collection("comments").document().set(comment)

            commentAlarm(destinationUid!!, findViewById<EditText>(R.id.comment_edit_message).text.toString())

            findViewById<EditText>(R.id.comment_edit_message).setText("")
        }

        findViewById<RecyclerView>(R.id.comment_recyclerview).apply {
            adapter = CommentRecyclerviewAdapter()
            layoutManager = LinearLayoutManager(this@CommentActivity)
        }
    }

    fun commentAlarm(destinationUid: String, message: String) {
        val alarmDTO = AlarmDTO()
        alarmDTO.destinationUid = destinationUid
        alarmDTO.userId = FirebaseAuth.getInstance().currentUser?.email
        alarmDTO.uid = FirebaseAuth.getInstance().currentUser?.uid
        alarmDTO.message = message
        alarmDTO.timestamp = System.currentTimeMillis()
        FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)
    }

    inner class CommentRecyclerviewAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var comments = ArrayList<ContentDTO.Comment>()

        init {
            FirebaseFirestore
                    .getInstance()
                    .collection("images")
                    .document(contentUid!!)
                    .collection("comments")
                    .orderBy("timestamp")
                    .addSnapshotListener { value, error ->
                        comments.clear()
                        if (value == null) return@addSnapshotListener

                        for (snapshot in value.documents) {
                            comments.add(snapshot.toObject(ContentDTO.Comment::class.java)!!)
                        }
                    }
        }

        private inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
            return CustomViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val view = holder.itemView
            view.findViewById<TextView>(R.id.commentviewitem_textview_comment).text = comments[position].comment
            view.findViewById<TextView>(R.id.commentviewitem_textview_profile).text = comments[position].userId

            FirebaseFirestore.getInstance()
                .collection("profileImages")
                .document(comments[position].uid!!)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val url = task.result!!["image"]
                        Glide.with(holder.itemView.context).load(url).apply(RequestOptions().circleCrop()).into(view.findViewById(R.id.commentviewitem_imageview_profile))
                    }
                }
        }

        override fun getItemCount(): Int = comments.size
    }
}