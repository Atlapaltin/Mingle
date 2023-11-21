package com.iksanova.mingle.ui.home

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.iksanova.mingle.R
import com.iksanova.mingle.base.BaseActivity
import com.iksanova.mingle.constants.Constants
import com.iksanova.mingle.models.UserModel
import com.iksanova.mingle.ui.fragments.HomeFragment
import com.iksanova.mingle.ui.fragments.JobsFragment
import com.iksanova.mingle.ui.fragments.NetworkFragment
import com.iksanova.mingle.ui.fragments.NotificationFragment
import com.iksanova.mingle.ui.message_user.MessageUsersActivity
import com.iksanova.mingle.ui.profile.ProfileActivity
import com.iksanova.mingle.ui.share_post.SharePostActivity
import com.iksanova.mingle.utils.AppSharedPreferences
import com.iksanova.mingle.utils.UniversalImageLoaderClass
import com.nostra13.universalimageloader.core.ImageLoader


class HomeActivity : BaseActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var profileImg: ImageView
    private lateinit var messageBtn: ImageView
    private lateinit var navImg: ImageView
    private lateinit var navCloseImg: ImageView
    private lateinit var mNavigationView: NavigationView
    private lateinit var tt: TextView
    private lateinit var navName: TextView
    private lateinit var bottomNavigationView: BottomNavigationView
    private var selectedFragment: Fragment? = null
    private lateinit var appSharedPreferences: AppSharedPreferences
    private lateinit var userRef: DatabaseReference
    private lateinit var user: FirebaseUser
    private lateinit var model: UserModel

    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        appSharedPreferences = AppSharedPreferences(this)
        user = FirebaseAuth.getInstance().currentUser!!
        userRef = FirebaseDatabase.getInstance().reference.child(Constants.USER_CONSTANT).child(user.uid)
        drawerLayout = findViewById(R.id.drawerLayout)
        profileImg = findViewById(R.id.img)
        messageBtn = findViewById(R.id.messageBtn)
        mNavigationView = findViewById(R.id.nav_view)

        //UniversalImageLoaderClass
        val universalImageLoaderClass = UniversalImageLoaderClass(this)
        ImageLoader.getInstance().init(universalImageLoaderClass.getConfig())

        // Header
        val header = mNavigationView.getHeaderView(0)
        navName = header.findViewById(R.id.user_name)
        navImg = header.findViewById(R.id.img)
        navCloseImg = header.findViewById(R.id.close_img)
        tt = header.findViewById(R.id.tt)

        //Open Profile Activity
        tt.setOnClickListener { startActivity(Intent(this@HomeActivity, ProfileActivity::class.java)) }

        // Set Header Data
        Glide.with(this).load(appSharedPreferences.imgUrl).into(profileImg)
        Glide.with(this).load(appSharedPreferences.imgUrl).into(navImg)
        navName.text = appSharedPreferences.userName

        //NavBar Close
        navCloseImg.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
        }

        // Open Drawer Layout
        profileImg.setOnClickListener {
            if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.openDrawer(GravityCompat.START)
            } else {
                drawerLayout.closeDrawer(GravityCompat.END)
            }
        }

        // Open Message Activity
        messageBtn.setOnClickListener {
            val intent = Intent(this@HomeActivity, MessageUsersActivity::class.java)
            startActivity(intent)
        }

        //BottomNavigationView
        bottomNavigationView = findViewById(R.id.bottom_navigation_bar)
        bottomNavigationView.setOnItemSelectedListener(navigationSelectedListener)
        supportFragmentManager.beginTransaction().replace(R.id.frame_layout, HomeFragment())
            .commit()

        // Get Data from Firebase
        userRef.child("Info").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                model = snapshot.getValue(UserModel::class.java)!!
                appSharedPreferences.userName = model.username
                appSharedPreferences.imgUrl = model.imageUrl
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private val navigationSelectedListener = NavigationBarView.OnItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_home -> selectedFragment = HomeFragment()
            R.id.nav_network -> selectedFragment = NetworkFragment()
            R.id.nav_uplod -> {
                selectedFragment = null
                startActivity(Intent(this@HomeActivity, SharePostActivity::class.java),
                    ActivityOptions.makeCustomAnimation(this@HomeActivity, R.anim.slide_up, R.anim.slide_down).toBundle())
            }
            R.id.nav_notification -> selectedFragment = NotificationFragment()
            R.id.nav_jobs -> selectedFragment = JobsFragment()
        }

        if (selectedFragment != null) {
            supportFragmentManager.beginTransaction().replace(R.id.frame_layout, selectedFragment!!).commit()
        }
        true
    }


}