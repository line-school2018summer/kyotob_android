package com.kyotob.client.chatList

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import com.kyotob.client.R
import com.kyotob.client.entities.FriendItem
import com.kyotob.client.repositories.user.UsersRepository
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import ru.gildor.coroutines.retrofit.await
import ru.gildor.coroutines.retrofit.awaitResponse
import java.net.ConnectException


data class FriendItemForView(
        val name: String,
        val screenName: String,
        var isChecked: Boolean = false
)

class GroupFragment: Fragment() {

    lateinit var nameText: EditText
    lateinit var iconButton: ImageButton
    lateinit var registerButton: Button
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: RecyclerAdapter
    val job = Job()
    val usersRepository = UsersRepository()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.dialog_group, null)
        nameText = root.findViewById(R.id.nameText)
        iconButton = root.findViewById(R.id.iconButton)
        registerButton = root.findViewById(R.id.registerButton)
        recyclerView = root.findViewById(R.id.member_recycleview)
        recyclerView.layoutManager = LinearLayoutManager(context!!)

        //friendList
        launch(UI, parent = job) {
            try {
                val friendList = getFriendList()
                val friendForViewList = friendList.map { FriendItemForView(it.friendName, it.friendScreenName) }
                adapter = RecyclerAdapter(context!!, friendForViewList)
                recyclerView.adapter = adapter
                registerButton.setOnClickListener {
                    launch(UI, parent = job) {
                        clickRegisterButton()
                    }
                }
            } catch (exception: ConnectException) {
                if (this@GroupFragment.isVisible)Toast.makeText(context, exception.message, Toast.LENGTH_LONG).show()
            }
        }

        iconButton.setOnClickListener {
            //todo: icon
        }
        return root
    }

    suspend fun getFriendList(): List<FriendItem> {
        return withContext(CommonPool) {
            usersRepository.getFriendList("test", "bar").await()
        }
    }

    suspend fun clickRegisterButton() {
        val roomName = nameText.text.toString()
        val memberList: List<String> = adapter.itemList.filter {it.isChecked}.map{it.name} + listOf("test")
        val token = "bar"
        try {
            val response = withContext(CommonPool) {
                usersRepository.postGroupRoomRequest(token, roomName, memberList).awaitResponse()
            }
            if(response.isSuccessful) {
                val intent = Intent(activity?.application, ChatListActivity::class.java)
                startActivity(intent)
                activity?.finish()
            }
        } catch (exception: ConnectException) {
            if (this.isVisible) Toast.makeText(context, exception.message, Toast.LENGTH_LONG).show()
        }
    }
}