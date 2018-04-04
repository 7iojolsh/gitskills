package yyl.diyapp.com.mytexi.main;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;

import yyl.diyapp.com.mytexi.MyApplication;
import yyl.diyapp.com.mytexi.R;
import yyl.diyapp.com.mytexi.account.PhoneInputDialog;
import yyl.diyapp.com.mytexi.account.reaponse.Account;
import yyl.diyapp.com.mytexi.account.reaponse.LoginResponse;
import yyl.diyapp.com.mytexi.common.http.IHttpClient;
import yyl.diyapp.com.mytexi.common.http.IRequest;
import yyl.diyapp.com.mytexi.common.http.IResponse;
import yyl.diyapp.com.mytexi.common.http.Impl.BaseRequest;
import yyl.diyapp.com.mytexi.common.http.Impl.BaseResponse;
import yyl.diyapp.com.mytexi.common.http.Impl.OkHttpClientImpl;
import yyl.diyapp.com.mytexi.common.http.api.API;
import yyl.diyapp.com.mytexi.common.storage.SharedPreferencesDao;
import yyl.diyapp.com.mytexi.common.utils.ToastUtil;

/**
 * 1 检查本地记录(登陆状态检查)
 * 2 若用户没有登陆则登陆
 * 3 登陆之前先校验手机号码
 * 4 token 有效使用 token 自动登陆
 * todo : 地图初始化
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private IHttpClient mHttpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHttpClient = new OkHttpClientImpl() ;
        checkLoginState();
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
            // 网络请求 , 完成自动登陆
            new Thread() {
                @Override
                public void run() {
                    String url = API.Config.getDomain() + API.LOGIN_BY_TOKEN;
                    IRequest request = new BaseRequest(url);
                    request.setBody("token", account.getToken());
                    IResponse response = mHttpClient.post(request, false);
                    Log.d(TAG, response.getData());
                    if (response.getCode() == BaseResponse.STATE_OK) {
                        LoginResponse bizRes =
                                new Gson().fromJson(response.getData(), LoginResponse.class);
                        if (bizRes.getCode() == LoginResponse.STATE_OK) {
                            //保存登陆信息
                            Account account = bizRes.getData();
                            // Todo : 加个密存储
                            SharedPreferencesDao preferencesDao =
                                    new SharedPreferencesDao(MyApplication.getInstance(),
                                            SharedPreferencesDao.FILE_ACCOUNT);
                            //保存用户对象
                            preferencesDao.save(SharedPreferencesDao.KEY_ACCOUNT, account);
                            //通知ui
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ToastUtil.show(MainActivity.this ,getString(R.string.login_suc));
                                }
                            });
                        } else if (bizRes.getCode() == LoginResponse.STATE_TOKEN_INVALID) {
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showPhoneInputDialoig();
                                }
                            });
                        }
                    } else {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.show(MainActivity.this ,getString(R.string.error_server));
                            }
                        });
                    }
                }
            }.start();

        }


    }

    /**
     * 显示手机输入框
     */
    private void showPhoneInputDialoig() {
        PhoneInputDialog dialog = new PhoneInputDialog(this);
        dialog.show();
    }
}
