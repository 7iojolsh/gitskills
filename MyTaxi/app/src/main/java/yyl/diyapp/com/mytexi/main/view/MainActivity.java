package yyl.diyapp.com.mytexi.main.view;

import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import yyl.diyapp.com.mytexi.MyApplication;
import yyl.diyapp.com.mytexi.R;
import yyl.diyapp.com.mytexi.account.model.IAccountManager;
import yyl.diyapp.com.mytexi.account.model.AccountManagerImpl;
import yyl.diyapp.com.mytexi.account.view.PhoneInputDialog;
import yyl.diyapp.com.mytexi.account.model.reaponse.Account;
import yyl.diyapp.com.mytexi.common.Lbs.GaodeLbsLayerImpl;
import yyl.diyapp.com.mytexi.common.Lbs.ILbsLayer;
import yyl.diyapp.com.mytexi.common.Lbs.LocationInfo;
import yyl.diyapp.com.mytexi.common.databus.RxBus;
import yyl.diyapp.com.mytexi.common.http.IHttpClient;
import yyl.diyapp.com.mytexi.common.http.Impl.OkHttpClientImpl;
import yyl.diyapp.com.mytexi.common.storage.SharedPreferencesDao;
import yyl.diyapp.com.mytexi.common.utils.ToastUtil;
import yyl.diyapp.com.mytexi.main.presenter.IMainPresenter;
import yyl.diyapp.com.mytexi.main.presenter.MainPresenterImpl;

/**
 * 1 检查本地记录(登陆状态检查)
 * 2 若用户没有登陆则登陆
 * 3 登陆之前先校验手机号码
 * <p>
 * 地图初始化
 * 1 接入地图
 * 2 定位自己的位置,显示定位蓝点
 * 3 使用marker 改变当前位置和方向
 */
public class MainActivity extends AppCompatActivity implements IMainView{

    private static final String TAG = "MainActivity";
    private IMainPresenter mPresenter;
    private ILbsLayer mLbsLayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IHttpClient iHttpClient = new OkHttpClientImpl();
        SharedPreferencesDao preferencesDao = new SharedPreferencesDao(MyApplication.getInstance(),
                SharedPreferencesDao.FILE_ACCOUNT);
        AccountManagerImpl accountManager = new AccountManagerImpl(iHttpClient, preferencesDao);
        mPresenter = new MainPresenterImpl(this, accountManager);
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
                //首次定位 , 添加 当前位置的标记
                mLbsLayer.addOrUpdateMarker(locationInfo
                        , BitmapFactory.decodeResource(getResources(),
                                R.mipmap.navi_map_gps_locked));
            }
        });

        ViewGroup viewGroup = (ViewGroup) findViewById(R.id.activity_main);
        viewGroup.addView(mLbsLayer.getMapView());
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
        mLbsLayer.onSavaInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBus.getInstance().unRegister(mPresenter);
        mLbsLayer.onDestory();
    }



}
