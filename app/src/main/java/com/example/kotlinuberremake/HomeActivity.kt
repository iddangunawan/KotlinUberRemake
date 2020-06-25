package com.example.kotlinuberremake

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        btn_sign_out.setOnClickListener {
            AuthUI.getInstance().signOut(this).addOnCompleteListener {
                Log.d("wkwkw", "${it.result}")
                Toast.makeText(this, "Bye!!", Toast.LENGTH_SHORT).show()
                startActivity((Intent(this, SplashScreenActivity::class.java)))
                finish()
            }
        }
    }
}