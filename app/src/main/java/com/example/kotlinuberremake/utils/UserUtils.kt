package com.example.kotlinuberremake.utils

import android.view.View
import com.example.kotlinuberremake.Common
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

/**
 * Created by iddangunawan on 10/07/20
 */
class UserUtils {
    companion object {
        fun updateUser(view: View, updateData: HashMap<String, Any>) {
            FirebaseDatabase.getInstance()
                .getReference(Common.DRIVER_INFO_REFERENCE)
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .updateChildren(updateData)
                .addOnFailureListener { error ->
                    Snackbar.make(view, error.message.toString(), Snackbar.LENGTH_SHORT).show()
                }
                .addOnSuccessListener {
                    Snackbar.make(view, "Update information successfully!", Snackbar.LENGTH_SHORT).show()
                }
        }
    }
}