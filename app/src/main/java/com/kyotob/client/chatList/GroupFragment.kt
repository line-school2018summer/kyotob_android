package com.kyotob.client.chatList

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
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
import com.bumptech.glide.Glide
import com.kyotob.client.*
import com.kyotob.client.entities.FriendItem
import com.kyotob.client.repositories.user.UsersRepository
import com.kyotob.client.util.ImageDialog
import com.kyotob.client.util.createIconUpload
import com.kyotob.client.util.imageActivityResult
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
    private lateinit var recyclerView: RecyclerView
    lateinit var adapter: RecyclerAdapter
    lateinit var sharedPreferences: SharedPreferences
    private val job = Job()
    private val usersRepository = UsersRepository()

    var iconPath = "abc.png"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        sharedPreferences = activity!!.getSharedPreferences(USER_DATA_KEY, Context.MODE_PRIVATE)
        val root = inflater.inflate(R.layout.dialog_group, null)
        nameText = root.findViewById(R.id.nameText)
        iconButton = root.findViewById(R.id.iconButton)
        registerButton = root.findViewById(R.id.registerButton)
        recyclerView = root.findViewById(R.id.member_recycleview)
        recyclerView.layoutManager = LinearLayoutManager(context!!)
        iconButton.setImageResource(R.drawable.boy)

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
            showUpdateIconDialog()
        }
        return root
    }

    private fun showUpdateIconDialog() {
        val fragmentManager = fragmentManager
        val imageDialog = ImageDialog()
        imageDialog.setTargetFragment(this, 10)
        imageDialog.show(fragmentManager, "image選択")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val uri = imageActivityResult(requestCode, resultCode, data, activity!!.applicationContext)
        updateIcon(uri!!)
    }

    private fun updateIcon(uri: Uri) {
        try {
            launch (UI) {
                val part = createIconUpload(uri, activity!!)
                val response = withContext(CommonPool) { UsersRepository().uploadIcon(part).awaitResponse() }
                if (response.isSuccessful) {
                    iconPath = response.body()!!.path
                    showToast("変更されました")
                    Glide.with(this@GroupFragment).load(baseUrl+"image/download/" + iconPath).into(iconButton)
                } else {
                    showToast(response.code().toString())
                }
            }
        } catch (e: Throwable) {
            showToast(e.message!!)
        }
    }

    private suspend fun getFriendList(): List<FriendItem> {
        return withContext(CommonPool) {
            val name = sharedPreferences.getString(USER_NAME_KEY, "default")
            val token = sharedPreferences.getString(TOKEN_KEY, "default")
            usersRepository.getFriendList(name, token).await()
        }
    }

    private suspend fun clickRegisterButton() {
        val roomName = nameText.text.toString()
        val name = sharedPreferences.getString(USER_NAME_KEY, "default")
        val memberList: List<String> = adapter.itemList.filter {it.isChecked}.map{it.name} + listOf(name)
        val token = sharedPreferences.getString(TOKEN_KEY, "default")
        try {
            val response = withContext(CommonPool) {
                usersRepository.postGroupRoomRequest(token!!, roomName, memberList, iconPath).awaitResponse()
            }
            if(response.isSuccessful) {
                val intent = Intent(activity?.application, ChatListActivity::class.java)
                startActivity(intent)
                activity?.finish()
            }
        } catch (exception: Throwable) {
            if (this.isVisible) Toast.makeText(context, exception.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun showToast(message: String) {
        val toast = Toast.makeText(activity, message, Toast.LENGTH_LONG)
        toast.show()
    }

    fun setDefaultIcon() {
        iconButton.setImageResource(R.drawable.boy)
        iconPath = "abc.png"
    }
}
