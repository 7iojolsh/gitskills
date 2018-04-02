package yyl.diyapp.com.mytexi.common.http.api;

/**
 * Created by lsh on 2018/4/2.
 */

public class API {
    public  static  final String  TEST_GET  ="/get?uid=${uid}" ;
    public  static  final String  TEST_POST  ="/post" ;

    /**
     * 配置域名信息
     */
    public static class Config{
        private static final String TEST_DOMAIN = "http://httpbin.org";
        private static final String RELEASE_DOMAIN = "http://httpbin.org";
        private static String domain = "TEST_DOMAIN" ;
        public static void setDebug(boolean debug){
            domain = debug ? TEST_DOMAIN :RELEASE_DOMAIN ;

        }

        public  static  String getDomain(){
            return domain ;
        }
    }
}
