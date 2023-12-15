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
import com.iksanova.mingle.R
import com.iksanova.mingle.base.BaseActivity
import com.iksanova.mingle.constants.Constants
import com.iksanova.mingle.databinding.ActivityHomeBinding
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
    private lateinit var binding: ActivityHomeBinding
    private lateinit var appSharedPreferences: AppSharedPreferences
    private var selectedFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        appSharedPreferences = AppSharedPreferences(this)

        //UniversalImageLoaderClass
        val universalImageLoaderClass = UniversalImageLoaderClass(this)
        ImageLoader.getInstance().init(universalImageLoaderClass.getConfig())

        // Set Header Data
        Glide.with(this).load(appSharedPreferences.imgUrl).into(binding.img)
        Glide.with(this).load(appSharedPreferences.imgUrl).into(binding.navView.img)
        binding.navView.userName.text = appSharedPreferences.userName

        //NavBar Close
        binding.navView.closeImg.setOnClickListener {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }
        }

        // Open Drawer Layout
        binding.img.setOnClickListener {
            if (!binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            } else {
                binding.drawerLayout.closeDrawer(GravityCompat.END)
            }
        }

        // Open Message Activity
        binding.messageBtn.setOnClickListener {
            val intent = Intent(this@HomeActivity, MessageUsersActivity::class.java)
            startActivity(intent)
        }

        //BottomNavigationView
        binding.bottomNavigationBar.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> selectedFragment = HomeFragment()
                R.id.nav_network -> selectedFragment = NetworkFragment()
                R.id.nav_uplod -> {
                    startActivity(
                        Intent(
                            this@HomeActivity,
                            SharePostActivity::class.java
                        ), ActivityOptions.makeCustomAnimation(
                            this@HomeActivity,
                            R.anim.slide_up,
                            R.anim.slide_down
                        ).toBundle()
                    )
                }
                R.id.nav_notification -> selectedFragment = NotificationFragment()
                R.id.nav_jobs -> selectedFragment = JobsFragment()
            }

            if (selectedFragment != null) {
                supportFragmentManager.beginTransaction().replace(
                    binding.frameLayout.id,
                    selectedFragment!!
                ).commit()
            }
            true
        }
    }
}
