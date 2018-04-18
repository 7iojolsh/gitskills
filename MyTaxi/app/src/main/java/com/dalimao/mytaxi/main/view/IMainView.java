package com.dalimao.mytaxi.main.view;

import com.dalimao.mytaxi.account.view.IView;
import com.dalimao.mytaxi.common.Lbs.LocationInfo;
import com.dalimao.mytaxi.main.bean.Order;

import java.util.List;

/**
 * Created by lsh on 2018/4/8.
 */

public interface IMainView extends IView {

    /**
     * token 登陆成功
     */
    void showLoginSuc() ;

    /**
     * 显示附近司机
     */
    void showNearDrivers(List<LocationInfo> data);

    /**
     * 显示位置变化
     * @param locationInfo
     */
    void showLocationChange(LocationInfo locationInfo);

    /**
     * 呼叫司机成功
     * @param mCurrentOrder
     */
    void showCallDirverSuc(Order mCurrentOrder);

    /**
     * 呼叫司机失败
     */
    void showCallDirverFail();

    void showCancelSuc();

    void showCancelFail();

    /**
     * 显示司机接单
     * @param mCurrentOrder 订单信息
     */
    void showDriverAcceptOrder(Order mCurrentOrder);

    void showDriverArriveStart(Order mCurrentOrder);

    void showStartDriver(Order mCurrentOrder);

    void showArriveTarget(Order mCurrentOrder);

    void updateDriver2startRoutes(LocationInfo locationInfo, Order mCurrentOrder);

    void updateDriver2EndRoute(LocationInfo locationInfo, Order mCurrentOrder);

    void showPaySuc(Order mCurrentOrder);

    void showPayFail();
}
