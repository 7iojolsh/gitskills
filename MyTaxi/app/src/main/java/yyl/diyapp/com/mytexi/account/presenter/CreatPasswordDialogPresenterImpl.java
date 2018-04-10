package yyl.diyapp.com.mytexi.account.presenter;

import yyl.diyapp.com.mytexi.account.model.IAccountManager;
import yyl.diyapp.com.mytexi.account.model.reaponse.LoginResponse;
import yyl.diyapp.com.mytexi.account.model.reaponse.RegisterResponse;
import yyl.diyapp.com.mytexi.account.view.ICreatPasswordDialogView;
import yyl.diyapp.com.mytexi.common.databus.RegisterBus;

/**
 * Created by lsh on 2018/4/8.
 */

public class CreatPasswordDialogPresenterImpl implements ICreatPasswordDialogPresenter{
    private ICreatPasswordDialogView view ;
    private IAccountManager accountManager ;

    public CreatPasswordDialogPresenterImpl(ICreatPasswordDialogView creatPasswordDialogView,
                                            IAccountManager accountManager) {
        this.view = creatPasswordDialogView;
        this.accountManager = accountManager;
    }

    @RegisterBus
    public void onLoginResponse(LoginResponse LoginResponse) {
        // 处理UI 变化
        switch (LoginResponse.getCode()) {
            case IAccountManager.LOGIN_SUC:
                // 登录成功
                view.showLoginSuc();
                break;
            case IAccountManager.SERVER_FAIL:
                // 服务器错误
                view.showErrow(IAccountManager.SERVER_FAIL, "");
                break;
        }

    }
    @RegisterBus
    public  void  onCreatDialogResponse(RegisterResponse registerResponse){
        switch (registerResponse.getCode()){
            //注册成功
            case IAccountManager.REGISTER_SUC:
                System.out.println("state = " +  "注册成功");
                view.showRegisterSuc();
                break;

            //服务错误
            case IAccountManager.SERVER_FAIL:
                System.out.println("state = " +  "服务错误");
                view.showErrow(IAccountManager.SERVER_FAIL ,"");
                break;
        }

    }
    /**
     * 检查密码是否正确
     * @param pw
     * @param pw1
     */
    @Override
    public boolean checkPw(String pw, String pw1) {
        if (pw == null || pw.equals("")) {

            view.showPasswordNull();
            return false;
        }else if(pw1 == null || pw1.equals("")){
            view.showPasswordNull();
            return false;
        }
        if (!pw.equals(pw1)) {

            view.showPasswordNotEqual();
            return false;
        }
        return true;
    }

    /**
     * 注册
     * @param phone
     * @param pwd
     */
    @Override
    public void requestRegister(String phone, String pwd) {
        accountManager.register(phone , pwd);
    }

    /**
     * 登陆
     * @param phone
     * @param pwd
     */
    @Override
    public void requestLogin(String phone, String pwd) {
        accountManager.login(phone  , pwd);
    }


}
