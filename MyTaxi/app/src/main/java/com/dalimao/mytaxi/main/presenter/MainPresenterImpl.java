package com.dalimao.mytaxi.main.presenter;

import android.view.View;

import com.dalimao.mytaxi.account.model.IAccountManager;
import com.dalimao.mytaxi.account.model.reaponse.LoginResponse;
import com.dalimao.mytaxi.common.Lbs.LocationInfo;
import com.dalimao.mytaxi.common.databus.RegisterBus;
import com.dalimao.mytaxi.common.http.biz.BaseBizResponse;
import com.dalimao.mytaxi.common.utils.LogUtil;
import com.dalimao.mytaxi.main.bean.Order;
import com.dalimao.mytaxi.main.model.IMainManager;
import com.dalimao.mytaxi.main.model.response.NearDirversResponse;
import com.dalimao.mytaxi.main.model.response.OrderStateOptResponse;
import com.dalimao.mytaxi.main.view.IMainView;

/**
 * Created by lsh on 2018/4/8.
 */

public class MainPresenterImpl implements IMainPresenter {
    private IMainView view;
    private IAccountManager accountManager;
    private IMainManager mainManager;
    private Order mCurrentOrder;

    @RegisterBus
    public void onMainRequset(LoginResponse loginResponse) {
        switch (loginResponse.getCode()) {
            //token失效
            case IAccountManager.TOKEN_INVALID:
                view.showErrow(IAccountManager.LOGIN_SUC, "");
                break;
            //登陆成功
            case IAccountManager.LOGIN_SUC:
                view.showLoginSuc();
                break;
            //服务器错误
            case IAccountManager.SERVER_FAIL:
                view.showErrow(IAccountManager.SERVER_FAIL, "");
                break;
        }
    }

    @RegisterBus
    public void onNearDirversRequset(NearDirversResponse nearDirversResponse) {
        if (nearDirversResponse.getCode() == BaseBizResponse.STATE_OK) {
            view.showNearDrivers(nearDirversResponse.getData());
        }

    }

    @RegisterBus
    public void onLocationInfo(LocationInfo locationInfo) {
        if (mCurrentOrder != null
                && (mCurrentOrder.getState() == OrderStateOptResponse.ORDER_STATE_ACCEPT)) {
            view.updateDriver2startRoutes(locationInfo, mCurrentOrder);
        } else if (mCurrentOrder != null
                && (mCurrentOrder.getState() == OrderStateOptResponse.ORDER_STATE_START_DRIVE)) {
            view.updateDriver2EndRoute(locationInfo, mCurrentOrder);
        }
        view.showLocationChange(locationInfo);
    }

    @RegisterBus
    public void onOrderOptResponse(OrderStateOptResponse response) {
        if (response.getState() == OrderStateOptResponse.ORDER_STATE_CREATE) {
            //呼叫司机
            if (response.getCode() == BaseBizResponse.STATE_OK) {
                System.out.println(response.getData());
                mCurrentOrder = response.getData();
                view.showCallDirverSuc(mCurrentOrder);
            } else {
                view.showCallDirverFail();
            }
        } else if (response.getState() == OrderStateOptResponse.ORDER_STATE_CANCEL) {
            //取消订单
            if (response.getCode() == BaseBizResponse.STATE_OK) {
                mCurrentOrder = null;
                view.showCancelSuc();
            } else {
                view.showCancelFail();
            }
        } else if (response.getState() == OrderStateOptResponse.ORDER_STATE_ACCEPT) {
            //司机接单
            mCurrentOrder = response.getData();
            view.showDriverAcceptOrder(mCurrentOrder);
        } else if (response.getState() == OrderStateOptResponse.ORDER_STATE_ARRIVE_START) {
            //司机到达山车地点
            mCurrentOrder = response.getData();
            view.showDriverArriveStart(mCurrentOrder);
        } else if (response.getState() == OrderStateOptResponse.ORDER_STATE_START_DRIVE) {
            //开始行程
            mCurrentOrder = response.getData();
            view.showStartDriver(mCurrentOrder);
        } else if (response.getState() == OrderStateOptResponse.ORDER_STATE_ARRIVE_END) {
            //到达终点
            mCurrentOrder = response.getData();
            view.showArriveTarget(mCurrentOrder);
        } else if (response.getState() == OrderStateOptResponse.PAY) {
            System.out.println("pay order = " + response.getState() );
            if (response.getCode() == BaseBizResponse.STATE_OK) {

                view.showPaySuc(mCurrentOrder);
            } else {
                view.showPayFail();
            }
        }
    }

    public MainPresenterImpl(IMainView view, IAccountManager accountManager, IMainManager mainManager) {
        this.view = view;
        this.accountManager = accountManager;
        this.mainManager = mainManager;
    }

    @Override
    public void LoginByToken() {
        accountManager.loginByToken();
    }

    @Override
    public void fetchNearDirvers(double latitude, double longitude) {
        mainManager.fetchNearDrivers(latitude, longitude);
    }

    /**
     * 上报位置
     *
     * @param locationInfo
     */
    @Override
    public void updateLocationToServer(LocationInfo locationInfo) {
        mainManager.updateLocationToServer(locationInfo);
    }

    @Override
    public void callDriver(String mPushKey, float mCost, LocationInfo mStartLocation, LocationInfo mEndLocation) {
        mainManager.callDriver(mPushKey, mCost, mStartLocation, mEndLocation);
    }

    @Override
    public boolean isLogin() {
        return accountManager.isLogin();
    }

    /**
     * 取消呼叫
     */
    @Override
    public void cancel() {
        if (mCurrentOrder != null) {
            System.out.println("订单取消了1");
            mainManager.cancelOrder(mCurrentOrder.getOrderId());
        } else {
            System.out.println("订单取消了2");
            view.showCancelSuc();
        }
    }

    @Override
    public void pay() {
        if (mCurrentOrder != null) {
            mainManager.pay(mCurrentOrder.getOrderId());
        }

    }

    @Override
    public void getProcessOrder() {
        mainManager.getProcessOrder();
        LogUtil.d("MainPresenterImpl", "getProcessOrder");
    }
}
