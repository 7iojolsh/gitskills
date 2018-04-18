package com.dalimao.mytaxi.common.http;

import java.util.Map;

/**
 * Created by lsh on 2018/4/2.
 * 定义请求数据的封装方式
 */

public interface IRequest {
    //请求方式
    public static final String POST = "POST";
    public static final String GET = "GET";

    /**
     * 指定请求方式
     *
     * @param method post 或是 get ....
     */
    void setMethod(String method);

    /**
     * 情定请求头部分
     * @param key
     * @param value
     */
    void setHeader(String key, String value);

    /**
     * 指定请求参数
     * @param key
     * @param valu
     */
    void setBody(String key, String valu) ;

    /**
     * 提供给执行库请求行url
     * @return
     */
    String getUrl() ;
    /**
     * 提供给执行库请求行头部
     * @return
     */
    Map<String, String> getHeader() ;

    /**
     * 提供给执行库请求行参数
     *
     * @return
     */
    Object getBody();


}
