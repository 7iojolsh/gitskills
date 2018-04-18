package com.dalimao.mytaxi.main.model;

import com.dalimao.mytaxi.MyApplication;
import com.dalimao.mytaxi.account.model.reaponse.Account;
import com.dalimao.mytaxi.common.Lbs.LocationInfo;
import com.dalimao.mytaxi.common.databus.RxBus;
import com.dalimao.mytaxi.common.http.IHttpClient;
import com.dalimao.mytaxi.common.http.IRequest;
import com.dalimao.mytaxi.common.http.IResponse;
import com.dalimao.mytaxi.common.http.Impl.BaseRequest;
import com.dalimao.mytaxi.common.http.Impl.BaseResponse;
import com.dalimao.mytaxi.common.http.api.API;
import com.dalimao.mytaxi.common.http.biz.BaseBizResponse;
import com.dalimao.mytaxi.common.storage.SharedPreferencesDao;
import com.dalimao.mytaxi.common.utils.LogUtil;
import com.dalimao.mytaxi.main.bean.Order;
import com.dalimao.mytaxi.main.model.response.NearDirversResponse;
import com.dalimao.mytaxi.main.model.response.OrderStateOptResponse;
import com.google.gson.Gson;

import rx.functions.Func1;

/**
 * Created by lsh on 2018/4/11.
 */

public class MainManagerImpl implements IMainManager {
    private static final String TAG = "MainManagerImpl";
    private IHttpClient mHttpClient;

    public MainManagerImpl(IHttpClient iHttpClient) {
        this.mHttpClient = iHttpClient;
    }

    @Override
    public void fetchNearDrivers(final double latitude, final double longitude) {
        RxBus.getInstance().chainProcess(new Func1() {
            @Override
            public Object call(Object o) {
                //进行网络请求的处理

                IRequest request = new BaseRequest(API.Config.getDomain()
                        + API.GET_NEAR_DRIVERS);
                request.setBody("latitude", Double.toString(latitude));
                request.setBody("longitude", Double.toString(longitude));
                IResponse response = mHttpClient.get(request, false);
                int code = response.getCode();
                if (code == BaseBizResponse.STATE_OK) {

                    try {
                        NearDirversResponse nearDirversResponse =
                                new Gson().fromJson(response.getData(), NearDirversResponse.class);
                        LogUtil.d("MainManagerImpl", response.getData());
                        return nearDirversResponse;
                    } catch (Exception e) {
                        return null;
                    }

                }
                return null;
            }
        });
    }

    @Override
    public void updateLocationToServer(final LocationInfo locationInfo) {
        RxBus.getInstance().chainProcess(new Func1() {
            @Override
            public Object call(Object o) {
                //进行网络请求的处理

                IRequest request = new BaseRequest(API.Config.getDomain()
                        + API.UPLOAD_LOCATION);
                request.setBody("latitude", Double.toString(locationInfo.getLatitude()));
                request.setBody("longitude", Double.toString(locationInfo.getLongitude()));
                request.setBody("key", locationInfo.getKey());
                request.setBody("rotation", Float.toString(locationInfo.getRotation()));
                IResponse response = mHttpClient.post(request, false);
                int code = response.getCode();
                if (code == BaseBizResponse.STATE_OK) {
                    LogUtil.d(TAG, "位置上报成功");
                } else {
                    LogUtil.d(TAG, "位置上报失败");
                }
                return null;
            }
        });
    }

