package com.dalimao.mytaxi.account.model.reaponse;

import com.dalimao.mytaxi.common.http.biz.BaseBizResponse;

/**
 * Created by lsh on 2018/4/4.
 */

public class LoginResponse extends BaseBizResponse {
    Account data ;

    public Account getData() {
        return data;
    }

    public void setData(Account data) {
        this.data = data;
    }
}
