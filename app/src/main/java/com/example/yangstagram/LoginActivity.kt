package com.example.yangstagram

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val emailLoginButton = findViewById<Button>(R.id.email_login_button).setOnClickListener {
            signinAndSignup()
        }
    }

    fun signinAndSignup() {
        val email = findViewById<EditText>(R.id.email_edittext).text.toString()
        val password = findViewById<EditText>(R.id.password_edittext).text.toString()

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            task ->
                if (task.isSuccessful) {
                    // Creating a user account
                    moveMainPage(task.result?.user)
                } else if (task.exception?.message.isNullOrEmpty()) {
                    // Show the error message
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                } else {
                    // Login if you have account
                    signinEmail()
                }
        }
    }

    fun signinEmail() {
        val email = findViewById<EditText>(R.id.email_edittext).text.toString()
        val password = findViewById<EditText>(R.id.password_edittext).text.toString()

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                task ->
            if (task.isSuccessful) {
                // Login
                moveMainPage(task.result?.user)
            } else {
                // Show the error message
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun moveMainPage(user: FirebaseUser?) {
        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}