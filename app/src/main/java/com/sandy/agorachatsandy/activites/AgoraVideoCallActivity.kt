package com.sandy.agorachatsandy.activites

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.view.ViewStub
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sandy.agorachatsandy.R
import com.sandy.agorachatsandy.activites.AgoraVideoCallActivity
import com.sandy.agorachatsandy.layout.AgoraSmallVideoViewAdapter
import com.sandy.agorachatsandy.layout.AgoraSmallVideoViewDecoration
import com.sandy.agorachatsandy.model.AgoraUser
import com.sandy.agorachatsandy.ui.RecyclerItemClickListener
import com.sandy.agorachatsandy.ui.RtlLinearLayoutManager
import com.sandy.agorachatsandy.utils.MessageUtil
import io.agora.rtc.Constants
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas
import kotlinx.android.synthetic.main.agora_activity_video_call.*
import java.util.*

class AgoraVideoCallActivity : AppCompatActivity() {

    private var channelName: String? = null
    private var user: AgoraUser? = null
    var mLayoutType = LAYOUT_TYPE_DEFAULT
    private var mRtcEngine: RtcEngine? = null
    private var isCalling = true
    private var isMuted = false
    private val mIsLandscape = false
    private var mSmallVideoViewDock: RelativeLayout? = null

    private val mUidsList = HashMap<Int, SurfaceView?>()
    private var mSmallVideoViewAdapter: AgoraSmallVideoViewAdapter? = null

