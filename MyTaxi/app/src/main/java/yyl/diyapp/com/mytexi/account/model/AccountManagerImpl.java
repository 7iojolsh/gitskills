package yyl.diyapp.com.mytexi.account.model;

import android.os.Handler;
import android.util.Log;

import com.google.gson.Gson;

import rx.functions.Func1;
import yyl.diyapp.com.mytexi.MyApplication;
import yyl.diyapp.com.mytexi.account.model.reaponse.Account;
import yyl.diyapp.com.mytexi.account.model.reaponse.LoginResponse;
import yyl.diyapp.com.mytexi.account.model.reaponse.RegisterResponse;
import yyl.diyapp.com.mytexi.account.model.reaponse.SmsCodeResponse;
import yyl.diyapp.com.mytexi.account.model.reaponse.UserExistResponse;
import yyl.diyapp.com.mytexi.common.databus.RxBus;
import yyl.diyapp.com.mytexi.common.http.IHttpClient;
import yyl.diyapp.com.mytexi.common.http.IRequest;
import yyl.diyapp.com.mytexi.common.http.IResponse;
import yyl.diyapp.com.mytexi.common.http.Impl.BaseRequest;
import yyl.diyapp.com.mytexi.common.http.Impl.BaseResponse;
import yyl.diyapp.com.mytexi.common.http.api.API;
import yyl.diyapp.com.mytexi.common.http.biz.BaseBizResponse;
import yyl.diyapp.com.mytexi.common.storage.SharedPreferencesDao;
import yyl.diyapp.com.mytexi.common.utils.DevUtil;
import yyl.diyapp.com.mytexi.common.utils.LogUtil;

/**
 * Created by lsh on 2018/4/8.
 */

public class AccountManagerImpl implements IAccountManager {
    private static final String TAG = "AccountManagerImpl";

    //网路请求库
    private IHttpClient httpClient;
    //数据存储
    private SharedPreferencesDao sharedPreferencesDao;
    //发送消息handler
    private Handler myHandler;

    public AccountManagerImpl(IHttpClient mHttpClient,
                              SharedPreferencesDao sharedPreferencesDao) {
        this.httpClient = mHttpClient;
        this.sharedPreferencesDao = sharedPreferencesDao;
    }

    /**
     * 获取验证码
     *
     * @param phone
     */
    @Override
    public void fetchSMSCode(final String phone) {

        RxBus.getInstance().chainProcess(new Func1() {
            @Override
            public Object call(Object o) {

                String url = API.Config.getDomain() + API.GET_SMS_CODE;
                IRequest request = new BaseRequest(url);
                request.setBody("phone", phone);
                IResponse response = httpClient.get(request, false);
                Log.d(TAG, response.getData());
                SmsCodeResponse smsCodeResponse = new SmsCodeResponse();
                LogUtil.d(TAG, response.getData());
                if (response.getCode() == BaseResponse.STATE_OK) {
                    BaseBizResponse bizRes =
                            new Gson().fromJson(response.getData(),
                                    BaseBizResponse.class);
                    if (bizRes.getCode() == BaseBizResponse.STATE_OK) {
                        smsCodeResponse.setCode(SMS_SEND_SUC);
                    } else {
                        smsCodeResponse.setCode(SMS_SEND_FAIL);
                    }
                } else {
                    smsCodeResponse.setCode(SMS_SEND_FAIL);
                }
                return smsCodeResponse;
            }
        });
    }

    /**
     * 校验验证码
     *
     * @param phone
     * @param smsCode
     */
    @Override
    public void checkSMSCode(final String phone, final String smsCode) {
        RxBus.getInstance().chainProcess(new Func1() {
            @Override
            public Object call(Object o) {

                String url = API.Config.getDomain() + API.CHECK_SMS_CODE;
                IRequest request = new BaseRequest(url);
                request.setBody("phone", phone);
                request.setBody("code", smsCode);
                IResponse response = httpClient.get(request, false);
                Log.d(TAG, response.getData());
                SmsCodeResponse smsCodeResponse = new SmsCodeResponse();

                if (response.getCode() == BaseResponse.STATE_OK) {
                    BaseBizResponse bizRes =
                            new Gson().fromJson(response.getData(), BaseBizResponse.class);
                    if (bizRes.getCode() == BaseBizResponse.STATE_OK) {
                        smsCodeResponse.setCode(SMS_CHECK_SUC);
                    } else {
                        smsCodeResponse.setCode(SMS_CHECK_FAIL);
                    }
                } else {
                    smsCodeResponse.setCode(SMS_CHECK_FAIL);
                }
                return smsCodeResponse;
            }
        });
    }

