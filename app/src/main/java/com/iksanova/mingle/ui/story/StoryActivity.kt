package com.iksanova.mingle.ui.story

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.iksanova.mingle.R
import com.iksanova.mingle.base.BaseActivity
import com.iksanova.mingle.models.StoryModel
import de.hdodenhof.circleimageview.CircleImageView
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import android.widget.TextView
import com.google.firebase.database.DatabaseError
import com.iksanova.mingle.constants.Constants
import com.iksanova.mingle.utils.UniversalImageLoderClass
import jp.shts.android.storiesprogressview.StoriesProgressView

class StoryActivity : BaseActivity(), StoriesProgressView.StoriesListener {
    private var counter = 0
    private var presstime: Long = 0L
    private val limit: Long = 500L
    private lateinit var storiesProgressView: StoriesProgressView
    private lateinit var storyPhoto: ImageView
    private lateinit var storyUsername: TextView
    private lateinit var timetxt: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var user: FirebaseUser

    private lateinit var images: List<String>
    private lateinit var storyids: List<String>

    private lateinit var userid: String
    private lateinit var imageView: CircleImageView

    private val onTouchListener = View.OnTouchListener { view, motionEvent ->
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                presstime = System.currentTimeMillis()
                storiesProgressView.pause()
                false
            }
            MotionEvent.ACTION_UP -> {
                val now = System.currentTimeMillis()
                storiesProgressView.resume()
                limit < now - presstime
            }
            else -> false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story)
        val window: Window = this.window

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        // finally change the color
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        storiesProgressView = findViewById(R.id.stories)
        user = FirebaseAuth.getInstance().currentUser!!

        imageView = findViewById(R.id.img)
        storyPhoto = findViewById(R.id.storyimage)
        storyUsername = findViewById(R.id.username)
        timetxt = findViewById(R.id.time)

        userid = intent.getStringExtra("userid")!!

        userInfo(userid)
        getStories(userid)

        val reverse = findViewById<View>(R.id.reverse)
        reverse.setOnClickListener {
            storiesProgressView.reverse()
        }
        reverse.setOnTouchListener(onTouchListener)

        val skip = findViewById<View>(R.id.skip)
        skip.setOnClickListener {
            storiesProgressView.skip()
        }
        skip.setOnTouchListener(onTouchListener)
    }

    override fun onNext() {
        UniversalImageLoderClass.setImage(images[++counter], storyPhoto, progressBar)
        addView(storyids[counter])
    }

    override fun onPrev() {
        if ((counter - 1) < 0)
            return
        UniversalImageLoderClass.setImage(images[--counter], storyPhoto, progressBar)
    }

    override fun onComplete() {
        finish()
    }

    override fun onDestroy() {
        storiesProgressView.destroy()
        super.onDestroy()
    }

    override fun onPause() {
        storiesProgressView.pause()
        super.onPause()
    }

    override fun onResume() {
        storiesProgressView.resume()
        super.onResume()
    }

    private fun getStories(userid: String) {
        images = ArrayList()
        storyids = ArrayList()
        val reference: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Story").child(userid)
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                images.clear()
                storyids.clear()
                for (snap in snapshot.children) {
                    val model: StoryModel = snap.getValue(StoryModel::class.java)!!
                    val timecurrent: Long = System.currentTimeMillis()

                    if (timecurrent > model.timeStart && timecurrent < model.timeEnd) {
                        images.add(model.storyImg)
                        storyids.add(model.storyId)
                        covertTimeToText(model.timeUpload, timetxt)
                    }
                }

                storiesProgressView.setStoriesCount(images.size)
                storiesProgressView.setStoryDuration(5000L)
                storiesProgressView.setStoriesListener(this@StoryActivity)
                storiesProgressView.startStories(counter)

                UniversalImageLoderClass.setImage(images[counter], storyPhoto, null)
                addView(storyids[counter])
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
            }
        })
    }

    private fun userInfo(userid: String) {
        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference.child(Constants.USER_CONSTANT).child(userid)
            .child(Constants.INFO)
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val username: String = snapshot.child("username").getValue(String::class.java)!!
                val img: String = snapshot.child("imgurl").getValue(String::class.java)!!
                UniversalImageLoderClass.setImage(img, imageView, null)
                storyUsername.text = username
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
            }
        })
    }

    private fun addView(storyid: String) {
        if (userid != FirebaseAuth.getInstance().currentUser!!.uid) {
            FirebaseDatabase.getInstance().reference.child("Story").child(userid).child(storyid)
                .child("views").child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(true)
        }
    }

    private fun covertTimeToText(dataDate: String, timetxt: TextView): String? {
        var convTime: String? = null
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val pasTime: Date = dateFormat.parse(dataDate)!!
            val nowTime = Date()
            val dateDiff: Long = nowTime.time - pasTime.time
            val second: Long = TimeUnit.MILLISECONDS.toSeconds(dateDiff)
            val minute: Long = TimeUnit.MILLISECONDS.toMinutes(dateDiff)
            val hour: Long = TimeUnit.MILLISECONDS.toHours(dateDiff)
            convTime = when {
                second < 60 -> "$second s "
                minute < 60 -> "$minute m "
                hour < 24 -> "$hour h "
                else -> ""
            }
            timetxt.text = convTime
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return convTime
    }
}
