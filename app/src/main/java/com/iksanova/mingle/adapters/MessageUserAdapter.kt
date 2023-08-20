package com.iksanova.mingle.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.iksanova.mingle.R
import com.iksanova.mingle.models.UserModel
import com.bumptech.glide.Glide

class MessageUserAdapter(private val aCtx: Context, private val list: List<UserModel>) :
    RecyclerView.Adapter<MessageUserAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(aCtx).inflate(R.layout.card_useritem, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.name.text = list[position].username
        Glide.with(aCtx).load(list[position].imageUrl).into(holder.userImage)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun setHasStableIds(hasStableIds: Boolean) {
        super.setHasStableIds(hasStableIds)
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView = itemView.findViewById(R.id.item_text)
        var userImage: ImageView = itemView.findViewById(R.id.item_image)
    }
}
