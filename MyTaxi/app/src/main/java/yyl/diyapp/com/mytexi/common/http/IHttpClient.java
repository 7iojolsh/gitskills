package yyl.diyapp.com.mytexi.common.http;

/**
 * HttpClient 抽象接口
 * Created by lsh on 2018/4/2.
 */

public interface IHttpClient {
    IResponse get(IRequest request , boolean forceCache);
    IResponse post(IRequest request , boolean forceCache) ;
}
