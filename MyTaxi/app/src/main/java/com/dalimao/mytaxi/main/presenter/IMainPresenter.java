package com.dalimao.mytaxi.main.presenter;

import com.dalimao.mytaxi.common.Lbs.LocationInfo;

/**
 * Created by lsh on 2018/4/8.
 */

public interface IMainPresenter {

    void LoginByToken() ;

    /**
     * 获取附近的司机
     * @param latitude
     * @param longitude
     */
    void fetchNearDirvers(double latitude, double longitude);

    /**
     * 将位置信息推送给服务端
     * @param locationInfo
     */
    void updateLocationToServer(LocationInfo locationInfo);

    /**
     * 呼叫司机
     * @param mPushKey
     * @param mCost
     * @param mStartLocation
     * @param mEndLocation
     */
    void callDriver(String mPushKey, float mCost, LocationInfo mStartLocation, LocationInfo mEndLocation);

    boolean isLogin();

    /**
     * 取消呼叫
     */
    void cancel();

    /**
     * 支付
     */
    void pay();

    /**
     * 获取正在进行的订单
     */
    void getProcessOrder();
}
