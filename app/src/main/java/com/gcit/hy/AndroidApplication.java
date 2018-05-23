package com.gcit.hy;

import android.app.Application;

import com.gcit.hy.util.CrashHandler;

public class AndroidApplication extends Application {

    private static AndroidApplication sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        //在这里为应用设置异常处理程序，然后我们的程序才能捕获未处理的异常
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(this);
    }

    public static AndroidApplication getInstance() {
        return sInstance;
    }

}
