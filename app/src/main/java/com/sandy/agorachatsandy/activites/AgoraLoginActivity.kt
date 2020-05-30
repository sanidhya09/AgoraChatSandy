package com.sandy.agorachatsandy.activites

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sandy.agorachatsandy.R
import com.sandy.agorachatsandy.model.AgoraUser
import com.sandy.agorachatsandy.rtm.AGApplication
import com.sandy.agorachatsandy.utils.MessageUtil
import io.agora.rtm.ErrorInfo
import io.agora.rtm.ResultCallback
import io.agora.rtm.RtmClient
import kotlinx.android.synthetic.main.agora_activity_login.*
import kotlin.random.Random

class AgoraLoginActivity : AppCompatActivity(), View.OnClickListener {

    private val mRtmClient: RtmClient by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        val mChatManager = AGApplication.the().chatManager
        mChatManager.rtmClient
    }
    private val mRandom: Random by lazy {
        Random(System.nanoTime())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.agora_activity_login)
        sign_in_button.setOnClickListener(this)
        signOutButton.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.sign_in_button -> signIn()
            R.id.signOutButton -> signOut()
        }
    }

    private fun signIn() {
        val user = AgoraUser("100${mRandom.nextInt()}")
        user.displayName = "S_${mRandom.nextInt()}"
        val textView = findViewById<TextView>(R.id.status_textview)
        textView.text = user.displayName
        mRtmClient.login(null, user.displayName, object : ResultCallback<Void?> {
            override fun onSuccess(aVoid: Void?) {
                runOnUiThread {
                    val intent = Intent(this@AgoraLoginActivity, AgoraVideoCallActivity::class.java)
                    intent.putExtra("User", user)
                    intent.putExtra("Channel", "MbChannel1")
                    startActivity(intent)
                }
            }

            override fun onFailure(errorInfo: ErrorInfo) {
                runOnUiThread { Toast.makeText(this@AgoraLoginActivity, getString(R.string.login_failed) + " " + errorInfo.errorDescription, Toast.LENGTH_SHORT).show() }
            }
        })
    }

    fun signOut() {
        val textView = findViewById<TextView>(R.id.status_textview)
        textView.text = ""
        mRtmClient.logout(null)
        MessageUtil.cleanMessageListBeanList()
    }

}