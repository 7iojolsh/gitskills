package com.dalimao.mytaxi.common.http.Impl;

import com.dalimao.mytaxi.common.http.IResponse;

/**
 * Created by lsh on 2018/4/2.
 *
 */

public class BaseResponse implements IResponse {
    public static final int STATE_INKNOWN_ERROR = 100001 ;
    public static final int STATE_OK = 200;
    //状态码
    private int code ;
    //响应数据
    private String data ;


    @Override
    public String getData() {
        return data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setData(String data) {
        this.data = data;
    }
}
