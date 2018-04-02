package yyl.diyapp.com.mytexi.common.http.Impl;

import com.google.gson.Gson;

import java.security.Key;
import java.util.HashMap;
import java.util.Map;

import yyl.diyapp.com.mytexi.common.http.IRequest;

/**
 * Created by lsh on 2018/4/2.
 * 封装参数的实现
 */

public class BaseRequest implements IRequest{

    private String method = POST ;
    private String url ;
    private Map<String, String> header  ;
    private Map<String, Object> body  ;

    public BaseRequest(String url) {
        //公共参数  及头部信息
        this.url = url;
        header = new HashMap<>();
        body = new HashMap<>();
        header.put("Application-Id" , "myTaxiID") ;
        header.put("API-Key" , "myTaxiKey") ;
    }

    @Override
    public void setMethod(String method) {
        this.method = method ;
    }

    @Override
    public void setHeader(String key, String value) {
            header.put(key , value) ;
    }

    @Override
    public void setBody(String key, String value) {
        body.put(key , value) ;
    }

    @Override
    public String getUrl() {
        //判断如果是get方法
        if(GET.equals(method)){
            //组装get 请求参数
            for (String Key : body.keySet()) {
                url = url.replace("${"+ Key +"}" , body.get(Key).toString()) ;
            }
        }
        return url;
    }

    @Override
    public Map<String, String> getHeader() {
        return header;
    }

    @Override
    public Object getBody() {
        if(body != null){
            //组装post 方式请求参数
            return new Gson().toJson(this.body  , HashMap.class) ;
        }else{
            return  "{}" ;
        }
    }
}
