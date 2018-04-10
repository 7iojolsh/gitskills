package yyl.diyapp.com.mytexi.account.presenter;

/**
 * Created by lsh on 2018/4/8.
 */

public interface ICreatPasswordDialogPresenter {

    /**
     * 检验密码输入是否合法
     * @param pw
     * @param pw1
     */
    boolean checkPw(String pw , String pw1);

    /**
     * 提交注册
     * @param phone
     * @param pwd
     */
    void requestRegister(String phone , String pwd) ;

    /**
     * 登陆
     * @param phone
     * @param pwd
     */
    void requestLogin(String phone , String pwd) ;
}
