package com.sandy.agorachatsandy.rtm;

import android.app.Application;

public class AGApplication extends Application {
    private static AGApplication sInstance;
    private AgoraChatManager mChatManager;

    public static AGApplication the() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        mChatManager = new AgoraChatManager(this);
        mChatManager.init();
        mChatManager.enableOfflineMessage(true);
    }

    public AgoraChatManager getChatManager() {
        return mChatManager;
    }
}
