package com.example.project__

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.ContentValues.TAG
import java.text.SimpleDateFormat
import java.util.Date
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlin.properties.Delegates

class PostWriting : AppCompatActivity() {
    private lateinit var edittitle: EditText
    private lateinit var editprice: EditText
    private lateinit var edittext: EditText
    private lateinit var imagespinner: String
    private lateinit var imagewriting: String
    private var isEditMode by Delegates.notNull<Boolean>()

    var price = 0
    var postcount = 2
    //private lateinit var sellername: EditText
    var identify = ""

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_writing_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val postID = intent.getStringExtra("edit").toString()
        edittitle = findViewById(R.id.EditTitle)
        editprice = findViewById(R.id.EditPrice)
        edittext = findViewById(R.id.EditText)
        val imageView = findViewById<ImageView>(R.id.imageView)
        imageView.visibility = View.VISIBLE
        isEditMode = intent.getBooleanExtra("editMode", false)
        val checktitle_empty = edittitle.text.toString().trim()
        val checkprice_empty = editprice.text.toString().trim()
        val checktext_empty = edittext.text.toString().trim()
        val intent = Intent(this, PostView::class.java)
        val db: FirebaseFirestore = Firebase.firestore

        fun getCurrentDateTime(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            val currentDateAndTime: String = sdf.format(Date())
            return currentDateAndTime
        }

