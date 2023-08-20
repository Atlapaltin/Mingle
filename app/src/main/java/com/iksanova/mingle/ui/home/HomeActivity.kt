package com.iksanova.mingle.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
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
import com.iksanova.mingle.utils.UniversalImageLoderClass
import com.nostra13.universalimageloader.core.ImageLoader

class HomeActivity : BaseActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var profileImg: ImageView
    private lateinit var messageBtn: ImageView
    private lateinit var nav_img: ImageView
    private lateinit var nav_close_img: ImageView
    private lateinit var mNavigationView: NavigationView
    private lateinit var tt: TextView
    private lateinit var nav_name: TextView
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
        userRef = FirebaseDatabase.getInstance().getReference().child(Constants.USER_CONSTANT).child(user.uid)
        drawerLayout = findViewById(R.id.drawerLayout)
        profileImg = findViewById(R.id.img)
        messageBtn = findViewById(R.id.messageBtn)
        mNavigationView = findViewById(R.id.nav_view)

        //UniversalImageLoaderClass
        val universalImageLoderClass = UniversalImageLoderClass(this)
        ImageLoader.getInstance().init(universalImageLoderClass.config)

        // Header
        val header = mNavigationView.getHeaderView(0)
        nav_name = header.findViewById(R.id.user_name)
        nav_img = header.findViewById(R.id.img)
        nav_close_img = header.findViewById(R.id.close_img)
        tt = header.findViewById(R.id.tt)

        //Open Profile Activity
        tt.setOnClickListener { startActivity(Intent(this@HomeActivity, ProfileActivity::class.java)) }

        // Set Header Data
        Glide.with(this).load(appSharedPreferences.imgUrl).into(profileImg)
        Glide.with(this).load(appSharedPreferences.imgUrl).into(nav_img)
        nav_name.text = appSharedPreferences.userName

        //NavBar Close
        nav_close_img.setOnClickListener {
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
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationSelectedListener)
        supportFragmentManager.beginTransaction().replace(R.id.frame_layout, HomeFragment()).commit()

        // Get Data from Firebase
        userRef.child("Info").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                model = snapshot.getValue(UserModel::class.java)!!
                appSharedPreferences.username = model.username
                appSharedPreferences.imgUrl = model.imageUrl
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private val navigationSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_home -> selectedFragment = HomeFragment()
            R.id.nav_network -> selectedFragment = NetworkFragment()
            R.id.nav_uplod -> {
                selectedFragment = null
                startActivity(Intent(this@HomeActivity, SharePostActivity::class.java))
                overridePendingTransition(R.anim.slide_up, R.anim.slide_down)
            }
            R.id.nav_notification -> selectedFragment = NotificationFragment()
            R.id.nav_jobs -> selectedFragment = JobsFragment()
        }

        if (selectedFragment != null) {
            supportFragmentManager.beginTransaction().replace(R.id.frame_layout, selectedFragment!!).commit()
        }
        true
    }

    override fun onBackPressed() {
        val mBottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation_bar)
        if (mBottomNavigationView.selectedItemId == R.id.nav_home) {
            super.onBackPressed()
            finish()
        } else {
            mBottomNavigationView.selectedItemId = R.id.nav_home
        }
    }
}