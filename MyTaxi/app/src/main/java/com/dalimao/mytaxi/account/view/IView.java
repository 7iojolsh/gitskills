package com.dalimao.mytaxi.account.view;

/**
 * Created by lsh on 2018/4/8.
 */

public interface IView {
    /**
     * 显示Loading
     */
    void showLoading();
    /**
     * 显示错误
     */
    void showErrow(int code, String msg);
}