    private val mRtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
        // Listen for the onJoinChannelSuccess callback.
        // This callback occurs when the local user successfully joins the channel.
        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
            runOnUiThread {
                Toast.makeText(this@AgoraVideoCallActivity, "User: $uid join!", Toast.LENGTH_LONG).show()
                user!!.agoraUid = uid
                val localView = mUidsList.remove(0)
                mUidsList[uid] = localView
            }
        }

        // Listen for the onFirstRemoteVideoDecoded callback.
        // This callback occurs when the first video frame of a remote user is received and decoded after the remote user successfully joins the channel.
        // You can call the setupRemoteVideo method in this callback to set up the remote video view.
        override fun onFirstRemoteVideoFrame(uid: Int, width: Int, height: Int, elapsed: Int) {
            super.onFirstRemoteVideoFrame(uid, width, height, elapsed)
            runOnUiThread { setupRemoteVideo(uid) }
        }

        // Listen for the onUserOffline callback.
        // This callback occurs when the remote user leaves the channel or drops offline.
        override fun onUserOffline(uid: Int, reason: Int) {
            runOnUiThread {
                Toast.makeText(this@AgoraVideoCallActivity, "User: $uid left the room.", Toast.LENGTH_LONG).show()
                onRemoteUserLeft(uid)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        val ab = supportActionBar
        ab?.hide()
        setContentView(R.layout.agora_activity_video_call)
        channelName = intent?.extras?.getString("Channel")
        user = intent?.extras?.getParcelable("User")
        initUI()
        initEngineAndJoinChannel()
    }

    private fun initUI() {
        grid_video_view_container.setItemEventHandler(object : RecyclerItemClickListener.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                //can add single click listener logic
            }

            override fun onItemLongClick(view: View, position: Int) {
                //can add long click listener logic
            }

            override fun onItemDoubleClick(view: View, position: Int) {
                onBigVideoViewDoubleClicked(view, position)
            }
        })
    }

    private fun initEngineAndJoinChannel() {
        initializeEngine()
        setupLocalVideo()
        joinChannel()
    }

    private fun initializeEngine() {
        mRtcEngine = try {
            RtcEngine.create(baseContext, getString(R.string.agora_app_id), mRtcEventHandler)
        } catch (e: Exception) {
            Log.e(TAG, Log.getStackTraceString(e))
            throw RuntimeException(""" NEED TO check rtc sdk init fatal error ${Log.getStackTraceString(e)} """.trimIndent())
        }
    }

    private fun setupLocalVideo() {
        runOnUiThread {
            mRtcEngine!!.enableVideo()
            mRtcEngine!!.enableInEarMonitoring(true)
            mRtcEngine!!.setInEarMonitoringVolume(80)
            val surfaceView = RtcEngine.CreateRendererView(baseContext)
            mRtcEngine!!.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0))
            surfaceView.setZOrderOnTop(false)
            surfaceView.setZOrderMediaOverlay(false)
            mUidsList[0] = surfaceView
            grid_video_view_container!!.initViewContainer(this@AgoraVideoCallActivity, 0, mUidsList, mIsLandscape)
        }
    }

    private fun joinChannel() {
        // Join a channel with a token, token can be null.
        mRtcEngine!!.joinChannel(null, channelName, "Extra Optional Data", 0)
    }

    private fun onBigVideoViewDoubleClicked(view: View, position: Int) {
        if (mUidsList.size < 2) {
            return
        }
        val user = grid_video_view_container!!.getItem(position)
        val uid = if (user.mUid == 0) this.user!!.agoraUid else user.mUid
        if (mLayoutType == LAYOUT_TYPE_DEFAULT && mUidsList.size != 1) {
            switchToSmallVideoView(uid)
        } else {
            switchToDefaultVideoView()
        }
    }

    private fun switchToSmallVideoView(bigBgUid: Int) {
        val slice = HashMap<Int, SurfaceView?>(1)
        slice[bigBgUid] = mUidsList[bigBgUid]
        val iterator: Iterator<SurfaceView?> = mUidsList.values.iterator()
        while (iterator.hasNext()) {
            val s = iterator.next()
            s!!.setZOrderOnTop(true)
            s.setZOrderMediaOverlay(true)
        }
        mUidsList[bigBgUid]!!.setZOrderOnTop(false)
        mUidsList[bigBgUid]!!.setZOrderMediaOverlay(false)
        grid_video_view_container!!.initViewContainer(this, bigBgUid, slice, mIsLandscape)
        bindToSmallVideoView(bigBgUid)
        mLayoutType = LAYOUT_TYPE_SMALL
    }

    private fun bindToSmallVideoView(exceptUid: Int) {
        if (mSmallVideoViewDock == null) {
            val stub = findViewById<ViewStub>(R.id.small_video_view_dock)
            mSmallVideoViewDock = stub.inflate() as RelativeLayout
        }
        val twoWayVideoCall = mUidsList.size == 2
        val recycler = findViewById<RecyclerView>(R.id.small_video_view_container)
        var create = false
        if (mSmallVideoViewAdapter == null) {
            create = true
            mSmallVideoViewAdapter = AgoraSmallVideoViewAdapter(this, user!!.agoraUid, exceptUid, mUidsList)
            mSmallVideoViewAdapter!!.setHasStableIds(true)
        }
        recycler.setHasFixedSize(true)
        if (twoWayVideoCall) {
            recycler.layoutManager = RtlLinearLayoutManager(applicationContext, RtlLinearLayoutManager.HORIZONTAL, false)
        } else {
            recycler.layoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.HORIZONTAL, false)
        }
        recycler.addItemDecoration(AgoraSmallVideoViewDecoration())
        recycler.adapter = mSmallVideoViewAdapter
        recycler.addOnItemTouchListener(RecyclerItemClickListener(baseContext, object : RecyclerItemClickListener.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {}
            override fun onItemLongClick(view: View, position: Int) {}
            override fun onItemDoubleClick(view: View, position: Int) {
                onSmallVideoViewDoubleClicked(view, position)
            }
        }))
        recycler.isDrawingCacheEnabled = true
        recycler.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_AUTO
        if (!create) {
            mSmallVideoViewAdapter!!.setLocalUid(user!!.agoraUid)
            mSmallVideoViewAdapter!!.notifyUiChanged(mUidsList, exceptUid, null, null)
        }
        for (tempUid in mUidsList.keys) {
            if (user!!.agoraUid != tempUid) {
                if (tempUid == exceptUid) {
                    mRtcEngine!!.setRemoteUserPriority(tempUid, Constants.USER_PRIORITY_HIGH)
                } else {
                    mRtcEngine!!.setRemoteUserPriority(tempUid, Constants.USER_PRIORITY_NORANL)
                }
            }
        }
        recycler.visibility = View.VISIBLE
        mSmallVideoViewDock!!.visibility = View.VISIBLE
    }

    private fun onSmallVideoViewDoubleClicked(view: View, position: Int) {
        switchToDefaultVideoView()
    }

    private fun onRemoteUserLeft(uid: Int) {
        removeRemoteVideo(uid)
    }

    private fun removeRemoteVideo(uid: Int) {
        runOnUiThread(Runnable {
            val target = mUidsList.remove(uid) ?: return@Runnable
            switchToDefaultVideoView()
        })
    }

    private fun setupRemoteVideo(uid: Int) {
        runOnUiThread {
            val mRemoteView = RtcEngine.CreateRendererView(applicationContext)
            mUidsList[uid] = mRemoteView
            mRemoteView.setZOrderOnTop(true)
            mRemoteView.setZOrderMediaOverlay(true)
            mRtcEngine!!.setupRemoteVideo(VideoCanvas(mRemoteView, VideoCanvas.RENDER_MODE_HIDDEN, uid))
            switchToDefaultVideoView()
        }
    }

    private fun switchToDefaultVideoView() {
        grid_video_view_container!!.initViewContainer(this@AgoraVideoCallActivity, user!!.agoraUid, mUidsList, mIsLandscape)
        var setRemoteUserPriorityFlag = false
        mLayoutType = LAYOUT_TYPE_DEFAULT
        var sizeLimit = mUidsList.size
        if (sizeLimit > 5) {
            sizeLimit = 5
        }
        for (i in 0 until sizeLimit) {
            val uid = grid_video_view_container!!.getItem(i).mUid
            if (user!!.agoraUid != uid) {
                if (!setRemoteUserPriorityFlag) {
                    setRemoteUserPriorityFlag = true
                    mRtcEngine!!.setRemoteUserPriority(uid, Constants.USER_PRIORITY_HIGH)
                } else {
                    mRtcEngine!!.setRemoteUserPriority(uid, Constants.USER_PRIORITY_NORANL)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isCalling) {
            leaveChannel()
        }
        RtcEngine.destroy()
    }

    private fun leaveChannel() {
        // Leave the current channel.
        mRtcEngine!!.leaveChannel()
    }

    fun onCallClicked(view: View?) {
        if (isCalling) {
            //finish current call
            finishCalling()
            isCalling = false
            start_call_end_call_btn!!.setImageResource(R.drawable.btn_startcall)
            finish()
        } else {
            //start the call
            startCalling()
            isCalling = true
            start_call_end_call_btn!!.setImageResource(R.drawable.btn_endcall)
        }
    }

    private fun finishCalling() {
        leaveChannel()
        mUidsList.clear()
    }

    private fun startCalling() {
        setupLocalVideo()
        joinChannel()
    }

    fun onSwitchCameraClicked(view: View?) {
        mRtcEngine!!.switchCamera()
    }

    fun onLocalAudioMuteClicked(view: View?) {
        isMuted = !isMuted
        mRtcEngine!!.muteLocalAudioStream(isMuted)
        val res = if (isMuted) R.drawable.btn_mute else R.drawable.btn_unmute
        audio_mute_audio_unmute_btn!!.setImageResource(res)
    }

    fun onVideoChatClicked(view: View?) {
        jumpToMessageActivity()
    }

    private fun jumpToMessageActivity() {
        val intent = Intent(this, AgoraMessageActivity::class.java)
        intent.putExtra(MessageUtil.INTENT_EXTRA_TARGET_NAME, channelName)
        intent.putExtra(MessageUtil.INTENT_EXTRA_USER_ID, user)
        startActivity(intent)
    }

    companion object {
        const val LAYOUT_TYPE_DEFAULT = 0
        const val LAYOUT_TYPE_SMALL = 1
        private val TAG = AgoraVideoCallActivity::class.java.name
    }
}