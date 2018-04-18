package com.dalimao.mytaxi.main.view;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import com.dalimao.mytaxi.MyApplication;
import com.dalimao.mytaxi.account.model.AccountManagerImpl;
import com.dalimao.mytaxi.account.model.IAccountManager;
import com.dalimao.mytaxi.account.model.reaponse.Account;
import com.dalimao.mytaxi.account.view.PhoneInputDialog;
import com.dalimao.mytaxi.common.Lbs.GaodeLbsLayerImpl;
import com.dalimao.mytaxi.common.Lbs.ILbsLayer;
import com.dalimao.mytaxi.common.Lbs.LocationInfo;
import com.dalimao.mytaxi.common.Lbs.RouteInfo;
import com.dalimao.mytaxi.common.databus.RxBus;
import com.dalimao.mytaxi.common.http.IHttpClient;
import com.dalimao.mytaxi.common.http.Impl.OkHttpClientImpl;
import com.dalimao.mytaxi.common.http.api.API;
import com.dalimao.mytaxi.common.storage.SharedPreferencesDao;
import com.dalimao.mytaxi.common.utils.DevUtil;
import com.dalimao.mytaxi.common.utils.LogUtil;
import com.dalimao.mytaxi.common.utils.ToastUtil;
import com.dalimao.mytaxi.main.bean.Order;
import com.dalimao.mytaxi.main.model.MainManagerImpl;
import com.dalimao.mytaxi.main.presenter.IMainPresenter;
import com.dalimao.mytaxi.main.presenter.MainPresenterImpl;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.push.BmobPush;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobInstallation;

import com.dalimao.mytaxi.R;

/**
 * -------登陆逻辑-----------
 * 1 检查本地记录(登陆状态检查)
 * 2 若用户没有登陆则登陆
 * 3 登陆之前先校验手机号码
 * ------地图初始化-----------
 * 1 接入地图
 * 2 定位自己的位置,显示定位蓝点
 * 3 使用marker 改变当前位置和方向
 * 4 地图封装
 * ------获取附近司机------
 */
public class MainActivity extends AppCompatActivity implements IMainView {

    private static final String LOCATION_END = "10000end";
    private static final String TAG = "MainActivity";
    private IMainPresenter mPresenter;
    private ILbsLayer mLbsLayer;
    private Bitmap mDriverBit;
    private String mPushKey;
    // 起点和终点
    //  起点与终点
    private AutoCompleteTextView mStartEdit;
    private AutoCompleteTextView mEndEdit;
    private Bitmap mStartBit;
    private Bitmap mEndBit;
    private PoiAdapter mEndAdapter;
    // 标题栏显示当前城市
    private TextView mCity;
    LocationInfo mStartLocation;
    LocationInfo mEndLocation;
    //  当前是否登录
    private boolean mIsLogin;
    //  操作状态相关元素
    private View mOptArea;
    private View mLoadingArea;
    private TextView mTips;
    private TextView mLoadingText;
    private Button mBtnCall;
    private Button mBtnCancel;
    private Button mBtnPay;
    private float mCost;
    private Bitmap mLocationBit;
    private boolean mIsLocate;
    private Object processingOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IHttpClient iHttpClient = new OkHttpClientImpl();
        SharedPreferencesDao preferencesDao = new SharedPreferencesDao(MyApplication.getInstance(),
                SharedPreferencesDao.FILE_ACCOUNT);
        AccountManagerImpl accountManager = new AccountManagerImpl(iHttpClient, preferencesDao);
        MainManagerImpl mainManager = new MainManagerImpl(iHttpClient);
        mPresenter = new MainPresenterImpl(this, accountManager, mainManager);
        checkLoginState();
        RxBus.getInstance().register(mPresenter);
        //地图服务
        mLbsLayer = new GaodeLbsLayerImpl(this);
        mLbsLayer.onCreate(savedInstanceState);
        mLbsLayer.setLocationChangeListener(new ILbsLayer.CommonLocationChangeListener() {
            @Override
            public void onLocationChange(LocationInfo locationInfo) {
            }

            @Override
            public void onLocation(LocationInfo locationInfo) {

                // 记录起点
                mStartLocation = locationInfo;
                //设置标题
                mCity.setText(mLbsLayer.getCity());
                //设置起点
                mStartEdit.setText(locationInfo.getName().trim());
                //获取附近司机
                getNearDrivers(locationInfo.getLatitude(), locationInfo.getLongitude());
                //上报当前位置
                updateLocationToServer(locationInfo);
                // 首次定位，添加当前位置的标记
                addLocationMarker();
                mIsLocate = true;
                //获取进行中的订单
                getProcessingOrder();
            }
        });

