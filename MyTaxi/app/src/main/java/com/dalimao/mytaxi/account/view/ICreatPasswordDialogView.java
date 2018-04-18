package com.dalimao.mytaxi.account.view;

/**
 * Created by lsh on 2018/4/8.
 */

public interface ICreatPasswordDialogView extends IView {


    /**
     * 显示注册成功
     */
    void  showRegisterSuc();

    /**
     * 显示登陆成功
     */
    void showLoginSuc() ;


    /**
     * 密码为空
     */
    void showPasswordNull();

    /**
     * 两次密码不相同
     */
    void showPasswordNotEqual();
}
