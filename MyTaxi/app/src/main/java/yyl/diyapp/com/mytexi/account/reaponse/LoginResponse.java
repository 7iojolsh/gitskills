package yyl.diyapp.com.mytexi.account.reaponse;

import yyl.diyapp.com.mytexi.common.http.biz.BaseBizResporse;

/**
 * Created by lsh on 2018/4/4.
 */

public class LoginResponse extends BaseBizResporse {
    Account data ;

    public Account getData() {
        return data;
    }

    public void setData(Account data) {
        this.data = data;
    }
}