        ViewGroup viewGroup = (ViewGroup) findViewById(R.id.map_container);
        viewGroup.addView(mLbsLayer.getMapView());
        //推送服务
        //初始化BmobSdk
        Bmob.initialize(this, API.Config.getAppId());
        //使用推送服务时的初始化操作
        BmobInstallation installation = BmobInstallation.getCurrentInstallation(this);
        installation.save();
        mPushKey = installation.getInstallationId();
        //启动推送服务
        BmobPush.startWork(this);

        //初始化其他地图
        initViews();
    }


    private void initViews() {
        mStartEdit = (AutoCompleteTextView) findViewById(R.id.start);
        mEndEdit = (AutoCompleteTextView) findViewById(R.id.end);
        mCity = (TextView) findViewById(R.id.city);
        mOptArea = findViewById(R.id.optArea);
        mLoadingArea = findViewById(R.id.loading_area);
        mLoadingText = (TextView) findViewById(R.id.loading_text);
        mBtnCall = (Button) findViewById(R.id.btn_call_driver);
        mBtnCancel = (Button) findViewById(R.id.btn_cancel);
        mBtnPay = (Button) findViewById(R.id.btn_pay);
        mTips = (TextView) findViewById(R.id.tips_info);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.btn_call_driver:
                        //呼叫司机
                        callDriver();
                        break;
                    case R.id.btn_cancel:
                        cancel();
                        break;
                    case R.id.btn_pay:
                        pay();
                        break;
                }
            }
        };
        mBtnCall.setOnClickListener(listener);
        mBtnCancel.setOnClickListener(listener);
        mBtnPay.setOnClickListener(listener);
        //加入动态监听
        mEndEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //关键搜索推荐地点
                mLbsLayer.poiSearch(s.toString(), new ILbsLayer.OnSearchedListener() {

                    @Override
                    public void onSearched(List<LocationInfo> results) {
                        //更新列表
                        updataPoiList(results);
                    }

                    @Override
                    public void onError(int code) {

                    }
                });

            }
        });
    }

    /**
     * 支付订单
     */
    private void pay() {
        mLoadingArea.setVisibility(View.VISIBLE);
        mTips.setVisibility(View.GONE);
        mLoadingText.setText(getString(R.string.paying));
        mPresenter.pay();
    }

    /**
     * 取消
     */
    private void cancel() {
        if (!mBtnCall.isEnabled()) {

            //说明已经点了呼叫
            showCanceling();
            mPresenter.cancel();
        } else {

            //只是显示了路径信息 , 还没有点击呼叫, 护肤ui即可
            restoreUI();
        }
    }

    private void restoreUI() {
        // 清楚地图上所有标记：路径信息、起点、终点
        mLbsLayer.clearAllMarkers();
        // 添加定位标记
        addLocationMarker();
        // 恢复地图视野
        mLbsLayer.moveCameraToPoint(mStartLocation, 17);
        //  获取附近司机
        getNearDrivers(mStartLocation.getLatitude(), mStartLocation.getLongitude());
        // 隐藏操作栏
        hideOptArea();
    }

    private void hideOptArea() {
        mOptArea.setVisibility(View.GONE);
    }

    private void showCanceling() {
        mTips.setVisibility(View.GONE);
        mLoadingArea.setVisibility(View.VISIBLE);
        mLoadingText.setText(getString(R.string.canceling));
        mBtnCancel.setEnabled(false);
    }

    /**
     * 呼叫司机
     */
    private void callDriver() {
        mIsLogin = mPresenter.isLogin();
        if (mIsLogin) {
            //如果已经登录 , 直接呼叫
            showCalling();

            mPresenter.callDriver(mPushKey, mCost, mStartLocation, mEndLocation);
        } else {
            //未登陆,先登陆
            mPresenter.LoginByToken();
            ToastUtil.show(this, getString(R.string.pls_login));
        }

    }

    private void showCalling() {
        System.out.println("呼叫了司机");
        mTips.setVisibility(View.GONE);
        mLoadingArea.setVisibility(View.VISIBLE);
        mLoadingText.setText(getString(R.string.calling_driver));
        mBtnCall.setEnabled(false);
        mBtnCancel.setEnabled(true);
    }

    private void updataPoiList(final List<LocationInfo> results) {
        List<String> listString = new ArrayList<>();
        for (LocationInfo result : results) {
            listString.add(result.getName());
        }

        if (mEndAdapter == null) {
            mEndAdapter = new PoiAdapter(getApplicationContext(), listString);
            mEndEdit.setAdapter(mEndAdapter);
        } else {
            mEndAdapter.setData(listString);
        }
        mEndEdit.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                ToastUtil.show(MainActivity.this, results.get(position).getName());
                DevUtil.closeInputMethod(MainActivity.this);
                //记录终点
                mEndLocation = results.get(position);
                mEndLocation.setKey(LOCATION_END);
                //路径绘制
                showRoute(mStartLocation, mEndLocation, new ILbsLayer.OnRouteCompleteListener() {
                    @Override
                    public void onComplete(RouteInfo result) {
                        LogUtil.d(TAG, "driverRout : " + result);
                        mLbsLayer.moveCamera(mStartLocation, mEndLocation);
                        //显示操作区
                        showOptArea();
                        mCost = result.getTaxiCost();
                        String infoString = getString(R.string.route_info);
                        infoString = String.format(infoString,
                                Float.valueOf(result.getDistance()).intValue()
                                , mCost, result.getDuration());
                        mTips.setVisibility(View.VISIBLE);
                        mTips.setText(infoString);
                    }
                });


            }
        });
        mEndAdapter.notifyDataSetChanged();

    }


    /**
     * 绘制起点终点路径
     *
     * @param mStartLocation
     * @param mEndLocation
     */
    private void showRoute(final LocationInfo mStartLocation, final LocationInfo mEndLocation,
                           ILbsLayer.OnRouteCompleteListener listener) {
        mLbsLayer.clearAllMarkers();
        addStartMarker();
        addEndMarker();
        mLbsLayer.driverRoute(mStartLocation,
                mEndLocation,
                Color.GREEN,
                listener
        );

    }

    /**
     * 显示操作区
     */
    private void showOptArea() {
        mOptArea.setVisibility(View.VISIBLE);
        mLoadingArea.setVisibility(View.GONE);
        mTips.setVisibility(View.VISIBLE);
        mBtnCall.setEnabled(true);
        mBtnCancel.setEnabled(true);
        mBtnCancel.setVisibility(View.VISIBLE);
        mBtnCall.setVisibility(View.VISIBLE);
        mBtnPay.setVisibility(View.GONE);
    }

    private void addEndMarker() {
        if (mEndBit == null || mEndBit.isRecycled()) {
            mEndBit = BitmapFactory.decodeResource(getResources(),
                    R.drawable.end);
        }
        mLbsLayer.addOrUpdateMarker(mEndLocation, mEndBit);
    }

    private void addStartMarker() {
        if (mStartBit == null || mStartBit.isRecycled()) {
            mStartBit = BitmapFactory.decodeResource(getResources(),
                    R.drawable.start);
        }
        mLbsLayer.addOrUpdateMarker(mStartLocation, mStartBit);
    }

    /**
     * 上报当前位置
     *
     * @param locationInfo
     */
    private void updateLocationToServer(LocationInfo locationInfo) {
        locationInfo.setKey(mPushKey);
        mPresenter.updateLocationToServer(locationInfo);
    }

    /**
     * 获取附近司机
     *
     * @param latitude
     * @param longitude
     */
    private void getNearDrivers(double latitude, double longitude) {
        mPresenter.fetchNearDirvers(latitude, longitude);
    }

    /**
     * 首次定位，添加当前位置的标记
     */
    private void addLocationMarker() {
        if (mLocationBit == null || mLocationBit.isRecycled()) {
            mLocationBit = BitmapFactory.decodeResource(getResources(),
                    R.drawable.navi_map_gps_locked);
        }
        mLbsLayer.addOrUpdateMarker(mStartLocation, mLocationBit);
    }

    /**
     * 检查用户是否登陆
     */
    private void checkLoginState() {
        //  获取本地用户信息
        SharedPreferencesDao preferencesDao =
                new SharedPreferencesDao(MyApplication.getInstance(),
                        SharedPreferencesDao.FILE_ACCOUNT);
        final Account account =
                (Account) preferencesDao.get(SharedPreferencesDao.KEY_ACCOUNT, Account.class);
        //登陆是否过期
        boolean tokenVlid = false;
        //检查token是否过期
        if (account != null) {
            if (account.getExpired() > System.currentTimeMillis()) {
                //toKen 有效
                tokenVlid = true;
            }
        }
        if (!tokenVlid) {
            showPhoneInputDialoig();
        } else {
            mPresenter.LoginByToken();
        }


    }

    /**
     * 显示手机输入框
     */
    private void showPhoneInputDialoig() {
        PhoneInputDialog dialog = new PhoneInputDialog(this);
        dialog.show();
    }

    @Override
    public void showLoginSuc() {
        ToastUtil.show(getApplicationContext(), getString(R.string.login_suc));
        mIsLogin = true;
        if (mStartLocation != null) {
            updateLocationToServer(mStartLocation);
        }
        //获取正在进行的订单
        getProcessingOrder();
    }

    /**
     * 显示附近司机
     */
    @Override
    public void showNearDrivers(List<LocationInfo> data) {
        //判断司机图标是否为空, 或是被回收
        if (mDriverBit == null || mDriverBit.isRecycled()) {
            mDriverBit = BitmapFactory.decodeResource(getResources(), R.drawable.car);
        }
        for (LocationInfo locationInfo : data) {
            showLocationChange(locationInfo);
        }
    }

    @Override
    public void showLocationChange(LocationInfo locationInfo) {
        if (mDriverBit == null || mDriverBit.isRecycled()) {
            mDriverBit = BitmapFactory.decodeResource(getResources(), R.drawable.car);
        }
        mLbsLayer.addOrUpdateMarker(locationInfo, mDriverBit);
    }

    //呼叫成功
    @Override
    public void showCallDirverSuc(Order order) {

        mLoadingArea.setVisibility(View.GONE);
        mTips.setVisibility(View.VISIBLE);
        mTips.setText(getString(R.string.show_call_suc));
        //显示操作区
        showOptArea();
        mBtnCall.setEnabled(false);
        //显示路径信息
        if (order.getEndLongitude() != 0 ||
                order.getDriverLatitude() != 0) {
            mEndLocation = new LocationInfo(order.getEndLatitude(), order.getEndLongitude());
            mEndLocation.setKey(LOCATION_END);
            //绘制路径
            showRoute(mStartLocation, mEndLocation, new ILbsLayer.OnRouteCompleteListener() {
                @Override
                public void onComplete(RouteInfo result) {
                    LogUtil.d(TAG, "driverRout : " + result);
                    mLbsLayer.moveCamera(mStartLocation, mEndLocation);
                    mCost = result.getTaxiCost();
                    String infoString = getString(R.string.route_info);
                    infoString = String.format(infoString,
                            Float.valueOf(result.getDistance()).intValue()
                            , mCost, result.getDuration());
                    mTips.setVisibility(View.VISIBLE);
                    mTips.setText(infoString);
                }
            });
        }
    }

    //呼叫失败
    @Override
    public void showCallDirverFail() {
        mLoadingArea.setVisibility(View.GONE);
        mTips.setVisibility(View.VISIBLE);
        mTips.setText(getString(R.string.show_call_fail));
    }

    /**
     * 订单取消成功
     */
    @Override
    public void showCancelSuc() {
        ToastUtil.show(this, getString(R.string.order_cancel_suc));
        restoreUI();
    }

    /**
     * 订单取消失败
     */
    @Override
    public void showCancelFail() {
        ToastUtil.show(this, getString(R.string.order_cancel_error));
        mBtnCancel.setEnabled(true);
    }

    /**
     * 司机接单
     *
     * @param mCurrentOrder 订单信息
     */
    @Override
    public void showDriverAcceptOrder(final Order mCurrentOrder) {
        // 提示信息
        ToastUtil.show(this, getString(R.string.driver_accept_order));

        // 清除地图标记
        mLbsLayer.clearAllMarkers();
        /**
         * 添加司机标记
         */

        final LocationInfo driverLocation =
                new LocationInfo(mCurrentOrder.getDriverLatitude(),
                        mCurrentOrder.getDriverLongitude());
        driverLocation.setKey(mCurrentOrder.getKey());
        showLocationChange(driverLocation);
        // 显示我的位置
        addLocationMarker();
        /**
         * 显示司机到乘客的路径
         */
        mLbsLayer.driverRoute(driverLocation,
                mStartLocation,
                Color.BLUE,
                new ILbsLayer.OnRouteCompleteListener() {
                    @Override
                    public void onComplete(RouteInfo result) {
                        // 地图聚焦到司机和我的位置
                        mLbsLayer.moveCamera(mStartLocation, driverLocation);
                        // 显示司机、路径信息
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("司机：")
                                .append(mCurrentOrder.getDriverName())
                                .append(", 车牌：")
                                .append(mCurrentOrder.getCarNo())
                                .append("，预计")
                                .append(result.getDuration())
                                .append("分钟到达");


                        mTips.setText(stringBuilder.toString());
                        // 显示操作区
                        showOptArea();
                        // 呼叫不可点击
                        mBtnCall.setEnabled(false);

                    }
                });
    }

    /**
     * 司机到达出发点
     *
     * @param mCurrentOrder
     */
    @Override
    public void showDriverArriveStart(Order mCurrentOrder) {
        showOptArea();
        String arriveTemp = getString(R.string.driver_arrive);
        String arrive = String.format(arriveTemp, mCurrentOrder.getDriverName(), mCurrentOrder.getCarNo());
        mTips.setText(arrive);
        mBtnCall.setEnabled(false);
        mBtnCancel.setEnabled(true);
        //清除地图标记
        mLbsLayer.clearAllMarkers();
        /**
         * 添加司机位置
         */
        LocationInfo driverLocation =
                new LocationInfo(mCurrentOrder.getDriverLatitude(),
                        mCurrentOrder.getDriverLongitude());
        driverLocation.setKey(mCurrentOrder.getKey());
        showLocationChange(driverLocation);
        addLocationMarker();

    }

    /**
     * 开始行程
     *
     * @param mCurrentOrder
     */
    @Override
    public void showStartDriver(Order mCurrentOrder) {
        LocationInfo locationInfo = new LocationInfo(mCurrentOrder.getDriverLatitude()
                , mCurrentOrder.getDriverLongitude());
        //路径线路规划
        updateDriver2EndRoute(locationInfo, mCurrentOrder);
        //隐藏按钮
        mBtnCancel.setVisibility(View.GONE);
        mBtnCall.setVisibility(View.GONE);

    }


    /**
     * 到达目的地
     *
     * @param mCurrentOrder
     */
    @Override
    public void showArriveTarget(Order mCurrentOrder) {
        String tipsTemp = getString(R.string.pay_info);
        String tips = String.format(tipsTemp,
                mCurrentOrder.getCost(),
                mCurrentOrder.getDriverName(),
                mCurrentOrder.getCarNo());
        // 显示操作区
        showOptArea();
        mBtnCancel.setVisibility(View.GONE);
        mBtnCall.setVisibility(View.GONE);
        mTips.setText(tips);
        mBtnPay.setVisibility(View.VISIBLE);
    }

    @Override
    public void updateDriver2startRoutes(LocationInfo locationInfo, final Order mCurrentOrder) {
        mLbsLayer.clearAllMarkers();
        addLocationMarker();
        showLocationChange(locationInfo);
        mLbsLayer.driverRoute(locationInfo, mStartLocation, Color.BLUE, new ILbsLayer.OnRouteCompleteListener() {
            @Override
            public void onComplete(RouteInfo result) {

                String tipsTemp = getString(R.string.accept_info);
                mTips.setText(String.format(tipsTemp,
                        mCurrentOrder.getDriverName(),
                        mCurrentOrder.getCarNo(),
                        result.getDistance(),
                        result.getDuration()));
            }
        });
        // 聚焦
        mLbsLayer.moveCamera(locationInfo, mStartLocation);
    }

    @Override
    public void updateDriver2EndRoute(LocationInfo locationInfo, final Order mCurrentOrder) {
        // 终点位置从 order 中获取
        if (mCurrentOrder.getEndLongitude() != 0 ||
                mCurrentOrder.getEndLatitude() != 0) {
            mEndLocation = new LocationInfo(mCurrentOrder.getEndLatitude(), mCurrentOrder.getEndLongitude());
            mEndLocation.setKey(LOCATION_END);
        }
        mLbsLayer.clearAllMarkers();
        addEndMarker();
        showLocationChange(locationInfo);
        addLocationMarker();
        mLbsLayer.driverRoute(locationInfo, mEndLocation, Color.GREEN, new ILbsLayer.OnRouteCompleteListener() {
            @Override
            public void onComplete(RouteInfo result) {

                String tipsTemp = getString(R.string.driving_info);
                mTips.setText(String.format(tipsTemp,
                        mCurrentOrder.getDriverName(),
                        mCurrentOrder.getCarNo(),
                        result.getDistance(),
                        result.getDuration()));
                // 显示操作区
                showOptArea();
                mBtnCancel.setEnabled(false);
                mBtnCall.setEnabled(false);
            }
        });
        // 聚焦
        mLbsLayer.moveCamera(locationInfo, mEndLocation);
    }

    /**
     * 支付成功
     *
     * @param mCurrentOrder
     */
    @Override
    public void showPaySuc(Order mCurrentOrder) {
        restoreUI();
        ToastUtil.show(this, getString(R.string.pay_suc));
    }

    /**
     * 支付失败
     */
    @Override
    public void showPayFail() {
        restoreUI();
        ToastUtil.show(this, getString(R.string.pay_fail));
    }


    @Override
    public void showLoading() {

    }

    @Override
    public void showErrow(int code, String msg) {

        switch (code) {
            case IAccountManager.TOKEN_INVALID:
                ToastUtil.show(this, getString(R.string.token_invalid));
                showPhoneInputDialoig();
                break;
            case IAccountManager.SERVER_FAIL:
                showPhoneInputDialoig();
                break;
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mLbsLayer.onResume();

    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mLbsLayer.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mLbsLayer.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBus.getInstance().unRegister(mPresenter);
        mLbsLayer.onDestory();
    }

    /**
     * 获取正在进行的订单
     */
    public void getProcessingOrder() {
        if (mIsLogin && mIsLocate) {
            mPresenter.getProcessOrder();
        }
    }
}
