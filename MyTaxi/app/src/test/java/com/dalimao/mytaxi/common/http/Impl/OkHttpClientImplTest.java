package com.dalimao.mytaxi.common.http.Impl;

import org.junit.Before;
import org.junit.Test;

import yyl.com.dalimao.mytaxi.common.http.IHttpClient;
import yyl.com.dalimao.mytaxi.common.http.IRequest;
import yyl.com.dalimao.mytaxi.common.http.IResponse;
import yyl.com.dalimao.mytaxi.common.http.Impl.BaseRequest;
import yyl.com.dalimao.mytaxi.common.http.Impl.OkHttpClientImpl;
import yyl.com.dalimao.mytaxi.common.http.api.API;

/**
 * Created by lsh on 2018/4/2.
 */
public class OkHttpClientImplTest {
    IHttpClient httpClient ;
    @Before
    public void setUp() throws Exception {
        httpClient = new OkHttpClientImpl() ;
        API.Config.setDebug(false);
    }

    @Test
    public void get() throws Exception {
        //Request 参数
        String url =API.Config.getDomain()+ API.TEST_GET ;
        IRequest request = new BaseRequest(url) ;
        request.setBody("uid" , "123456");
        request.setHeader("testHeader" , "text header");
        IResponse iResponse = httpClient.get(request, false);
        System.out.println("stateCode =" + iResponse.getCode());
        System.out.println("stateBody =" + iResponse.getData());
    }

    @Test
    public void post() throws Exception {
        //Request 参数
        String url =API.Config.getDomain()+ API.TEST_POST ;
        IRequest request = new BaseRequest(url) ;
        request.setBody("uid" , "123456");
        request.setHeader("testHeader" , "text header");
        IResponse iResponse = httpClient.post(request, false);
        System.out.println("stateCode =" + iResponse.getCode());
        System.out.println("stateBody =" + iResponse.getData());
    }

}
