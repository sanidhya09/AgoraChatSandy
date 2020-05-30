package com.sandy.agorachatsandy.layout;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.sandy.agorachatsandy.R;
import com.sandy.agorachatsandy.model.AgoraUserStatusData;
import com.sandy.agorachatsandy.model.AgoraVideoInfoData;
import com.sandy.agorachatsandy.model.AgoraVideoUserStatusHolder;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class AgoraVideoViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final boolean DEBUG = false;

    protected final LayoutInflater mInflater;
    protected final Context mContext;

    protected final ArrayList<AgoraUserStatusData> mUsers;

    protected int mLocalUid;
    protected HashMap<Integer, AgoraVideoInfoData> mVideoInfo; // left user should removed from this HashMap

    protected int mItemWidth;
    protected int mItemHeight;

    private int mDefaultChildItem = 0;

    private void init(HashMap<Integer, SurfaceView> uids) {
        mUsers.clear();

        customizedInit(uids, true);
    }

    protected abstract void customizedInit(HashMap<Integer, SurfaceView> uids, boolean force);

    public abstract void notifyUiChanged(HashMap<Integer, SurfaceView> uids, int uidExtra, HashMap<Integer, Integer> status, HashMap<Integer, Integer> volume);

    public AgoraVideoViewAdapter(Activity activity, int localUid, HashMap<Integer, SurfaceView> uids) {
        mInflater = activity.getLayoutInflater();
        mContext = activity.getApplicationContext();

        mLocalUid = localUid;

        mUsers = new ArrayList<>();

        init(uids);
    }

    public void addVideoInfo(int uid, AgoraVideoInfoData video) {
        if (mVideoInfo == null) {
            mVideoInfo = new HashMap<>();
        }
        mVideoInfo.put(uid, video);
    }

    public void cleanVideoInfo() {
        mVideoInfo = null;
    }

    public void setLocalUid(int uid) {
        mLocalUid = uid;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewGroup v = (ViewGroup) mInflater.inflate(R.layout.agora_video_view_container, parent, false);
        v.getLayoutParams().width = mItemWidth;
        v.getLayoutParams().height = mItemHeight;
        mDefaultChildItem = v.getChildCount();
        return new AgoraVideoUserStatusHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        AgoraVideoUserStatusHolder myHolder = ((AgoraVideoUserStatusHolder) holder);

        final AgoraUserStatusData user = mUsers.get(position);

        FrameLayout holderView = (FrameLayout) myHolder.itemView;

        if (holderView.getChildCount() == mDefaultChildItem) {
            SurfaceView target = user.mView;
            AgoraVideoViewAdapterUtil.stripView(target);
            holderView.addView(target, 0, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }

        AgoraVideoViewAdapterUtil.renderExtraData(mContext, user, myHolder);
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    @Override
    public long getItemId(int position) {
        AgoraUserStatusData user = mUsers.get(position);

        SurfaceView view = user.mView;
        if (view == null) {
            throw new NullPointerException("SurfaceView destroyed for user " + user.mUid + " " + user.mStatus + " " + user.mVolume);
        }

        return (String.valueOf(user.mUid) + System.identityHashCode(view)).hashCode();
    }
}
