package com.example.kotlinuberremake

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.example.kotlinuberremake.utils.UserUtils
import com.firebase.geofire.GeoFire
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

/**
 * Created by iddangunawan on 30/06/20
 */
class DriverHomeActivity : AppCompatActivity() {

    companion object {
        private val PICK_IMAGE_REQUEST: Int = 7172
    }

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var navController: NavController
    private lateinit var waitingDialog: AlertDialog
    private lateinit var storageReference: StorageReference

    private var imageUri: Uri? = null
    private var imgAvatar: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_home)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(R.id.nav_home), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navView.setNavigationItemSelectedListener { item ->
            if (item.itemId == R.id.nav_sign_out) {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                builder.setTitle("Sign out")
                    .setMessage("Do you want to sign out ?")
                    .setCancelable(false)
                    .setNegativeButton("Cancel") { dialogInterface, p1 -> dialogInterface.dismiss() }
                    .setPositiveButton("Sign out") { dialogInterface, p1 ->
                        GeoFire(
                            FirebaseDatabase.getInstance()
                                .getReference(Common.DRIVERS_LOCATION_REFERENCE)
                        ).removeLocation(FirebaseAuth.getInstance().currentUser!!.uid)
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(this, SplashScreenActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                val dialog: AlertDialog = builder.create()
                dialog.setOnShowListener { dialogInterface ->
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(resources.getColor(android.R.color.holo_red_dark))
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(resources.getColor(R.color.colorAccent))
                }
                dialog.show()
            }
            true
        }

        waitingDialog = AlertDialog.Builder(this)
            .setCancelable(false)
            .setMessage("Waiting ...")
            .create()

        storageReference = FirebaseStorage.getInstance().reference

        // Set data for user
        val headerView = navView.getHeaderView(0)
        val txtName = headerView.findViewById(R.id.txt_name) as TextView
        val txtPhone = headerView.findViewById(R.id.txt_phone) as TextView
        val txtStar = headerView.findViewById(R.id.txt_star) as TextView
        imgAvatar = headerView.findViewById(R.id.img_avatar) as ImageView

        txtName.text = Common.buildWelcomeMessage()
        txtPhone.text = Common.currentUser?.phoneNumber ?: ""
        txtStar.text = Common.currentUser?.rating.toString()

        if (Common.currentUser != null && Common.currentUser?.avatar != "" && !TextUtils.isEmpty(
                Common.currentUser?.avatar
            )
        ) {
            Glide.with(this)
                .load(Common.currentUser?.avatar)
                .into(imgAvatar!!)
        }

        imgAvatar?.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null && data.data != null) {
                imageUri = data.data
                imgAvatar?.setImageURI(imageUri)

                showDialogUpload()
            }
        }
    }

    private fun showDialogUpload() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Change avatar")
            .setMessage("Do you want to change avatar ?")
            .setCancelable(false)
            .setNegativeButton("Cancel") { dialogInterface, p1 -> dialogInterface.dismiss() }
            .setPositiveButton("Upload") { dialogInterface, p1 ->
                if (imageUri != null) {
                    waitingDialog.setMessage("Uploading ...")
                    waitingDialog.show()

                    val uniqueName = FirebaseAuth.getInstance().currentUser!!.uid
                    val avatarFolder = storageReference.child("avatars/$uniqueName")

                    avatarFolder.putFile(imageUri!!)
                        .addOnFailureListener { error ->
                            waitingDialog.dismiss()
                            Snackbar.make(
                                drawerLayout,
                                error.message.toString(),
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                avatarFolder.downloadUrl
                                    .addOnSuccessListener { uri ->
                                        val updateData: HashMap<String, Any> = HashMap()
                                        updateData["avatar"] = uri.toString()

                                        UserUtils.updateUser(drawerLayout, updateData)
                                    }
                            }
                            waitingDialog.dismiss()
                        }
                        .addOnProgressListener { taskSnapshot ->
                            val progress =
                                (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                            waitingDialog.setMessage(
                                StringBuilder("Uploading: ").append(progress).append("%")
                            )
                        }
                }
            }
        val dialog: AlertDialog = builder.create()
        dialog.setOnShowListener { dialogInterface ->
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(resources.getColor(android.R.color.holo_red_dark))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(resources.getColor(R.color.colorAccent))
        }
        dialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.driver_home, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}