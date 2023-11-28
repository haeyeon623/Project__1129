package com.example.project__

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
// 메시지 정보(유저 이름, 메시지 내용, 보낸 시간)을 저장하는 데이터 클래스.
data class Chat(var user: String? = null, var chat: String? = null, var time: String? = null)
// 메시지 보내기 화면 에서 메시지를 보냈을 때 화면에 띄우는 리사이클러뷰 구현
class MyAdapter(private val itemList: ArrayList<Chat>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    inner class MyChatViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val chat = v.findViewById<TextView>(R.id.tv_chat_text)
        val currentTime = v.findViewById<TextView>(R.id.currentTime)
    }
    inner class OtherChatViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val user = v.findViewById<TextView>(R.id.userName)
        val chat = v.findViewById<TextView>(R.id.tv_chat_text)
        val currentTime = v.findViewById<TextView>(R.id.currentTime)
    }

    companion object {
        private const val MY_CHAT = 1
        private const val OTHER_CHAT = 2
    }
    override fun getItemViewType(position: Int): Int { // 현재 유저 정보를 받아와서 유저정보가 현재 유저와 일치하면 오른쪽에 메시지를 띄우고, 아니면 오른쪽에 메시지 띄우기
        val currentUser = FirebaseAuth.getInstance().currentUser?.displayName.toString()
        return if (currentUser == itemList[position].user) MY_CHAT
        else OTHER_CHAT
        //return super.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {    // 왼쪽 오른쪽 각각의 레이아웃 inflate
            MY_CHAT -> {
                val itemView = LayoutInflater.from(parent.context).inflate(R.layout.text_layout, parent, false)
                MyChatViewHolder(itemView)
            }
            OTHER_CHAT -> {
                val itemView = LayoutInflater.from(parent.context).inflate(R.layout.text_layout_receive, parent, false)
                OtherChatViewHolder(itemView)
            }
            else -> throw IllegalArgumentException("Invalid view type") // 예외 처리
        }
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {     // 각각의 레이아웃에서 정보를 textview에 출력
            is MyChatViewHolder -> { // 뷰 타입 A에 대한 데이터 바인딩
                holder.chat.text = itemList[position].chat
                holder.currentTime.text = itemList[position].time
            }
            is OtherChatViewHolder -> { // 뷰 타입 B에 대한 데이터 바인딩
                holder.user.text = itemList[position].user
                holder.chat.text = itemList[position].chat
                holder.currentTime.text = itemList[position].time
            }
        }
    }
    override fun getItemCount(): Int { // 아이템 개수 리턴
        return itemList.count()
    }
}

class ChatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_layout)
        // 상단 툴바 설정, 가운데에 메시지를 보낼 상대방의 이름이 출력
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayShowCustomEnabled(true)
        actionBar?.setDisplayShowTitleEnabled(false)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        val textUserName = findViewById<TextView>(R.id.textViewUserName)
        val currentUser = FirebaseAuth.getInstance().currentUser?.displayName.toString()

        val receivedData = intent.getStringExtra("author") ?: "" // 상대방의 이름을 intent로 받아와서 textview에 출력
        if (receivedData != "") { //
            textUserName.text = receivedData
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        val itemList = ArrayList<Chat>()
        val boardAdapter = MyAdapter(itemList)
        // 메시지 정보를 firestore에 저장할 때
        val documentRef = Firebase.firestore.collection("Chat").document(currentUser).collection(receivedData)
        val documentRef2 = Firebase.firestore.collection("Chat").document(receivedData).collection(currentUser)

        recyclerView.adapter = boardAdapter
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        documentRef.addSnapshotListener { snapshot, e ->
            if (e != null) { // 오류 처리
                Log.w(TAG, "Listen failed.", e)
            }
            snapshot?.documentChanges?.forEach { change ->
                when (change.type) {
                    DocumentChange.Type.ADDED -> { // document 추가될 때
                        itemList.clear()
                        for (document in snapshot) {
                            val item = Chat(document["user"] as String?,
                                document["chat"] as String?,
                                document["time"] as String?)
                            itemList.add(item)
                            boardAdapter.notifyItemInserted(itemList.size - 1)
                            recyclerView.scrollToPosition(itemList.size - 1)
                        }
                        boardAdapter.notifyDataSetChanged()
                    }
                    // 다른 DocumentChange.Type 처리
                    else -> {}
                }
            }
        }
        documentRef2.addSnapshotListener { snapshot, e ->
            if (e != null) { // 오류 처리
                Log.w(TAG, "Listen failed.", e)
            }
            snapshot?.documentChanges?.forEach { change ->
                when (change.type) {
                    DocumentChange.Type.ADDED -> { // document 추가될 때
                        itemList.clear()
                        for (document in snapshot) {
                            val item = Chat(document["user"] as String?,
                                document["chat"] as String?,
                                document["time"] as String?)
                            itemList.add(item)
                            boardAdapter.notifyItemInserted(itemList.size - 1)
                            recyclerView.scrollToPosition(itemList.size - 1)
                        }
                        boardAdapter.notifyDataSetChanged()
                    }
                    // 다른 DocumentChange.Type 처리
                    else -> {}
                }
            }
        }

        findViewById<ImageButton>(R.id.imageButtonSend).setOnClickListener {
            val currentTimeMillis = System.currentTimeMillis() / 1000
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val currentDate = Date(currentTimeMillis * 1000) // SimpleDateFormat 사용 하여 날짜와 시간을 포맷
            val formattedDate = dateFormat.format(currentDate)
            val currentTime = formattedDate.toString()

            val chat = findViewById<EditText>(R.id.editTextChat).text
            if (chat.toString().isNotEmpty()) {
                itemList.add(Chat(currentUser, chat.toString(), currentTime)) // 화면에 추가
                storeChatData(chat.toString(), currentTime) // 파이어 스토어 데이터 추가
                boardAdapter.notifyItemInserted(itemList.size - 1)
                recyclerView.scrollToPosition(itemList.size - 1)
                chat.clear() // EditText 비우기
            }
        }
    }
    private fun storeChatData(chat: String, currentTime: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser?.displayName.toString()
        val fbStore = Firebase.firestore
        val chatInfo = Chat()
        val chatListInfo = ChatView()
        val receivedData = intent.getStringExtra("author") ?: ""

        chatInfo.user = currentUser
        chatInfo.chat = chat
        chatInfo.time = currentTime
        chatListInfo.text = chat
        chatListInfo.name = receivedData
        chatListInfo.time = currentTime

        fbStore.collection("Chat").document(currentUser).collection(receivedData).document(System.currentTimeMillis().toString()).set(chatInfo)
        fbStore.collection("Chat").document(receivedData).collection(currentUser).document(System.currentTimeMillis().toString()).set(chatInfo)
        fbStore.collection("ChatList: ${receivedData}").document(currentUser).set(chatListInfo)
        fbStore.collection("ChatList: ${currentUser}").document(receivedData).set(chatListInfo)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> { // 백 버튼 구현 -> 이전 액티비티(프래그먼트)로 돌아가기
                finish()
            }
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }
}