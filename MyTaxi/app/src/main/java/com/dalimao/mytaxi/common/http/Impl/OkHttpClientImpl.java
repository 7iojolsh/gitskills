package com.dalimao.mytaxi.common.http.Impl;

import com.dalimao.mytaxi.common.http.IHttpClient;
import com.dalimao.mytaxi.common.http.IRequest;
import com.dalimao.mytaxi.common.http.IResponse;

import java.io.IOException;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * okHttp 的实现
 * Created by lsh on 2018/4/2.
 */

public class OkHttpClientImpl implements IHttpClient {
    OkHttpClient mOkHttpClient = new OkHttpClient.Builder().build() ;
    @Override
    public IResponse get(IRequest request, boolean forceCache) {
        //解析成okhttp 需要的参数
        //指定请求方式
        request.setMethod(IRequest.GET);
        //解析头部
        Map<String, String> header = request.getHeader();
        //创建 Request.Builder 对象
        Request.Builder builder = new Request.Builder() ;
        for (String key : header.keySet()) {
            //组装okhttp 的 header
            builder.header(key , header.get(key)) ;
        }
        //获取url
        String url = request.getUrl();
        builder.url(url).get();
        Request okRequest = builder.build();
        //执行 okRequest
        return execute(okRequest);
    }


    @Override
    public IResponse post(IRequest request, boolean forceCache) {
        //设置数据类型
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8") ;
        //设置请求体
        RequestBody requestBody = RequestBody.create(mediaType , request.getBody().toString()) ;
        request.setMethod(IRequest.POST);
        Map<String, String> header = request.getHeader();
        Request.Builder builder = new Request.Builder();
        for (String key : header.keySet()) {
            builder.header(key , header.get(key));
        }
        builder.url(request.getUrl())
                .post(requestBody);
        Request requestPost = builder.build();
        return execute(requestPost);
    }

    /**
     * 请求执行过程
     * @param request
     * @return
     */
    private IResponse execute(Request request) {

        BaseResponse commonResponse = new BaseResponse();

        try {
            Response response = mOkHttpClient.newCall(request).execute();
            //设置状态码
            commonResponse.setCode(response.code());
            String body = response.body().string();
            //设置响应数据
            commonResponse.setData(body);

        } catch (IOException e) {
            e.printStackTrace();
            commonResponse.setCode(BaseResponse.STATE_INKNOWN_ERROR);
        }

        return commonResponse ;
    }


}
