package com.dalimao.mytaxi.main.model;

import com.dalimao.mytaxi.common.Lbs.LocationInfo;

/**
 * Created by lsh on 2018/4/11.
 */

public interface IMainManager {

    /**
     * 获取附近的司机
     * @param latitude
     * @param longitude
     */
    void fetchNearDrivers(double latitude, double longitude);

    /**
     * 上报位置给服务器
     * @param locationInfo
     */
    void updateLocationToServer(LocationInfo locationInfo);


    void callDriver(String mPushKey, float mCost, LocationInfo mStartLocation, LocationInfo mEndLocation);

    /**
     * 取消订单
     * @param orderId
     */
    void cancelOrder(String orderId);

    /**
     * 支付订单
     * @param orderId
     */
    void pay(String orderId);

    /**
     * 获取正在进行的订单
     */
    void getProcessOrder();
}
