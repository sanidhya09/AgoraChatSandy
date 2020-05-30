package com.sandy.agorachatsandy.layout;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.util.HashMap;

public class AgoraSmallVideoViewAdapter extends AgoraVideoViewAdapter {

    private int mExceptedUid;

    public AgoraSmallVideoViewAdapter(Activity activity, int localUid, int exceptedUid, HashMap<Integer, SurfaceView> uids) {
        super(activity, localUid, uids);
        mExceptedUid = exceptedUid;
    }

    @Override
    protected void customizedInit(HashMap<Integer, SurfaceView> uids, boolean force) {
        AgoraVideoViewAdapterUtil.composeDataItem(mUsers, uids, mLocalUid, null, null, mVideoInfo, mExceptedUid);

        if (force || mItemWidth == 0 || mItemHeight == 0) {
            WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics outMetrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(outMetrics);
            mItemWidth = outMetrics.widthPixels / 4;
            mItemHeight = outMetrics.heightPixels / 4;
        }
    }

    @Override
    public void notifyUiChanged(HashMap<Integer, SurfaceView> uids, int uidExcepted, HashMap<Integer, Integer> status, HashMap<Integer, Integer> volume) {
        mUsers.clear();

        mExceptedUid = uidExcepted;

        AgoraVideoViewAdapterUtil.composeDataItem(mUsers, uids, mLocalUid, status, volume, mVideoInfo, uidExcepted);

        notifyDataSetChanged();
    }

    public int getExceptedUid() {
        return mExceptedUid;
    }
}
