package com.iksanova.mingle.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.iksanova.mingle.R
import com.iksanova.mingle.constants.Constants.CONNECTIONS
import com.iksanova.mingle.constants.Constants.REQUEST
import com.iksanova.mingle.constants.Constants.USER_CONSTANT
import com.iksanova.mingle.models.RequestModel

class RequestAdapter(private val aCtx: Context, private val list: List<RequestModel>) : RecyclerView.Adapter<RequestAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(aCtx).inflate(R.layout.card_network_request, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.name.text = list[position].username
        holder.headline.text = list[position].headline
        Glide.with(aCtx).load(list[position].imageUrl).into(holder.userImage)
        val user = FirebaseAuth.getInstance().currentUser
        val ref = FirebaseDatabase.getInstance().getReference(USER_CONSTANT).child(user!!.uid)

        holder.connectOk.setOnClickListener {
            list[position].key?.let { it1 -> ref.child(REQUEST).child(it1).setValue(null) }
            list[position].key?.let { it1 -> ref.child(CONNECTIONS).child(it1).setValue(true) }
        }

        holder.connectCancel.setOnClickListener {
            list[position].key?.let { it1 -> ref.child(it1).setValue(null) }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView = itemView.findViewById(R.id.item_text)
        var userImage: ImageView = itemView.findViewById(R.id.item_image)
        var connectOk: ViewGroup = itemView.findViewById(R.id.connect_ok)
        var connectCancel: ViewGroup = itemView.findViewById(R.id.connect_cancel)
        var headline: TextView = itemView.findViewById(R.id.item_headline)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun setHasStableIds(hasStableIds: Boolean) {
        hasStableIds
    }
}