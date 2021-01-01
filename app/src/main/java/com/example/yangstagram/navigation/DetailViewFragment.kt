package com.example.yangstagram.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.yangstagram.R
import com.example.yangstagram.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DetailViewFragment : Fragment() {
    lateinit var firestore: FirebaseFirestore
    lateinit var recyclerView: RecyclerView

    var uid: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = LayoutInflater.from(activity).inflate(R.layout.fragment_detail, container, false)

        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid
        recyclerView = view.findViewById(R.id.detailviewfragment_recyclerview)
        recyclerView.apply {
            adapter = DetailViewRecyclerViewAdapter()
            layoutManager = LinearLayoutManager(activity)
        }

        return view
    }

    inner class DetailViewRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        val contentDTOs: ArrayList<ContentDTO> = arrayListOf()
        val contentUidList: ArrayList<String> = arrayListOf()

        init {
            firestore.collection("images").orderBy("timestamp").addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                contentDTOs.clear()
                contentUidList.clear()
                for (snapshot in querySnapshot!!.documents) {
                    var item = snapshot.toObject(ContentDTO::class.java)
                    contentDTOs.add(item!!)
                    contentUidList.add(snapshot.id)
                }
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_detail, parent, false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val viewholder =  (holder as CustomViewHolder).itemView

            // UserId
            viewholder.findViewById<TextView>(R.id.detailviewitem_profile_textview).text = contentDTOs[position].userId

            // Image
            Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl).into(viewholder.findViewById(R.id.detailviewitem_imageview_content))

            // Explain of content
            viewholder.findViewById<TextView>(R.id.detailviewitem_explain_textview).text = contentDTOs[position].explain

            // likes
            viewholder.findViewById<TextView>(R.id.detailviewitem_favoritecounter_textview).text = "Likes ${contentDTOs[position].favoriteCount}"

            // ProfileImage
            Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl).into(viewholder.findViewById(R.id.detailviewitem_profile_image))

            // This code is when the button is clicked
            viewholder.findViewById<ImageView>(R.id.detailviewitem_favorite_imageview).setOnClickListener {
                favoriteEvent(position)
            }

            // This code is when the page is loaded
            if (contentDTOs[position].favorites.containsKey(uid)) {
                // This is like status
                viewholder.findViewById<ImageView>(R.id.detailviewitem_favorite_imageview).setImageResource(R.drawable.ic_favorite)
            } else {
                // This is unlike status
                viewholder.findViewById<ImageView>(R.id.detailviewitem_favorite_imageview).setImageResource(R.drawable.ic_favorite_border)
            }
        }

        override fun getItemCount(): Int = contentDTOs.size

        fun favoriteEvent(position: Int) {
            var tsDoc = firestore.collection("images").document(contentUidList[position])
            firestore.runTransaction { transaction ->
                var uid = FirebaseAuth.getInstance().currentUser?.uid
                var contentDTO = transaction.get(tsDoc).toObject(ContentDTO::class.java)

                if (contentDTO!!.favorites.containsKey(uid)) {
                    // When the button is clicked
                    contentDTO.favoriteCount = contentDTO.favoriteCount-1
                    contentDTO.favorites.remove(uid)
                } else {
                    // When the button is not clicked
                    contentDTO.favoriteCount = contentDTO.favoriteCount+1
                    contentDTO.favorites[uid!!] = true
                }
                transaction.set(tsDoc, contentDTO)
            }
        }
    }
}