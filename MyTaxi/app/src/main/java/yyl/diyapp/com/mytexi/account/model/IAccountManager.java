package yyl.diyapp.com.mytexi.account.model;

import android.os.Handler;

/**
 * Created by lsh on 2018/4/8.
 * 账号相关业务逻辑抽象
 */

public interface IAccountManager {
    //注册成功
    static final int REGISTER_SUC = 4;
    //服务器错误
     static final int SERVER_FAIL = -999;
    //登陆成功
     static final int LOGIN_SUC = 5;
    //登陆失败
     static final int TOKEN_INVALID = -6;
    //密码错误
     static final int PW_ERROR = 3;
    //验证码发送成功
     static final int SMS_SEND_SUC = 1;
    //验证码发送失败
     static final int SMS_SEND_FAIL = -1;
    //验证码校验成功
     static final int SMS_CHECK_SUC = 2;
    //验证码校验失败
     static final int SMS_CHECK_FAIL = -2;
    //用户存在
     static final int USER_EXIST = 3;
    //用户不存在
     static final int USER_NOT_EXIST = -3;
    /**
     * 下发验证码
     */
    void fetchSMSCode(String phone);

    /**
     * 校验验证码
     */
    void checkSMSCode(String phone ,String smsCode);

    /**
     * 用户是否存在接口
     * @param phone 用户电话号码
     */
    void checkUserExist(String phone);

    /**
     * 注册接口
     * @param phone 手机号
     * @param pwd 密码
     */
    void register(String phone , String pwd);

    /**
     * 登陆接口
     * @param phone 手机号
     * @param pwd 密码
     */
    void login(String phone , String pwd) ;

    /**
     * token 登陆
     */
    void loginByToken();

}
