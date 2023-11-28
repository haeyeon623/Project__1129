package com.example.project__

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import org.w3c.dom.Text

data class Items(
    var id: String? = null,
    var author:String? = null,//판매자
    var body: String? = null,//내용
    var condition:String? = null,//제품 상태
    var date:String? = null,//올린 시간
    var price: Long? = null,//가격
    var soldout: Boolean? = null,//팔렸는지
    var title: String? = null//제목
)

class MainAdapter(private val items: ArrayList<Items>) : RecyclerView.Adapter<MainAdapter.ViewHolderItems>(){
    inner class ViewHolderItems(v: View) : RecyclerView.ViewHolder(v) {
        val title = v.findViewById<TextView>(R.id.titleTextView)
        val price = v.findViewById<TextView>(R.id.priceTextView)
        val soldout = v.findViewById<TextView>(R.id.SoldoutTextView)
        val date = v.findViewById<TextView>(R.id.DateTextView)
        val image = v.findViewById<ImageView>(R.id.GoodsImageView)
        val id = v.findViewById<TextView>(R.id.documentId)
        init {
            v.setOnClickListener {
                val position = adapterPosition
                val context = itemView.context
                val id = items[position].id
                val author = items[position].author
                val body = items[position].body
                val price = items[position].price
                val title = items[position].title
                val condition = items[position].condition
                val soldout = items[position].soldout
                var image = ""

                if (condition == "새 상품") image = "unwrapped"
                else if (condition == "상태 좋음") image = "good"
                else if (condition == "상태 보통") image = "normal"
                else if (condition == "상태 안 좋음") image = "bad"

                val intent = Intent(context, PostView::class.java)
                intent.putExtra("id", id)
                intent.putExtra("Author", author)
                intent.putExtra("text", body)
                intent.putExtra("price", price)
                intent.putExtra("title", title)
                intent.putExtra("image", image)
                intent.putExtra("soldout", soldout)
                intent.putExtra("condition", condition)

                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderItems {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.view_item, parent, false)
        return ViewHolderItems(view)
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    override fun onBindViewHolder(holder: ViewHolderItems, position: Int) {
        holder.title.text = items[position].title
        holder.price.text = items[position].price.toString() + "원"
        holder.date.text = items[position].date
        holder.id.text = items[position].id

        holder.soldout.text = items[position].soldout.toString()
        holder.date.text = items[position].date
        when (items[position].condition) {
            "새 상품" -> holder.image.setImageResource(R.drawable.unwrapped)
            "상태 좋음" -> holder.image.setImageResource(R.drawable.good)
            "상태 보통" -> holder.image.setImageResource(R.drawable.normal)
            "상태 안 좋음" -> holder.image.setImageResource(R.drawable.bad)
        }
        if(items[position].soldout == true) {
            holder.soldout.text = "판매완료"
            holder.image.setImageResource(R.drawable.soldout)
        }
        else {
            holder.soldout.text = "판매중"
        }
    }
}

data class ItemsList(var author: String? = null)

class MyMainAdapter(private val items: ArrayList<ItemsList>) : RecyclerView.Adapter<MyMainAdapter.MyViewHolder>() {
    inner class MyViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val author = v.findViewById<TextView>(R.id.textView3)
        val enterChat = v.findViewById<Button>(R.id.enterChatReal)
        init {
            val currentUser = FirebaseAuth.getInstance().currentUser?.displayName.toString()
            enterChat.setOnClickListener {
                val position = adapterPosition
                val context = itemView.context
                if (position != RecyclerView.NO_POSITION) {
                    // 클릭한 아이템의 위치(position)에 대한 작업 수행
                    val author = items[position].author.toString() // 클릭 이벤트 처리
                    if (author == currentUser) {
                        Toast.makeText(context, "자신의 글에 채팅할 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        val intent = Intent(context, ChatActivity::class.java)
                        intent.putExtra("author", author)
                        context.startActivity(intent)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.ltems_list, parent, false)
        return MyViewHolder(view)
    }
    override fun getItemCount(): Int {
        return items.count()
    }
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.author.text = items[position].author
    }
}

data class ChatView(var name: String? = null, var text: String? = null, var time: String? = null)

class MyListViewAdapter(private val items: ArrayList<ChatView>) : RecyclerView.Adapter<MyListViewAdapter.ListChatViewHolder>() {
    inner class ListChatViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val listUserName = v.findViewById<TextView>(R.id.chatListName)
        val chatInfo = v.findViewById<TextView>(R.id.chatListChat)
        val time = v.findViewById<TextView>(R.id.textCurrentTime)
        init {
            v.setOnClickListener {
                val position = adapterPosition
                val context = itemView.context
                val author = items[position].name.toString()
                val intent = Intent(context, ChatActivity::class.java)
                intent.putExtra("author", author)
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListChatViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.chatting_list, parent, false)
        return ListChatViewHolder(view)
    }
    override fun onBindViewHolder(holder: ListChatViewHolder, position: Int) {
        holder.listUserName.text = items[position].name
        holder.chatInfo.text = items[position].text
        holder.time.text = items[position].time
    }
    override fun getItemCount(): Int {
        return items.count()
    }
}