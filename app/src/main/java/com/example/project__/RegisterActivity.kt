package com.example.project__

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Suppress("DEPRECATION")
class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var message: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        //뒤로가기
        val toolbar:Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        auth = Firebase.auth

        // 연도, 월, 일 선택에 사용할 데이터 배열 생성
        val years = (1965 until 2010).map { it.toString() }.toTypedArray()
        val months = (1..12).map { it.toString().padStart(2, '0') }.toTypedArray()
        val days = (1..31).map { it.toString().padStart(2, '0') }.toTypedArray()

        // Spinner에 어댑터 설정
        val spinnerYear = findViewById<Spinner>(R.id.spinnerYear)
        val spinnerMonth = findViewById<Spinner>(R.id.spinnerMonth)
        val spinnerDay = findViewById<Spinner>(R.id.spinnerDay)

        spinnerYear.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, years)
        spinnerMonth.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, months)
        spinnerDay.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, days)

        spinnerYear.setSelection(years.indexOf("2000"))

        findViewById<Button>(R.id.join).setOnClickListener {
            val email = findViewById<EditText>(R.id.join_email).text.toString()
            val password = findViewById<EditText>(R.id.join_password).text.toString()
            val passwordconfirm = findViewById<EditText>(R.id.editPWConfirm).text.toString()
            val nickname = findViewById<EditText>(R.id.join_nickname).text.toString()
            val name = findViewById<EditText>(R.id.join_name).text.toString()

            val selectedYear = spinnerYear.selectedItem.toString()
            val selectedMonth = spinnerMonth.selectedItem.toString()
            val selectedDay = spinnerDay.selectedItem.toString()

// Ensure that month and day are represented as two digits
            val formattedMonth = selectedMonth.padStart(2, '0')
            val formattedDay = selectedDay.padStart(2, '0')

            if(email.isBlank() || password.isBlank() || nickname.isBlank() || name.isBlank() || selectedYear.isBlank() || selectedMonth.isBlank() || selectedDay.isBlank()){
                showErrorDialog("모든 항목을 입력하세요.")
                return@setOnClickListener
            }
            if(password != passwordconfirm) {
                showErrorDialog("비밀번호가 일치하지 않습니다.")
                return@setOnClickListener
            }
            val displayNameCollection = Firebase.firestore.collection("Users")
            displayNameCollection
                .whereEqualTo("nickname", nickname)
                .get()
                .addOnFailureListener{e ->
                    showErrorDialog("닉네임 관련 에려 발생: ${e.message}")
                    return@addOnFailureListener
                }
                .addOnSuccessListener { querySnapshot ->
                    if(!querySnapshot.isEmpty){
                        showErrorDialog("이미 사용 중인 닉네임입니다.")
                        return@addOnSuccessListener
                    }else{
                        // Firebase Authentication을 사용하여 계정 생성
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this) { task ->
                                if (task.isSuccessful) {
                                    // 계정 생성 성공 시 사용자 정보 업데이트
                                    val user = auth.currentUser
                                    val profileUpdates = com.google.firebase.auth.userProfileChangeRequest {
                                        displayName = nickname
                                    }
                                    //사용자 정보 users에 저장
                                    val newUser = hashMapOf(
                                        "nickname" to nickname,
                                        "name" to name,
                                        "email" to email,
                                        "birthdate" to "$selectedYear 년 $formattedMonth 월 $formattedDay 일"
                                    )
                                    val newUserDocRef = Firebase.firestore.collection("Users").document()
                                    newUserDocRef
                                        .set(newUser)
                                        .addOnSuccessListener {
                                            user?.updateProfile(profileUpdates)
                                                ?.addOnCompleteListener { profileTask ->
                                                    if (profileTask.isSuccessful) {
                                                        message = "닉네임: $nickname \n 이름: $name\n 이메일: $email\n 생년월일: $selectedYear 년 $formattedMonth 월 $formattedDay 일"
                                                        showSuccessDialog()
                                                    } else {
                                                        // 사용자 정보 업데이트 실패 시 에러 다이얼로그 표시
                                                        showErrorDialog("닉네임 관련 에러 발생!")
                                                        return@addOnCompleteListener

                                                    }
                                                }
                                                ?.addOnFailureListener {
                                                    showErrorDialog("닉네임 관련 에러 발생: ${it.message}")
                                                    return@addOnFailureListener
                                                }
                                        }


                                }
                                else{
                                    if (task.exception is FirebaseAuthUserCollisionException) {
                                        showErrorDialog("이미 가입된 이메일 주소입니다.")
                                    }
                                    else{
                                        showErrorDialog("회원 가입 에러 발생: ${task.exception}")
                                    }
                                    return@addOnCompleteListener
                                }
                            }
                    }

                }
        }
    }

    private fun showSuccessDialog() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("회원 가입 성공")
        alertDialog.setMessage(message)
        alertDialog.setPositiveButton("확인") { _, _ ->
            val intent = Intent(this, MainActivity::class.java)
            val welcomeMessage = "안녕하세요, ${Firebase.auth.currentUser?.displayName} 님!"
            Toast.makeText(this, welcomeMessage, Toast.LENGTH_SHORT).show()
            startActivity(intent)
            finish()
        }
        alertDialog.show()
    }

    private fun showErrorDialog(errorMessage: String) {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("회원 가입 실패")
        alertDialog.setMessage(errorMessage)
        alertDialog.setPositiveButton("확인") { _, _ ->
        }
        alertDialog.show()
    }
}