    /**
     * 呼叫司机
     *
     * @param mPushKey
     * @param mCost
     * @param mStartLocation
     * @param mEndLocation
     */
    @Override
    public void callDriver(final String mPushKey,
                           final float mCost,
                           final LocationInfo mStartLocation,
                           final LocationInfo mEndLocation) {
        RxBus.getInstance().chainProcess(new Func1() {
            @Override
            public Object call(Object o) {
                /**
                 *  获取 uid,phone
                 */

                SharedPreferencesDao sharedPreferencesDao =
                        new SharedPreferencesDao(MyApplication.getInstance(),
                                SharedPreferencesDao.FILE_ACCOUNT);
                Account account =
                        (Account) sharedPreferencesDao.get(SharedPreferencesDao.KEY_ACCOUNT,
                                Account.class);
                String uid = account.getUid();
                String phone = account.getAccount();
                IRequest request = new BaseRequest(API.Config.getDomain()
                        + API.CALL_DRIVER);
                request.setBody("key", mPushKey);
                request.setBody("uid", uid);
                request.setBody("phone", phone);
                request.setBody("startLatitude",
                        Double.valueOf(mStartLocation.getLatitude()).toString());
                request.setBody("startLongitude",
                        Double.valueOf(mStartLocation.getLongitude()).toString());
                request.setBody("endLatitude",
                        Double.valueOf(mEndLocation.getLatitude()).toString());
                request.setBody("endLongitude",
                        Double.valueOf(mEndLocation.getLongitude()).toString());
                request.setBody("cost", Float.valueOf(mCost).toString());

                IResponse response = mHttpClient.post(request, false);
                OrderStateOptResponse orderStateOptResponse =
                        new OrderStateOptResponse();
                if (response.getCode() == BaseBizResponse.STATE_OK || response.getCode() == BaseBizResponse.STATE_OK) {
                    // 解析订单信息
                    orderStateOptResponse =
                            new Gson().fromJson(response.getData(),
                                    OrderStateOptResponse.class);
                }

                orderStateOptResponse.setCode(response.getCode());
                orderStateOptResponse.setState(OrderStateOptResponse.ORDER_STATE_CREATE);
                LogUtil.d(TAG, "call driver: " + response.getData());

                return orderStateOptResponse;
            }
        });
    }

    /**
     * 取消订单
     *
     * @param orderId
     */
    @Override
    public void cancelOrder(final String orderId) {
        RxBus.getInstance().chainProcess(new Func1() {
            @Override
            public Object call(Object o) {
                System.out.println("我执行了");
                IRequest request = new BaseRequest(API.Config.getDomain()
                        + API.CANCEL_ORDER);
                request.setBody("id", orderId);
                IResponse response = mHttpClient.post(request, false);
                OrderStateOptResponse orderStateOptResponse = new OrderStateOptResponse();
                orderStateOptResponse.setCode(response.getCode());
                orderStateOptResponse.setState(OrderStateOptResponse.ORDER_STATE_CANCEL);
                LogUtil.d(TAG, "cancel order : " + response.getData());
                return orderStateOptResponse;
            }
        });
    }

    @Override
    public void pay(final String orderId) {
        RxBus.getInstance().chainProcess(new Func1() {
            @Override
            public Object call(Object o) {
                IRequest request = new BaseRequest(API.Config.getDomain()
                        + API.PAY);
                request.setBody("id", orderId);
                IResponse response = mHttpClient.post(request, false);
                OrderStateOptResponse orderStateOptResponse = new OrderStateOptResponse();
                orderStateOptResponse.setCode(response.getCode());
                orderStateOptResponse.setState(OrderStateOptResponse.PAY);

                LogUtil.d(TAG, "pay order : " + response.getData());
                return orderStateOptResponse;
            }
        });
    }

    @Override
    public void getProcessOrder() {
        RxBus.getInstance().chainProcess(new Func1() {
            @Override
            public Object call(Object o) {
                /**
                 * 获取uid
                 */
                SharedPreferencesDao sharedPreferencesDao =
                        new SharedPreferencesDao(MyApplication.getInstance(),
                                SharedPreferencesDao.FILE_ACCOUNT);
                Account account =
                        (Account) sharedPreferencesDao.get(SharedPreferencesDao.KEY_ACCOUNT,
                                Account.class);
                String uid = account.getUid();
                IRequest request = new BaseRequest(API.Config.getDomain() +
                        API.GET_PROCESSING_ORDER);
                request.setBody("uid" ,uid);
                IResponse response = mHttpClient.get(request ,false);
                LogUtil.d(TAG  , "getProcessOrder order" + response.getData());
                if(response.getCode() == BaseBizResponse.STATE_OK){
                    /**
                     * 解析订单数据 , 封装到OrderStateOptResponse
                     */
                    OrderStateOptResponse orderStateOptResponse
                            = new Gson().fromJson(response.getData(), OrderStateOptResponse.class);
                    LogUtil.d(TAG , orderStateOptResponse.toString());
                    // 只有当订单状态是才返回
                    if (orderStateOptResponse.getCode() == BaseBizResponse.STATE_OK) {
                        orderStateOptResponse.setState(orderStateOptResponse.getData().getState());
                        return orderStateOptResponse ;
                    }

                }

                return null;
            }
        });
    }
}