        fun registerPost() {
            val itemsCollectionRef = db.collection("pocket_post")
            val idText = findViewById<TextView>(R.id.idText)
            val currentUser = FirebaseAuth.getInstance().currentUser?.displayName.toString()
            val listInfo = Items()
            val documentName = "post" + System.currentTimeMillis()
            idText.text = documentName

            // 게시물 정보 설정
            val itemMap = hashMapOf(
                "price" to checkprice_empty,
                "body" to checktext_empty,
                "title" to checktitle_empty,
                "id" to documentName,
                "date" to getCurrentDateTime(),
                "condition" to imagespinner,
                "soldout" to false
            )

            listInfo.date = itemMap["date"] as String?
            listInfo.soldout = itemMap["soldout"] as Boolean?
            listInfo.condition = itemMap["condition"] as String?
            listInfo.author = currentUser
            listInfo.body = itemMap["body"] as String?
            listInfo.price = itemMap["price"].toString().toLong()
            listInfo.title = itemMap["title"] as String?
            listInfo.id = itemMap["id"] as String?

            // Firestore에 데이터 추가
            itemsCollectionRef.document(documentName).set(listInfo)
                .addOnSuccessListener {
                    Toast.makeText(this, "글 등록 성공!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this,MainActivity::class.java))
                }
        }
        fun updatePost(postID : String) {
            // postId에 해당하는 문서 가져오기
            val itemsCollectionRef = db.collection("pocket_post")
            val postDocRef = itemsCollectionRef.document(postID)
            // 업데이트할 데이터
            val updateData = hashMapOf<String, Any>(
                "price" to editprice.text.toString().toLong(),
                "body" to edittext.text.toString(),
                "title" to edittitle.text.toString(),
                "date" to getCurrentDateTime(),
                "condition" to imagespinner,
                "soldout" to false
            )

            postDocRef.update(updateData).addOnSuccessListener {
                Toast.makeText(this, "글 수정 성공!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this,MainActivity::class.java))
            }
                .addOnFailureListener { e ->
                    // 업데이트 실패 시 에러 처리
                    Log.e(TAG, "Error updating document", e)
                }
        }

        when (item.itemId) {
            R.id.action_register -> {

                val myInstance2 = MyClass()
                val propertyValue = myInstance2.getMyProperty()

                val currentUser = FirebaseAuth.getInstance().currentUser?.displayName.toString()
                intent.putExtra("title", edittitle.text.toString())
                intent.putExtra("price", editprice.text.toString().toLong())
                intent.putExtra("text", edittext.text.toString())
                intent.putExtra("value", propertyValue)
                intent.putExtra("condition", imagespinner)
                intent.putExtra("image", imagewriting)
                intent.putExtra("Author", currentUser)
                intent.putExtra("id", intent.getStringExtra("edit"))

                fun isInteger(value: String): Boolean {
                    return try {
                        value.toInt()
                        true
                    } catch (e: NumberFormatException) {
                        false
                    }
                }
                if(!isInteger(checkprice_empty)) {
                    Toast.makeText(applicationContext, "가격란에는 정수를 입력해주세요.", Toast.LENGTH_SHORT).show()
                    return true
                }

                if (checktitle_empty.isEmpty()) {
                    Toast.makeText(applicationContext, "제목란이 비어있습니다.", Toast.LENGTH_SHORT).show()
                    return true
                } else if (checkprice_empty.isEmpty()) {
                    Toast.makeText(applicationContext, "가격란이 비어있습니다.", Toast.LENGTH_SHORT).show()
                    return true
                } else if (checktext_empty.isEmpty()) {
                    Toast.makeText(applicationContext, "내용란이 비어있습니다.", Toast.LENGTH_SHORT).show()
                    return true
                }
                if (isEditMode) {
                    updatePost(postID)
                }
                else{
                    registerPost()
                }
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_writing)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        val db = Firebase.firestore
        val itemsCollectionRef = db.collection("pocket_post")
        setSupportActionBar(toolbar)

        edittitle = findViewById(R.id.EditTitle)
        editprice = findViewById(R.id.EditPrice)
        edittext = findViewById(R.id.EditText)

        val spinner = findViewById<Spinner>(R.id.categorySpinner)
        val imageView = findViewById<ImageView>(R.id.imageView)
        val postID = intent.getStringExtra("edit")
        isEditMode = intent.getBooleanExtra("editMode", true)
        @SuppressLint("DiscouragedApi")
        fun loadPostData(postID: String) {
            itemsCollectionRef.document(postID)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val title = documentSnapshot.getString("title")
                        val price = documentSnapshot.getLong("price").toString()
                        val text = documentSnapshot.getString("body")
                        val condition= documentSnapshot.getString("condition")
                        when (condition) {
                            "새 상품" -> {
                                imagewriting = "unwrapped"
                                val resourceId = resources.getIdentifier(imagewriting, "drawable", packageName)
                                imageView.visibility = View.VISIBLE
                                imageView.setImageResource(resourceId)
                                spinner.setSelection(2)
                            }
                            "상태 좋음" -> {
                                imagewriting = "good"
                                val resourceId = resources.getIdentifier(imagewriting, "drawable", packageName)
                                imageView.visibility = View.VISIBLE
                                imageView.setImageResource(resourceId)

                                spinner.setSelection(0)
                            }
                            "상태 보통" -> {
                                imagewriting = "normal"
                                val resourceId = resources.getIdentifier(imagewriting, "drawable", packageName)
                                spinner.setSelection(1)
                                imageView.visibility = View.VISIBLE
                                imageView.setImageResource(resourceId)


                            }
                            else -> {
                                imagewriting = "bad"
                                val resourceId = resources.getIdentifier(imagewriting, "drawable", packageName)
                                imageView.visibility = View.VISIBLE
                                imageView.setImageResource(resourceId)
                                spinner.setSelection(3)
                            }
                        }

                        edittitle.setText(title)
                        editprice.setText(price)
                        edittext.setText(text)
                        imageView.visibility = View.VISIBLE

                        // 기타 필요한 작업 수행
                    } else {
                        // 문서가 존재하지 않는 경우, 예외 처리 등을 수행
                    }
                }
                .addOnFailureListener { exception ->
                    // 데이터를 가져오는 중 에러가 발생한 경우, 예외 처리 등을 수행
                    Log.w(ContentValues.TAG, "Error getting document", exception)
                }
        }
        if (isEditMode) {
            if (postID != null) {
                loadPostData(postID)
            }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // 뒤로가기 버튼
        toolbar.setNavigationOnClickListener {
            val editMode = intent.getBooleanExtra("editMode", false)
            if(editMode){
                val intent = Intent(this, PostView::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
            }
            else{
                val intent = Intent(this, MainActivity::class.java)
                finish()
                startActivity(intent)
            }
        }

        val spinnerItems: Array<String> = resources.getStringArray(R.array.categories)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerItems)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val myInstance = MyClass()
                val selectedItem = spinnerItems[position]
                when (selectedItem) {
                    spinnerItems[2] -> {
                        imageView.setImageResource(R.drawable.unwrapped)
                        imagewriting = "unwrapped"
                        imagespinner = "새 상품"
                    }
                    spinnerItems[0] -> {
                        imageView.setImageResource(R.drawable.good)
                        imagewriting = "good"
                        imagespinner = "상태 좋음"
                    }
                    spinnerItems[1] -> {
                        imageView.setImageResource(R.drawable.normal)
                        imagewriting = "normal"
                        imagespinner = "상태 보통"
                    }
                    spinnerItems[3] -> {
                        imageView.setImageResource(R.drawable.bad)
                        imagewriting = "bad"
                        imagespinner = "상태 안 좋음"
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                imageView.setImageResource(R.drawable.normal)
                imagewriting = "normal"
                imagespinner = "상태 보통"
            }
        }
    }

    class MyClass {
        private var myProperty: String = ""

        fun getMyProperty(): String {
            return myProperty
        }

        fun setMyProperty(newValue: String) {
            myProperty = newValue
        }
    }
}