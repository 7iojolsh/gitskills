package yyl.diyapp.com.mytexi.main.view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import yyl.diyapp.com.mytexi.MyApplication;
import yyl.diyapp.com.mytexi.R;
import yyl.diyapp.com.mytexi.account.model.IAccountManager;
import yyl.diyapp.com.mytexi.account.model.AccountManagerImpl;
import yyl.diyapp.com.mytexi.account.view.PhoneInputDialog;
import yyl.diyapp.com.mytexi.account.model.reaponse.Account;
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
 * todo : 地图初始化
 */
public class MainActivity extends AppCompatActivity implements IMainView {

    private static final String TAG = "MainActivity";
    private IMainPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IHttpClient iHttpClient  = new OkHttpClientImpl();
        SharedPreferencesDao preferencesDao = new SharedPreferencesDao(MyApplication.getInstance(),
                SharedPreferencesDao.FILE_ACCOUNT);
        AccountManagerImpl accountManager = new AccountManagerImpl(iHttpClient ,preferencesDao);
        mPresenter = new MainPresenterImpl(this, accountManager);
        checkLoginState();
        RxBus.getInstance().register(mPresenter);
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

        switch (code){
            case IAccountManager.TOKEN_INVALID:
                ToastUtil.show(this , getString(R.string.token_invalid));
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBus.getInstance().unRegister(mPresenter);
    }
}
