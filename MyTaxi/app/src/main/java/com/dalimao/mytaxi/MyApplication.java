package com.dalimao.mytaxi;

import android.app.Application;

/**
 * Created by lsh on 2018/4/4.
 */

public class MyApplication extends Application {
    private static MyApplication instance ;
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this ;
    }

    public static MyApplication getInstance(){
        return  instance ;
    }


}
