package com.iksanova.mingle.ui.story

import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.MimeTypeMap
import android.widget.Toast
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.iksanova.mingle.R
import com.iksanova.mingle.base.BaseActivity
import com.iksanova.mingle.constants.Constants
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddStoryActivity : BaseActivity() {
    private lateinit var mImageUri: Uri
    private var myUrl = ""
    private lateinit var mStorageReference: StorageReference
    private lateinit var storageTask: StorageTask<*>
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_story)
        mStorageReference = FirebaseStorage.getInstance().getReference(Constants.STORY)
        openFileChooser()
    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun getFileExtension(uri: Uri): String? {
        val contentResolver = contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))
    }

    private fun publishStory() {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Posting")
        progressDialog.show()

        if (mImageUri != null) {
            val imageReference = mStorageReference.child(System.currentTimeMillis().toString() + "." +
                    getFileExtension(mImageUri))

            storageTask = imageReference.putFile(mImageUri)
            storageTask.continueWithTask(Continuation { task ->
                if (!task.isSuccessful) {
                    throw task.exception!!
                }
                imageReference.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    myUrl = downloadUri.toString()

                    val myid = FirebaseAuth.getInstance().currentUser!!.uid
                    val storyId = FirebaseDatabase.getInstance().getReference().child("AllStories").child("StoryData").push().key
                    val databaseReference = FirebaseDatabase.getInstance().getReference()
                        .child("Story").child(myid).child(storyId!!)

                    val timend = System.currentTimeMillis() + 86400000
                    val timestart = System.currentTimeMillis() / 1000

                    val hashMap = HashMap<String, Any>()
                    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    val date = Date()
                    hashMap["storyImg"] = myUrl
                    hashMap["timestart"] = timestart
                    hashMap["timeEnd"] = timend
                    hashMap["storyId"] = storyId
                    hashMap["userId"] = myid
                    hashMap["timeUpload"] = sdf.format(date)
                    databaseReference.setValue(hashMap).addOnCompleteListener { task ->
                        progressDialog.dismiss()
                        finish()
                    }
                } else {
                    Toast.makeText(this@AddStoryActivity, "Failed", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e ->
                Toast.makeText(this@AddStoryActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this@AddStoryActivity, "Image not Selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
            && data != null) {
            mImageUri = data.data!!
            CropImage.activity(mImageUri)
                .start(this)
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                mImageUri = result.uri
                publishStory()
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                Toast.makeText(this@AddStoryActivity, "" + error, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
