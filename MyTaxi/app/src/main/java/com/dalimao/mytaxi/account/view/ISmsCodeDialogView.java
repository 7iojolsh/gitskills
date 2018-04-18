package com.dalimao.mytaxi.account.view;

/**
 * Created by lsh on 2018/4/8.
 */

public interface ISmsCodeDialogView extends IView {

    /**
     * 显示倒计时
     */
    void showCountDownTimer();



    /**
     * 显示验证码状态
     * @param b
     */
    void showSmsCodeCheckState(boolean b);

    /**
     * 显示用户是否存在
     * @param b
     */
    void showUserExist(boolean b);
}
