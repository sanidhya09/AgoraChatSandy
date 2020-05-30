package com.sandy.agorachatsandy.model;

import android.view.SurfaceView;

public class AgoraUserStatusData {
    public static final int DEFAULT_STATUS = 0;
    public static final int VIDEO_MUTED = 1;
    public static final int AUDIO_MUTED = VIDEO_MUTED << 1;

    public static final int DEFAULT_VOLUME = 0;

    private AgoraVideoInfoData mVideoInfo;

    public AgoraUserStatusData(int uid, SurfaceView view, Integer status, int volume) {
        this(uid, view, status, volume, null);
    }

    public int mUid;

    public SurfaceView mView;

    public Integer mStatus; // if status is null, do nothing

    public int mVolume;

    public AgoraUserStatusData(int uid, SurfaceView view, Integer status, int volume, AgoraVideoInfoData i) {
        this.mUid = uid;
        this.mView = view;
        this.mStatus = status;
        this.mVolume = volume;
        this.mVideoInfo = i;
    }

    public void setVideoInfo(AgoraVideoInfoData video) {
        mVideoInfo = video;
    }

    public AgoraVideoInfoData getVideoInfoData() {
        return mVideoInfo;
    }

    @Override
    public String toString() {
        return "UserStatusData{" +
                "mUid=" + (mUid & 0XFFFFFFFFL) +
                ", mView=" + mView +
                ", mStatus=" + mStatus +
                ", mVolume=" + mVolume +
                '}';
    }
}
