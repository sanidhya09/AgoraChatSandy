package com.sandy.agorachatsandy.model;

import android.os.Parcel;
import android.os.Parcelable;

public class AgoraUser implements Parcelable {
    private String fireUid, fireDisplayName;
    private int agoraUid;

    public static final Creator<AgoraUser> CREATOR = new Creator<AgoraUser>() {
        @Override
        public AgoraUser createFromParcel(Parcel in) {
            return new AgoraUser(in);
        }

        @Override
        public AgoraUser[] newArray(int size) {
            return new AgoraUser[size];
        }
    };

    public AgoraUser(String fireUid) {
        setFireUid(fireUid);
    }

    protected AgoraUser(Parcel in) {
        fireUid = in.readString();
        fireDisplayName = in.readString();
        agoraUid = in.readInt();
    }

    public String getFireUid() {
        return fireUid;
    }

    public void setFireUid(String fireUid) {
        this.fireUid = fireUid;
    }

    public String getFireDisplayName() {
        return fireDisplayName;
    }

    public void setFireDisplayName(String fireDisplayName) {
        this.fireDisplayName = fireDisplayName;
    }

    public int getAgoraUid() {
        return agoraUid;
    }

    public void setAgoraUid(int agoraUid) {
        this.agoraUid = agoraUid;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(fireUid);
        dest.writeString(fireDisplayName);
        dest.writeInt(agoraUid);
    }
}
