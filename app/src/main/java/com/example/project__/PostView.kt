package com.example.project__

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class PostView : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var cancelDialog: Dialog
    private lateinit var changeDialog: Dialog
    var imageString = ""
    var postcount = 1
    var nowon = false

    val db: FirebaseFirestore = Firebase.firestore

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_nav_menu, menu)
        return true
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_view)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 뒤로가기 버튼 활성화
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }


        val title = findViewById<TextView>(R.id.TitleText)
        val price = findViewById<TextView>(R.id.PriceText)
        val text = findViewById<TextView>(R.id.TextText)
        val imageView = findViewById<ImageView>(R.id.imageView)
        val sellerName = findViewById<TextView>(R.id.sellerName)
        val currentUser = FirebaseAuth.getInstance().currentUser?.displayName.toString()
        val nowon = findViewById<TextView>(R.id.NowOn)
        val intent = intent
        val soldout = intent.getBooleanExtra("soldout", false)
        title.text = intent.getStringExtra("title")
        price.text = intent.getLongExtra("price", 1000).toString() + " 원"
        text.text = intent.getStringExtra("text")
        sellerName.text = intent.getStringExtra("Author")
        val condition = intent.getStringExtra("condition")
        val id = intent.getStringExtra("id")
        if(soldout){
            nowon.text = "판매완료"
        }
        else{
            nowon.text = "판매중"
        }
        when(condition){
            "새 상품" -> {
                imageView.setImageResource(R.drawable.unwrapped)
            }
            "상태 좋음" -> {
                imageView.setImageResource(R.drawable.good)
            }
            "상태 보통" ->{
                imageView.setImageResource(R.drawable.normal)
            }
            else->{
                imageView.setImageResource(R.drawable.bad)
            }
        }
        if(soldout){
            imageView.setImageResource(R.drawable.soldout)
        }
        bottomNavigationView = findViewById(R.id.navigationView)

        if (sellerName.text == currentUser)
            bottomNavigationView.visibility = View.VISIBLE
        else
            bottomNavigationView.visibility = View.GONE
        bottomNavigationView.setOnItemSelectedListener{ item ->
            when (item.itemId){
                R.id.menu_edit -> {
                    val intent = Intent(this, PostWriting::class.java)
                    intent.putExtra("edit", id)
                    intent.putExtra("editMode", true)
                    startActivity(intent)
                    true
                }
                R.id.menu_change -> {
                    changeDialog = Dialog(this)
                    changeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // 타이틀 제거
                    changeDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    changeDialog.setContentView(R.layout.change_state)

                    if (id != null) {
                        ChangeDialog(id)
                    }
                    true
                }
                R.id.menu_delete -> {
                    cancelDialog = Dialog(this)
                    cancelDialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // 타이틀 제거
                    cancelDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    cancelDialog.setContentView(R.layout.dialog)
                    if (id != null) {
                        CancelDialog(id)
                    }
                    true
                }
                else -> false
            }
            return@setOnItemSelectedListener true
        }

        val sendButton = findViewById<Button>(R.id.chatButton)
        sendButton.setOnClickListener {
            if (sellerName.text == currentUser)
                Toast.makeText(this, "자신의 글에 메시지를 보낼 수 없습니다.", Toast.LENGTH_SHORT).show()
            else {
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("author", sellerName.text)
                startActivity(intent)
            }
        }
    }

    private fun CancelDialog(id: String) {
        cancelDialog.show() // 다이얼로그 띄우기
        val NoButton = cancelDialog.findViewById<Button>(R.id.NoButton)
        NoButton.setOnClickListener {
            cancelDialog.dismiss()
        }

        cancelDialog.findViewById<Button>(R.id.YesButton).setOnClickListener {
            val itemsCollectionRef = db.collection("pocket_post")
            val postDocRef = itemsCollectionRef.document(id)
            postDocRef.delete()
            finish()
            Toast.makeText(this, "글 삭제 성공!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun ChangeDialog(id: String) {
        changeDialog.show() // 다이얼로그 띄우기
        val itemsCollectionRef = db.collection("pocket_post")
        val documentRef = itemsCollectionRef.document(id)
        val SellButton = changeDialog.findViewById<Button>(R.id.NowSell)
        val NotSellButton = changeDialog.findViewById<Button>(R.id.NotSell)
        val CancelButton = changeDialog.findViewById<Button>(R.id.Cancel)
        val imageView = findViewById<ImageView>(R.id.imageView)

        imageString = intent.getStringExtra("image").toString()

        CancelButton.setOnClickListener {
            changeDialog.dismiss()
        }

        SellButton.setOnClickListener {
            val NowOn = findViewById<TextView>(R.id.NowOn)
            if (NowOn.text != "판매중") {
                nowon = false
                val resourceName = imageString // 동적으로 변경되는 리소스 이름
                val resourceId = resources.getIdentifier(resourceName, "drawable", packageName)
                imageView.setImageResource(resourceId)
                val updates = hashMapOf<String, Any>(
                    "soldout" to false
                )
                documentRef.update(updates)
                    .addOnSuccessListener {
                        Toast.makeText(applicationContext, "상태가 변경되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error updating document", e)
                        // Handle the failure if needed
                    }
                    .addOnCompleteListener {
                        changeDialog.dismiss()
                    }
                NowOn.text = "판매중"
            }
        }

        NotSellButton.setOnClickListener {
            val NowOn = findViewById<TextView>(R.id.NowOn)
            if (NowOn.text != "판매완료") {
                NowOn.text = "판매완료"
                nowon = true
                imageView.setImageResource(R.drawable.soldout)
                val updates = hashMapOf<String, Any>(
                    "soldout" to true
                )
                documentRef.update(updates)
                    .addOnSuccessListener {
                        Toast.makeText(applicationContext, "상태가 변경되었습니다.", Toast.LENGTH_SHORT)
                            .show()
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error updating document", e)
                        // Handle the failure if needed
                    }
                    .addOnCompleteListener {
                        changeDialog.dismiss()
                    }
                NowOn.text = "판매완료"
            }
        }
    }
}