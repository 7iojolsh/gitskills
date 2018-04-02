package yyl.diyapp.com.mytexi;

import android.os.SystemClock;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by lsh on 2018/3/30.
 */

@SuppressWarnings("ALL")
public class TestOkHttp3 {
    /**
     * 测试oktttp get 方法
     */
    @Test
    public void testGet() {
        //创建okhttp对象
        OkHttpClient client = new OkHttpClient();
        //创建Request 对象
        Request request = new Request.Builder()
                .url("http://httpbin.org/get?id=id")
                .build();

        //执行request
        try {
            Response response = client.newCall(request).execute();
            System.out.println("response :" + response.body().string());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试oktttp post 方法
     */
    @Test
    public void testPost() {
        //创建okhttp对象
        OkHttpClient client = new OkHttpClient();
        //创建Request 对象
        MediaType type = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(type, "{\"name\":\"lsh\"}");
        Request request = new Request.Builder()
                .url("http://httpbin.org/post")//请求行
                //.header(); //请求头
                .post(body)  //请求体
                .build();

        //执行request
        try {
            Response response = client.newCall(request).execute();
            System.out.println("response :" + response.body().string());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试拦截器
     */
    @Test
    public void textInterceptor() {
        //定义拦截器
        Interceptor interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                long start = System.currentTimeMillis();
                Request request = chain.request();
                Response response = chain.proceed(request);
                long end = System.currentTimeMillis();
                System.out.println("interceptor: cost time =" + (end - start));
                return response;
            }
        };
        //创建okhttp对象
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();
        //创建Request 对象
        Request request = new Request.Builder()
                .url("http://httpbin.org/get?id=id")
                .build();

        //执行request
        try {
            Response response = client.newCall(request).execute();
            System.out.println("response :" + response.body().string());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试缓存
     */
    @Test
    public void testCache() {
        //穿件缓存对想
        Cache cache = new Cache(new File("cache.cache"), 1024 * 1024);

        //创建okhttp对象
        OkHttpClient client = new OkHttpClient.Builder()
                .cache(cache)
                .build();
        //创建Request 对象
        Request request = new Request.Builder()
                .url("http://httpbin.org/get?id=id")
                .cacheControl(CacheControl.FORCE_NETWORK)
                .build();

        //执行request
        try {
            Response response = client.newCall(request).execute();

            Response responseCache = response.cacheResponse();
            Response responseNet = response.networkResponse();
            if (responseCache != null) {
                //从缓存响应
                System.out.println("response from cache");
            }
            if(responseNet != null){
                //从网络响应
                System.out.println("response from net");
            }
            System.out.println("response :" + response.body().string());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
