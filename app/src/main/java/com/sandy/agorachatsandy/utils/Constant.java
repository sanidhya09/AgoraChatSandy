package com.sandy.agorachatsandy.utils;

import io.agora.rtc.RtcEngine;

public class Constant {

    public static final String MEDIA_SDK_VERSION;

    static {
        String sdk = "undefined";
        try {
            sdk = RtcEngine.getSdkVersion();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        MEDIA_SDK_VERSION = sdk;
    }

    public static boolean SHOW_VIDEO_INFO = true;

}