    /**
     * 检查用户是否存在
     *
     * @param phone 用户电话号码
     */
    @Override
    public void checkUserExist(final String phone) {
        RxBus.getInstance().chainProcess(new Func1() {
            @Override
            public Object call(Object o) {
                String url = API.Config.getDomain() + API.CHECK_USER_EXIST;
                IRequest request = new BaseRequest(url);
                request.setBody("phone", phone);
                IResponse response = httpClient.get(request, false);
                Log.d(TAG, response.getData());
                UserExistResponse existResponse = new UserExistResponse();
                if (response.getCode() == BaseResponse.STATE_OK) {
                    BaseBizResponse bizRes =
                            new Gson().fromJson(response.getData(),
                                    BaseBizResponse.class);
                    if (bizRes.getCode() == BaseBizResponse.STATE_USER_EXIST) {
                        existResponse.setCode(USER_EXIST);
                    } else if (bizRes.getCode() ==
                            BaseBizResponse.STATE_USER_NOT_EXIST) {
                        existResponse.setCode(USER_NOT_EXIST);
                    }
                } else {
                    existResponse.setCode(SERVER_FAIL);
                }
                return existResponse;
            }
        });
    }

    /**
     * 注册
     *
     * @param phone 手机号
     * @param pwd   密码
     */
    @Override
    public void register(final String phone, final String pwd) {
        RxBus.getInstance().chainProcess(new Func1() {
            @Override
            public Object call(Object o) {
                String url = API.Config.getDomain() + API.REGISTER;
                IRequest request = new BaseRequest(url);
                request.setBody("phone", phone);
                request.setBody("password", pwd);
                request.setBody("uid", DevUtil.UUID(MyApplication.getInstance()));

                IResponse response = httpClient.post(request, false);
                Log.d(TAG, response.getData());

                RegisterResponse registerResponse = new RegisterResponse();
                if (response.getCode() == BaseResponse.STATE_OK) {
                    BaseBizResponse bizRes =
                            new Gson().fromJson(response.getData(),
                                    BaseBizResponse.class);
                    if (bizRes.getCode() == BaseBizResponse.STATE_OK) {
                        registerResponse.setCode(REGISTER_SUC);
                    } else {
                        registerResponse.setCode(SERVER_FAIL);
                    }
                } else {
                    registerResponse.setCode(SERVER_FAIL);
                }
                return registerResponse;
            }
        });
    }

    /**
     * 登陆
     *
     * @param phone 手机号
     * @param pwd   密码
     */
    @Override
    public void login(final String phone, final String pwd) {
        RxBus.getInstance().chainProcess(new Func1() {
            @Override
            public Object call(Object o) {
                String url = API.Config.getDomain() + API.LOGIN;
                IRequest request = new BaseRequest(url);
                request.setBody("phone", phone);
                request.setBody("password", pwd);


                IResponse response = httpClient.post(request, false);
                Log.d(TAG, response.getData());

                LoginResponse bizRes = new LoginResponse();
                if (response.getCode() == BaseResponse.STATE_OK) {
                    bizRes = new Gson().fromJson(response.getData(), LoginResponse.class);
                    if (bizRes.getCode() == BaseBizResponse.STATE_OK) {
                        // 保存登录信息
                        Account account = bizRes.getData();
                        sharedPreferencesDao.save(SharedPreferencesDao.KEY_ACCOUNT, account);
                        // 通知 UI
                        bizRes.setCode(LOGIN_SUC);
                    } else if (bizRes.getCode() == BaseBizResponse.STATE_PWD_ERR) {
                        bizRes.setCode(PW_ERROR);
                    } else {
                        bizRes.setCode(SERVER_FAIL);
                    }
                } else {
                    bizRes.setCode(SERVER_FAIL);
                }
                return bizRes;
            }
        });
    }

    /**
     * token登陆
     */
    @Override
    public void loginByToken() {
        RxBus.getInstance().chainProcess(new Func1() {
            @Override
            public Object call(Object o) {
                // 获取本地登录信息
                Account account =
                        (Account) sharedPreferencesDao.get(SharedPreferencesDao.KEY_ACCOUNT,
                                Account.class);


                // 登录是否过期
                boolean tokenValid = false;

                // 检查token是否过期

                if (account != null) {
                    if (account.getExpired() > System.currentTimeMillis()) {
                        // token 有效
                        tokenValid = true;
                    }
                }

                LoginResponse loginResponse = new LoginResponse();
                if (!tokenValid) {
                    loginResponse.setCode(TOKEN_INVALID);
                    return loginResponse;
                }


                // 请求网络，完成自动登录
                String url = API.Config.getDomain() + API.LOGIN_BY_TOKEN;
                IRequest request = new BaseRequest(url);
                request.setBody("token", account.getToken());
                IResponse response = httpClient.post(request, false);
                Log.d(TAG, response.getData());
                if (response.getCode() == BaseResponse.STATE_OK) {
                    loginResponse =
                            new Gson().fromJson(response.getData(), LoginResponse.class);
                    if (loginResponse.getCode() == BaseBizResponse.STATE_OK) {
                        // 保存登录信息
                        account = loginResponse.getData();
                        // todo: 加密存储
                        sharedPreferencesDao.save(SharedPreferencesDao.KEY_ACCOUNT, account);
                        loginResponse.setCode(LOGIN_SUC);
                    } else
                    if (loginResponse.getCode() == BaseBizResponse.STATE_TOKEN_INVALID) {
                        loginResponse.setCode(TOKEN_INVALID);
                    }
                } else {
                    loginResponse.setCode(SERVER_FAIL);
                }
                return loginResponse;
            }
        });
    }
}