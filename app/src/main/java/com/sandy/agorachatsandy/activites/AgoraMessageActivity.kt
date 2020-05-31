package com.sandy.agorachatsandy.activites

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sandy.agorachatsandy.R
import com.sandy.agorachatsandy.activites.AgoraMessageActivity
import com.sandy.agorachatsandy.adapter.AgoraMessageAdapter
import com.sandy.agorachatsandy.model.AgoraMessageBean
import com.sandy.agorachatsandy.model.AgoraUser
import com.sandy.agorachatsandy.rtm.AGApplication
import com.sandy.agorachatsandy.rtm.AgoraChatManager
import com.sandy.agorachatsandy.utils.MessageUtil
import io.agora.rtm.*
import kotlinx.android.synthetic.main.agora_activity_message.*
import java.util.*

class AgoraMessageActivity : AppCompatActivity() {
    private val TAG = AgoraMessageActivity::class.java.simpleName
    private val mMessageBeanList: MutableList<AgoraMessageBean> = ArrayList()
    private var mMessageAdapter: AgoraMessageAdapter? = null
    private var user: AgoraUser? = null
    private var mChannelName = ""
    private var mChannelMemberCount = 1
    private var targetName = ""
    private var mChatManager: AgoraChatManager? = null

    private val mRtmClient: RtmClient by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        val mChatManager = AGApplication.the().chatManager
        mChatManager.rtmClient
    }
    private var mClientListener: RtmClientListener? = null
    private var mRtmChannel: RtmChannel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.agora_activity_message)
        val ab = supportActionBar
        ab?.hide()
        init()
    }

    private fun init() {
        user = intent.getParcelableExtra(MessageUtil.INTENT_EXTRA_USER_ID)
        targetName = intent.getStringExtra(MessageUtil.INTENT_EXTRA_TARGET_NAME)
        mChatManager = AGApplication.the().chatManager
        mClientListener = MyRtmClientListener()
        mChatManager?.registerListener(mClientListener)

        mChannelName = targetName
        mChannelMemberCount = 1
        message_title?.text = "$mChannelName($mChannelMemberCount)"
        createAndJoinChannel()
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = RecyclerView.VERTICAL
        mMessageAdapter = AgoraMessageAdapter(this, mMessageBeanList)
        message_list.layoutManager = layoutManager
        message_list.adapter = mMessageAdapter
    }

    /**
     * API CALL: create and join channel
     */
    private fun createAndJoinChannel() {
        // step 1: create a channel instance
        mRtmChannel = mRtmClient.createChannel(mChannelName, MyChannelListener())
        if (mRtmChannel == null) {
            showToast(getString(R.string.join_channel_failed))
            finish()
            return
        }
        Log.e("channel", mRtmChannel.toString() + "")

        // step 2: join the channel
        mRtmChannel!!.join(object : ResultCallback<Void?> {
            override fun onSuccess(responseInfo: Void?) {
                Log.i(TAG, "join channel success")
                channelMemberList
            }

            override fun onFailure(errorInfo: ErrorInfo) {
                Log.e(TAG, "join channel failed")
                runOnUiThread {
                    showToast(getString(R.string.join_channel_failed))
                    finish()
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        leaveChannel()
        mChatManager!!.unregisterListener(mClientListener)
    }

    /**
     * API CALL: leave and release channel
     */
    private fun leaveChannel() {
        if (mRtmChannel != null) {
            mRtmChannel!!.leave(object : ResultCallback<Void?> {
                override fun onSuccess(aVoid: Void?) {}
                override fun onFailure(errorInfo: ErrorInfo) {
                    showToast("leve channel failed")
                }
            })
            mRtmChannel!!.release()
            mRtmChannel = null
        }
    }

    /**
     * API CALL: get channel member list
     */
    private val channelMemberList: Unit
        private get() {
            mRtmChannel!!.getMembers(object : ResultCallback<List<RtmChannelMember?>> {
                override fun onSuccess(responseInfo: List<RtmChannelMember?>) {
                    runOnUiThread {
                        mChannelMemberCount = responseInfo.size
                        refreshChannelTitle()
                    }
                }

                override fun onFailure(errorInfo: ErrorInfo) {
                    Log.e(TAG, "failed to get channel members, err: " + errorInfo.errorCode)
                }
            })
        }

    fun onClickSend(v: View?) {
        val msg = message_edittiext!!.text.toString()
        if (msg != "") {
            val messageBean = AgoraMessageBean(user!!.displayName, msg, true)
            mMessageBeanList.add(messageBean)
            mMessageAdapter!!.notifyItemRangeChanged(mMessageBeanList.size, 1)
            message_list!!.scrollToPosition(mMessageBeanList.size - 1)
            sendChannelMessage(msg)
        }
        message_edittiext!!.setText("")
    }

    private fun showToast(text: String) {
        Toast.makeText(this@AgoraMessageActivity, text, Toast.LENGTH_SHORT).show()
    }

    fun onClickFinish(v: View?) {
        finish()
    }

    /**
     * API CALLBACK: rtm channel event listener
     */
    internal inner class MyChannelListener : RtmChannelListener {
        override fun onMemberCountUpdated(i: Int) {}
        override fun onAttributesUpdated(list: List<RtmChannelAttribute>) {}
        override fun onMessageReceived(message: RtmMessage, fromMember: RtmChannelMember) {
            runOnUiThread {
                val account = fromMember.userId
                val msg = message.text
                Log.i(TAG, "onMessageReceived account = $account msg = $msg")
                val messageBean = AgoraMessageBean(account, msg, false)
                messageBean.background = getMessageColor(account)
                mMessageBeanList.add(messageBean)
                mMessageAdapter!!.notifyItemRangeChanged(mMessageBeanList.size, 1)
                message_list!!.scrollToPosition(mMessageBeanList.size - 1)
            }
        }

        override fun onMemberJoined(member: RtmChannelMember) {
            runOnUiThread {
                mChannelMemberCount++
                refreshChannelTitle()
            }
        }

        override fun onMemberLeft(member: RtmChannelMember) {
            runOnUiThread {
                mChannelMemberCount--
                refreshChannelTitle()
            }
        }
    }

    /**
     * API CALL: send message to a channel
     */
    private fun sendChannelMessage(content: String) {
        // step 1: create a message
        val message = mRtmClient.createMessage()
        message.text = content
        Log.e("channel", mRtmChannel.toString() + "")

        // step 2: send message to channel
        mRtmChannel!!.sendMessage(message, object : ResultCallback<Void?> {
            override fun onSuccess(aVoid: Void?) {}
            override fun onFailure(errorInfo: ErrorInfo) {
                // refer to RtmStatusCode.ChannelMessageState for the message state
                val errorCode = errorInfo.errorCode
                runOnUiThread {
                    when (errorCode) {
                        RtmStatusCode.ChannelMessageError.CHANNEL_MESSAGE_ERR_TIMEOUT, RtmStatusCode.ChannelMessageError.CHANNEL_MESSAGE_ERR_FAILURE -> showToast(getString(R.string.send_msg_failed))
                    }
                }
            }
        })
    }

    private fun getMessageColor(account: String): Int {
        for (i in mMessageBeanList.indices) {
            if (account == mMessageBeanList[i].account) {
                return mMessageBeanList[i].background
            }
        }
        return MessageUtil.COLOR_ARRAY[MessageUtil.RANDOM.nextInt(MessageUtil.COLOR_ARRAY.size)]
    }

    private fun refreshChannelTitle() {
        val titleFormat = getString(R.string.channel_title)
        val title = String.format(titleFormat, mChannelName, mChannelMemberCount)
        message_title!!.text = title
    }

    internal inner class MyRtmClientListener : RtmClientListener {
        override fun onPeersOnlineStatusChanged(map: Map<String, Int>) {}
        override fun onConnectionStateChanged(state: Int, reason: Int) {
            runOnUiThread {
                when (state) {
                    RtmStatusCode.ConnectionState.CONNECTION_STATE_RECONNECTING -> showToast(getString(R.string.reconnecting))
                    RtmStatusCode.ConnectionState.CONNECTION_STATE_ABORTED -> {
                        showToast(getString(R.string.account_offline))
                        setResult(MessageUtil.ACTIVITY_RESULT_CONN_ABORTED)
                        finish()
                    }
                }
            }
        }

        override fun onMessageReceived(message: RtmMessage, peerId: String) {
            runOnUiThread {
                val content = message.text
                if (peerId == "") {
                    val messageBean = AgoraMessageBean(peerId, content, false)
                    messageBean.background = getMessageColor(peerId)
                    mMessageBeanList.add(messageBean)
                    mMessageAdapter!!.notifyItemRangeChanged(mMessageBeanList.size, 1)
                    message_list!!.scrollToPosition(mMessageBeanList.size - 1)
                } else {
                    MessageUtil.addMessageBean(peerId, content)
                }
            }
        }

        override fun onTokenExpired() {}
    }
}