package com.iksanova.mingle.adapters

import com.iksanova.mingle.R
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iksanova.mingle.models.UserModel
import com.iksanova.mingle.ui.custom_user.CustomUserActivity


class NetworkAdapter(private val aCtx: Context, private val list: List<UserModel>) : RecyclerView.Adapter<NetworkAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(aCtx).inflate(R.layout.card_network, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.name.text = list[position].username
        Glide.with(aCtx).load(list[position].imageUrl).into(holder.userImage)
        holder.headline.text = list[position].headline
        holder.itemView.setOnClickListener {
            val intent = Intent(aCtx, CustomUserActivity::class.java)
            intent.putExtra("user_data", list[position])
            aCtx.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.txt_name)
        val userImage: ImageView = itemView.findViewById(R.id.profileImg)
        val headline: TextView = itemView.findViewById(R.id.text_headline)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun setHasStableIds(hasStableIds: Boolean) {
        super.setHasStableIds(hasStableIds)
    }
}
