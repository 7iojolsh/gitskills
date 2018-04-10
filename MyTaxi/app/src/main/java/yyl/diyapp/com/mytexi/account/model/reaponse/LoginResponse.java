package yyl.diyapp.com.mytexi.account.model.reaponse;

import yyl.diyapp.com.mytexi.common.http.biz.BaseBizResponse;

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
