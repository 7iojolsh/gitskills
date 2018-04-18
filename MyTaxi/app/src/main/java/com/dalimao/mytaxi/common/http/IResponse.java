package com.dalimao.mytaxi.common.http;

/**
 * Created by lsh on 2018/4/2.
 */

public interface IResponse  {
    //状态码
    int getCode();
    //数据体
    String getData();
}
