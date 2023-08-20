package com.iksanova.mingle.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.iksanova.mingle.R
import com.iksanova.mingle.models.StoryModel
import com.iksanova.mingle.ui.story.AddStoryActivity
import com.iksanova.mingle.ui.story.StoryActivity
import com.iksanova.mingle.utils.UniversalImageLoderClass
import de.hdodenhof.circleimageview.CircleImageView
import java.util.Objects

class StoryAdapter(private val aCtx: Context, private val list: List<StoryModel>) : RecyclerView.Adapter<StoryAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View = if (viewType == 0) {
            LayoutInflater.from(aCtx).inflate(R.layout.card_mystory, parent, false)
        } else {
            LayoutInflater.from(aCtx).inflate(R.layout.card_story, parent, false)
        }
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = list[position]
        userInfo(holder, model.userId, position)

        if (holder.adapterPosition != 0) {
            seenStory(holder, model.userId)
        }
        if (holder.adapterPosition == 0) {
            myStory(holder.addStory_text, holder.story_plus, false, holder)
        }

        holder.itemView.setOnClickListener {
            if (holder.adapterPosition == 0) {
                myStory(holder.addStory_text, holder.story_plus, true, holder)
            } else {
                val intent = Intent(aCtx, StoryActivity::class.java)
                intent.putExtra("userid", model.userId)
                aCtx.startActivity(intent)
            }
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            0
        } else {
            1
        }
    }

    private fun userInfo(holder: MyViewHolder, userid: String, position: Int) {

        val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userid).child("Info")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.child("username").getValue(String::class.java)
                val img = snapshot.child("imageUrl").getValue(String::class.java)
                UniversalImageLoderClass.setImage(img, holder.story_photo, null)

                if (position != 0) {
                    UniversalImageLoderClass.setImage(img, holder.story_photo, null)
                    holder.story_username.text = name
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun myStory(textView: TextView, imageView: ImageView, click: Boolean, holder: MyViewHolder) {
        val reference = FirebaseDatabase.getInstance().getReference("Story").child(FirebaseAuth.getInstance().currentUser!!.uid)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var count = 0
                val timeCurrent = System.currentTimeMillis()
                for (snap in snapshot.children) {
                    val storyModel = snap.getValue(StoryModel::class.java)
                    if (storyModel != null) {
                        if (timeCurrent > storyModel.timeStart && timeCurrent < storyModel.timeEnd) {
                            count++
                        }
                    }
                }

                if (click) {
                    if (count > 0) {
                        val alertDialog = AlertDialog.Builder(aCtx).create()
                        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "View Story") { dialogInterface, i ->
                            val intent = Intent(aCtx, StoryActivity::class.java)
                            intent.putExtra("userid", FirebaseAuth.getInstance().currentUser!!.uid)
                            aCtx.startActivity(intent)
                            dialogInterface.dismiss()
                        }
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Add Story") { dialogInterface, i ->
                            val intent = Intent(aCtx, AddStoryActivity::class.java)
                            aCtx.startActivity(intent)
                            dialogInterface.dismiss()
                        }
                        alertDialog.show()
                    } else {
                        val intent = Intent(aCtx, AddStoryActivity::class.java)
                        aCtx.startActivity(intent)
                    }
                } else {
                    if (count > 0) {
                        textView.text = "Your Story"
                        holder.story_photo_seen_layout.setBackgroundResource(R.drawable.profile_picture_gradient)
                        imageView.visibility = View.GONE
                        holder.white_card.visibility = View.GONE

                    } else {
                        textView.text = "Add Story"
                        imageView.visibility = View.VISIBLE
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun seenStory(holder: MyViewHolder, userId: String) {
        val reference = FirebaseDatabase.getInstance().getReference("Story").child(userId)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var i = 0
                for (snapshot1 in snapshot.children) {
                    if (!snapshot1.child("views")
                            .child(FirebaseAuth.getInstance().currentUser!!.uid).exists() && System.currentTimeMillis() < snapshot1.getValue(StoryModel::class.java)!!.timeEnd) {
                        i++
                    }
                }
                if (i > 0) {
                    holder.story_photo.visibility = View.VISIBLE
                    holder.story_photo_seen_layout.setBackgroundResource(R.drawable.profile_picture_gradient)

                } else {
                    holder.story_photo_seen_layout.setBackgroundColor(Color.GRAY)
                    holder.story_photo.visibility = View.VISIBLE

                }
            }


            override fun onCancelled(error: DatabaseError) {

            }
        })

    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var story_username: TextView = itemView.findViewById(R.id.story_username)
        var addStory_text: TextView = itemView.findViewById(R.id.mystorytext)
        var story_plus: ImageView = itemView.findViewById(R.id.add_story)
        var story_photo: CircleImageView = itemView.findViewById(R.id.button_image)
        var story_photo_seen_layout: RelativeLayout = itemView.findViewById(R.id.button_click_parent)
        var white_card: CardView = itemView.findViewById(R.id.white_card)


    }
}
