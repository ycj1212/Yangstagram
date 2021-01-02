package com.example.yangstagram.navigation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.yangstagram.LoginActivity
import com.example.yangstagram.MainActivity
import com.example.yangstagram.R
import com.example.yangstagram.navigation.model.ContentDTO
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserFragment : Fragment() {
    lateinit var fragmentView: View
    lateinit var firestore: FirebaseFirestore
    lateinit var auth: FirebaseAuth
    var uid: String? = null
    var currentUserUid: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentView = LayoutInflater.from(activity).inflate(R.layout.fragment_user, container, false)

        uid = arguments?.getString("destinationUid")
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUserUid = auth.currentUser?.uid

        if (uid == currentUserUid) {
            // MyPage
            fragmentView.findViewById<Button>(R.id.account_btn_follow_signout).text = getString(R.string.signout)
            fragmentView.findViewById<Button>(R.id.account_btn_follow_signout).setOnClickListener {
                activity?.finish()
                startActivity(Intent(activity, LoginActivity::class.java))
                auth.signOut()
            }
        } else {
            // OtherUserPage
            fragmentView.findViewById<Button>(R.id.account_btn_follow_signout).text = getString(R.string.follow)
            var mainactivity = (activity as MainActivity)
            mainactivity.findViewById<TextView>(R.id.toolbar_username).text = arguments?.getString("userId")
            mainactivity.findViewById<Button>(R.id.toolbar_btn_back).setOnClickListener {
                mainactivity.findViewById<BottomNavigationView>(R.id.bottom_navigation).selectedItemId = R.id.action_home
            }
            mainactivity.findViewById<ImageView>(R.id.toolbar_title_image).visibility = View.GONE
            mainactivity.findViewById<TextView>(R.id.toolbar_username).visibility = View.VISIBLE
            mainactivity.findViewById<Button>(R.id.toolbar_btn_back).visibility = View.VISIBLE
        }

        val recyclerView = fragmentView.findViewById<RecyclerView>(R.id.account_recyclerview).apply {
            layoutManager = GridLayoutManager(activity, 3)
            adapter = UserFragmentRecyclerViewAdapter()
        }

        return fragmentView
    }

    inner class UserFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        val contentDTOs: ArrayList<ContentDTO> = arrayListOf()

        init {
            firestore.collection("images").whereEqualTo("uid", uid).addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                // Sometimes, This code return null of querySnapshot when it signout
                if (querySnapshot == null) return@addSnapshotListener

                // Get data
                for (snapshot in querySnapshot.documents) {
                    contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                }

                fragmentView.findViewById<TextView>(R.id.account_tv_post_count).text = contentDTOs.size.toString()
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val width = resources.displayMetrics.widthPixels / 3
            val imageview = ImageView(parent.context)
            
            imageview.layoutParams = LinearLayoutCompat.LayoutParams(width, width)
            return CustomViewHolder(imageview)
        }

        inner class CustomViewHolder(var imageview: ImageView) : RecyclerView.ViewHolder(imageview) {

        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val imageview = (holder as CustomViewHolder).imageview
            Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl).apply(RequestOptions().centerCrop()).into(imageview)
        }

        override fun getItemCount(): Int = contentDTOs.size
    }
}