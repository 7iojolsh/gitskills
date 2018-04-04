package yyl.diyapp.com.mytexi.common.http.biz;

/**
 * Created by lsh on 2018/4/3.
 * 返回业务的公共格式
 */

public class BaseBizResporse {
    public static final int STATE_OK = 200 ;
    //用户已经存在
    public static final int STATE_USER_EXIST  = 100003;
    //用户不存在
    public static final int STATE_USER_NOT_EXIST = 100002;
    //密码错误
    public static final int STATE_PWD_ERR = 100005;
    //token 过期
    public static final int STATE_TOKEN_INVALID = 100006;
    //状态码
    private int code ;
    private String msg ;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
