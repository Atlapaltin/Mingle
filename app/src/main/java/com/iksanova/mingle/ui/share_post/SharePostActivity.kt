package com.iksanova.mingle.ui.share_post

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.iksanova.mingle.R
import com.iksanova.mingle.base.BaseActivity
import com.iksanova.mingle.constants.Constants.INFO
import com.iksanova.mingle.ui.home.HomeActivity
import com.iksanova.mingle.utils.AppSharedPreferences
import com.iksanova.mingle.utils.LoadingDialog

class SharePostActivity : BaseActivity() {
    private lateinit var edit_text: EditText
    private lateinit var post_img: ImageView
    private lateinit var btn_select_img: ImageView
    private lateinit var profileImg: ImageView
    private lateinit var closeImg: ImageView
    private lateinit var userName: TextView
    private lateinit var btn_post: TextView
    private val PICK_IMAGE_REQUEST = 1
    private var mImageUri: Uri? = null
    private lateinit var mStorageRef: StorageReference
    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser
    private lateinit var appSharedPreferences: AppSharedPreferences
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share_post)
        appSharedPreferences = AppSharedPreferences(this)
        loadingDialog = LoadingDialog(this)
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!
        mStorageRef = FirebaseStorage.getInstance().reference
        edit_text = findViewById(R.id.edit_text)
        post_img = findViewById(R.id.post_img)
        btn_select_img = findViewById(R.id.img3)
        btn_post = findViewById(R.id.btn_post)
        userName = findViewById(R.id.user_name)
        profileImg = findViewById(R.id.user_img)
        closeImg = findViewById(R.id.close_img)

        userName.text = appSharedPreferences.getUserName()
        Glide.with(this).load(appSharedPreferences.getImgUrl()).into(profileImg)

        // Close Activity
        closeImg.setOnClickListener { finish() }

        // Select Image
        btn_select_img.setOnClickListener { openFileChooser() }

        edit_text.requestFocus()
        edit_text.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                btn_post.setTextColor(Color.BLACK)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Post Button
        btn_post.setOnClickListener {
            if (mImageUri != null) {
                loadingDialog.startLoadingDialog()
                uploadFile(mImageUri!!)
            } else {
                if (!edit_text.text.toString().isEmpty())
                    loadingDialog.startLoadingDialog()
                uploadData(edit_text.text.toString())
            }
        }
    }

    private fun uploadData(description: String) {
        val ref = FirebaseDatabase.getInstance().getReference().child("AllPosts")
        val key = ref.push().key
        val map: HashMap<String, Any> = HashMap()
        map["description"] = description
        map["imgUrl"] = ""
        map["username"] = appSharedPreferences.getUserName()
        map["user_profile"] = appSharedPreferences.getImgUrl()
        map["key"] = key!!
        ref.child(key).child(INFO).setValue(map).addOnCompleteListener { task ->
            loadingDialog.dismissDialog()
            val intent = Intent(this@SharePostActivity, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            mImageUri = data.data
            CropImage.activity(mImageUri)
                .start(this)
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                mImageUri = result.uri
                Glide.with(this).load(mImageUri)
                    .into(post_img)
                btn_post.setTextColor(Color.BLACK)

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                Toast.makeText(this@SharePostActivity, "" + error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadFile(mImageUri: Uri) {
        if (mImageUri != null) {
            val reference = mStorageRef.child(user.uid).child("Files/" + System.currentTimeMillis())
            reference.putFile(mImageUri)
                .addOnSuccessListener { taskSnapshot ->
                    reference.downloadUrl.addOnSuccessListener { uri ->
                        val ref = FirebaseDatabase.getInstance().getReference().child("AllPosts")
                        val key = ref.push().key
                        val map: HashMap<String, Any> = HashMap()
                        val imageUrl = uri.toString()
                        map["imgUrl"] = imageUrl
                        map["description"] = edit_text.text.toString()
                        map["username"] = appSharedPreferences.getUserName()
                        map["user_profile"] = appSharedPreferences.getImgUrl()
                        map["key"] = key!!
                        ref.child(key).child(INFO).setValue(map).addOnCompleteListener { task ->
                            loadingDialog.dismissDialog()
                            val intent = Intent(this@SharePostActivity, HomeActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this@SharePostActivity, e.message, Toast.LENGTH_SHORT).show()
                }
        }
    }
}
