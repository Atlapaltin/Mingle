package com.iksanova.mingle.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iksanova.mingle.R
import com.iksanova.mingle.models.PostModel
import com.iksanova.mingle.utils.AppSharedPreferences
import com.github.pgreze.reactions.PopupGravity
import com.github.pgreze.reactions.ReactionPopup
import com.github.pgreze.reactions.ReactionsConfigBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.iksanova.mingle.constants.Constants.ALL_POSTS
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.luseen.autolinklibrary.AutoLinkMode
import com.luseen.autolinklibrary.AutoLinkTextView

class PostAdapter(private val aCtx: Context, private val list: List<PostModel>) : RecyclerView.Adapter<PostAdapter.MyViewHolder>() {
    private val strings = arrayOf("Like", "Celebrate", "Support", "Love", "Insightful", "Idea")
    private val appSharedPreferences = AppSharedPreferences(aCtx)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(aCtx).inflate(R.layout.card_post, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val user = FirebaseAuth.getInstance().currentUser
        val ref = FirebaseDatabase.getInstance().reference

        val gestureDetector = GestureDetector(aCtx, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean {
                return true
            }

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                list[position].key?.let { addLike(user!!.uid, it, ref, holder) }
                holder.likeClr.setTextColor(ContextCompat.getColor(aCtx, R.color.main_color))
                return super.onSingleTapUp(e)
            }

            override fun onLongPress(e: MotionEvent) {
                val config = ReactionsConfigBuilder(aCtx)
                    .withReactions(intArrayOf(
                        R.drawable.ic_link_like,
                        R.drawable.ic_link_celebrate,
                        R.drawable.ic_link_care,
                        R.drawable.ic_link_love,
                        R.drawable.ic_link_idea,
                        R.drawable.ic_link_curious
                    ))
                    .withPopupAlpha(255)
                    .withReactionTexts { position -> strings[position] }
                    .withTextBackground(ColorDrawable(Color.WHITE))
                    .withTextColor(Color.BLACK)
                    .withPopupGravity(PopupGravity.PARENT_RIGHT)
                    .withTextSize(aCtx.resources.getDimension(R.dimen.reactions_textSize))
                    .build()

                val popup = ReactionPopup(
                    context = aCtx,
                    reactionsConfig = config,
                    reactionSelectedListener = { position: Int ->
                        when (position) {
                            0 -> holder.icLike.setImageDrawable(
                                ContextCompat.getDrawable(
                                    aCtx,
                                    R.drawable.ic_link_like
                                )
                            )

                            1 -> holder.icLike.setImageDrawable(
                                ContextCompat.getDrawable(
                                    aCtx,
                                    R.drawable.ic_link_celebrate
                                )
                            )

                            2 -> holder.icLike.setImageDrawable(
                                ContextCompat.getDrawable(
                                    aCtx,
                                    R.drawable.ic_link_care
                                )
                            )

                            3 -> holder.icLike.setImageDrawable(
                                ContextCompat.getDrawable(
                                    aCtx,
                                    R.drawable.ic_link_love
                                )
                            )

                            4 -> holder.icLike.setImageDrawable(
                                ContextCompat.getDrawable(
                                    aCtx,
                                    R.drawable.ic_link_idea
                                )
                            )

                            else -> holder.icLike.setImageDrawable(
                                ContextCompat.getDrawable(
                                    aCtx,
                                    R.drawable.ic_link_curious
                                )
                            )
                        }
                        // Close selector if not invalid item (testing purpose)
                        position != 3
                    }, reactionPopupStateChangeListener = {
                        // true is closing popup, false is requesting a new selection
                    }
                )

                list[position].key?.let { addLike(user!!.uid, it, ref, holder) }
                holder.buttonLike.setOnTouchListener(popup)
                super.onLongPress(e)
            }
        })

        holder.buttonLike.setOnTouchListener { view, motionEvent ->
            gestureDetector.onTouchEvent(motionEvent)
            if (motionEvent.action == MotionEvent.ACTION_UP && !view.hasOnClickListeners()) {
                view.performClick()
            }
            return@setOnTouchListener true
        }

        holder.desc.addAutoLinkMode(AutoLinkMode.MODE_HASHTAG)
        holder.desc.setHashtagModeColor(aCtx.resources.getColor(R.color.main_color))
        holder.desc.setAutoLinkText(list[position].description)

        Glide.with(aCtx).load(list[position].userProfile).into(holder.userImage)
        holder.userName.text = list[position].username

        if (list[position].imgUrl == "") {
            holder.ll.visibility = View.GONE
        } else {
            Glide.with(aCtx).load(list[position].imgUrl).into(holder.postImage)
            holder.ll.visibility = View.VISIBLE
        }

        val userKey = user!!.uid
        val postKey = list[position].key

        val likeRef =
            postKey?.let { FirebaseDatabase.getInstance().reference.child(ALL_POSTS).child(it).child("Likes") }
        likeRef?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (snapshot1 in snapshot.children) {
                    if (snapshot1.key == userKey) {
                        holder.icLike.setImageDrawable(ContextCompat.getDrawable(aCtx, R.drawable.ic_link_like))
                        holder.likeClr.setTextColor(ContextCompat.getColor(aCtx, R.color.main_color))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        if (postKey != null) {
            isLikes(holder.likesTxt, holder.commentTxt, postKey)
        }
    }

    private fun isLikes(textView: TextView, commentCount: TextView, postKey: String) {
        val databaseReference = FirebaseDatabase.getInstance().reference
            .child(ALL_POSTS)
            .child(postKey)

        databaseReference.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                textView.text = snapshot.child("Likes").childrenCount.toString()
                commentCount.text = snapshot.child("Comments").childrenCount.toString() + " comments"
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun addLike(uid: String, key: String, ref: DatabaseReference, holder: MyViewHolder) {
        val ref = ref.child(ALL_POSTS).child(key).child("Likes")
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = Date()
        val map: MutableMap<String, Any?> = HashMap()
        map["time"] = sdf.format(date)
        map["username"] = appSharedPreferences.getUserName()
        map["imgUrl"] = appSharedPreferences.getImgUrl()
        ref.child(uid).setValue(map).addOnSuccessListener {
            holder.icLike.setImageDrawable(ContextCompat.getDrawable(aCtx, R.drawable.ic_link_like))
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var desc: AutoLinkTextView = itemView.findViewById(R.id.text_1)
        var postImage: ImageView = itemView.findViewById(R.id.post_img)
        var buttonLike: LinearLayout = itemView.findViewById(R.id.btn_like)
        var ll: CardView = itemView.findViewById(R.id.ll)
        var icLike: ImageView = itemView.findViewById(R.id.ic_like)
        var userImage: CircleImageView = itemView.findViewById(R.id.button_image)
        var userName: TextView = itemView.findViewById(R.id.username)
        var likesTxt: TextView = itemView.findViewById(R.id.likesTxt)
        var commentTxt: TextView = itemView.findViewById(R.id.commentTxt)
        var likeClr: TextView = itemView.findViewById(R.id.like_clr)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun setHasStableIds(hasStableIds: Boolean) {
        super.setHasStableIds(hasStableIds)
    }
}
