package com.dalimao.mytaxi.account.presenter;

/**
 * Created by lsh on 2018/4/8.
 */

public interface ISmsCodeDialogPresenter {

    /**
     * 请求下发验证码
     * @param phone
     */
    void requestSendSmsCode(String phone);

    /**
     * 请求校验验证码
     * @param phone
     * @param smsCode
     */
    void requestCheckSmsCode(String phone, String smsCode);


    /**
     * 检查用户是否存在
     * @param phone
     */
    void requestCheckUserExist(String phone);


}
