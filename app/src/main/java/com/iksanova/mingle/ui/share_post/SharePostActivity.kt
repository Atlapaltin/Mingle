package com.iksanova.mingle.ui.share_post

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.iksanova.mingle.R
import com.iksanova.mingle.base.BaseActivity
import com.iksanova.mingle.constants.Constants.INFO
import com.iksanova.mingle.ui.home.HomeActivity
import com.iksanova.mingle.utils.AppSharedPreferences
import com.iksanova.mingle.utils.LoadingDialog
import com.theartofdev.edmodo.cropper.CropImage

class SharePostActivity : BaseActivity() {
    private lateinit var editText: EditText
    private lateinit var postImg: ImageView
    private lateinit var btnSelectImg: ImageView
    private lateinit var profileImg: ImageView
    private lateinit var closeImg: ImageView
    private lateinit var userName: TextView
    private lateinit var btnPost: TextView
    private val pickImageRequest = 1
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
        editText = findViewById(R.id.edit_text)
        postImg = findViewById(R.id.post_img)
        btnSelectImg = findViewById(R.id.img3)
        btnPost = findViewById(R.id.btn_post)
        userName = findViewById(R.id.user_name)
        profileImg = findViewById(R.id.user_img)
        closeImg = findViewById(R.id.close_img)

        userName.text = appSharedPreferences.userName
        Glide.with(this).load(appSharedPreferences.imgUrl).into(profileImg)

        // Close Activity
        closeImg.setOnClickListener { finish() }

        // Select Image
        btnSelectImg.setOnClickListener { openFileChooser() }
        editText.requestFocus()
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                btnPost.setTextColor(Color.BLACK)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Post Button
        btnPost.setOnClickListener {
            if (mImageUri != null) {
                loadingDialog.startLoadingDialog()
                uploadFile(mImageUri!!)
            } else {
                if (editText.text.toString().isNotEmpty())
                    loadingDialog.startLoadingDialog()
                uploadData(editText.text.toString())
            }
        }
    }

    private fun uploadData(description: String) {
        val ref = FirebaseDatabase.getInstance().reference.child("AllPosts")
        val key = ref.push().key
        val map: HashMap<String, Any?> = HashMap()
        map["description"] = description
        map["imgUrl"] = ""
        map["username"] = appSharedPreferences.userName
        map["userProfile"] = appSharedPreferences.imgUrl
        map["key"] = key!!
        ref.child(key).child(INFO).setValue(map).addOnCompleteListener {
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
        startActivityForResult(intent, pickImageRequest)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == pickImageRequest && resultCode == RESULT_OK && data != null) {
            mImageUri = data.data
            CropImage.activity(mImageUri)
                .start(this)
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                mImageUri = result.uri
                Glide.with(this).load(mImageUri)
                    .into(postImg)
                btnPost.setTextColor(Color.BLACK)

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                Toast.makeText(this@SharePostActivity, "" + error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadFile(mImageUri: Uri) {
        val reference = mStorageRef.child(user.uid).child("Files/" + System.currentTimeMillis())
        reference.putFile(mImageUri)
            .addOnSuccessListener {
                reference.downloadUrl.addOnSuccessListener { uri ->
                    val ref = FirebaseDatabase.getInstance().reference.child("AllPosts")
                    val key = ref.push().key
                    val map: HashMap<String, Any?> = HashMap()
                    val imageUrl = uri.toString()
                    map["imgUrl"] = imageUrl
                    map["description"] = editText.text.toString()
                    map["username"] = appSharedPreferences.userName
                    map["userProfile"] = appSharedPreferences.imgUrl
                    map["key"] = key!!
                    ref.child(key).child(INFO).setValue(map).addOnCompleteListener {
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
