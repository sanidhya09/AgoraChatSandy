package com.sandy.agorachatsandy.model;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.sandy.agorachatsandy.R;

public class AgoraVideoUserStatusHolder extends RecyclerView.ViewHolder {
    public final RelativeLayout mMaskView;

    public final ImageView mAvatar;
    public final ImageView mIndicator;

    public final LinearLayout mVideoInfo;

    public final TextView mMetaData;

    public AgoraVideoUserStatusHolder(View v) {
        super(v);

        mMaskView = v.findViewById(R.id.user_control_mask);
        mAvatar = v.findViewById(R.id.default_avatar);
        mIndicator = v.findViewById(R.id.indicator);

        mVideoInfo = v.findViewById(R.id.video_info_container);

        mMetaData = v.findViewById(R.id.video_info_metadata);
    }
}
