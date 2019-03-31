package com.lc.monitor;

import android.app.Application;

public class AppApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CommCont.checkFileDirs(this);
    }
}
