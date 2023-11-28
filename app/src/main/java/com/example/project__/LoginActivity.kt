package com.example.project__

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private val PREFS_KEY = "com.example.project__.PREFS_KEY"

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 자동 로그인 체크
        if (Firebase.auth.currentUser != null)  {
            // 저장된 이메일과 비밀번호가 있는지 확인
            val sharedPreferences = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
            val savedEmail = sharedPreferences.getString("email", null)
            val savedPassword = sharedPreferences.getString("password", null)

            if (!savedEmail.isNullOrBlank() && !savedPassword.isNullOrBlank()) {
                // 저장된 이메일과 비밀번호가 있을 경우 자동으로 로그인 시도
                doLogin(savedEmail, savedPassword)
            }
        }
        findViewById<Button>(R.id.login_join)?.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.login)?.setOnClickListener {
            val userEmail = findViewById<EditText>(R.id.text_email)?.text.toString()
            val password = findViewById<EditText>(R.id.password)?.text.toString()

            // null 또는 빈 문자열이 입력되지 않도록 검증
            if (userEmail.isBlank() || password.isBlank()) {
                showErrorDialog("이메일과 비밀번호를 입력하세요.")
                return@setOnClickListener
            }

            doLogin(userEmail, password)
        }
    }


    private fun doLogin(userEmail: String, password: String) {
        Firebase.auth.signInWithEmailAndPassword(userEmail, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // 사용자의 로그인 상태를 shared preferences에 저장
                    saveLoginStatus(true, userEmail, password)
                    val welcomeMessage = "안녕하세요, ${Firebase.auth.currentUser?.displayName} 님!"
                    Toast.makeText(this, welcomeMessage, Toast.LENGTH_SHORT).show()
                    finish()
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    val errorMessage = getErrorMessage(task.exception)
                    showErrorDialog(errorMessage)
                }
            }
    }

    private fun saveLoginStatus(isLoggedIn: Boolean, email: String, password: String) {
        val sharedPreferences = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isLoggedIn", isLoggedIn)
        editor.putString("email", email)
        editor.putString("password", password)
        editor.apply()
        editor.commit()
    }

    private fun getErrorMessage(exception: Exception?): String {
        return when (exception) {
            is com.google.firebase.auth.FirebaseAuthInvalidUserException -> "유효하지 않은 사용자입니다."
            is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "이메일 또는 비밀번호가 올바르지 않습니다."
            is com.google.firebase.auth.FirebaseAuthUserCollisionException -> "이미 가입된 이메일 주소입니다."
            else -> "로그인 실패"
        }
    }

    private fun showErrorDialog(errorMessage: String) {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("로그인 실패")
        alertDialog.setMessage(errorMessage)
        alertDialog.setPositiveButton("확인") { _, _ ->
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        alertDialog.show()
    }
}
