package yyl.diyapp.com.mytexi.account.presenter;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

import yyl.diyapp.com.mytexi.account.model.IAccountManager;
import yyl.diyapp.com.mytexi.account.model.reaponse.LoginResponse;
import yyl.diyapp.com.mytexi.account.view.ILoginDialogView;
import yyl.diyapp.com.mytexi.common.databus.RegisterBus;

/**
 * Created by lsh on 2018/4/8.
 */

public class LoginDialogPresenterImpl implements ILoginDialogPresenter {

    //传入view
    private ILoginDialogView view ;
    //传入model
    private IAccountManager  accountManager ;

    public LoginDialogPresenterImpl(ILoginDialogView view, IAccountManager accountManager) {
        this.view = view;
        this.accountManager = accountManager;
    }

    @Override
    public void requestLogin(String phone, String pwd) {
        accountManager.login(phone , pwd);
    }

    @RegisterBus
    public  void  onLoginResponse(LoginResponse response){
        switch (response.getCode()){
            //登陆成功
            case IAccountManager.LOGIN_SUC:
                view.showLoginSuc();
                break;
            //密码错误
            case IAccountManager.PW_ERROR:
               view.showErrow(IAccountManager.PW_ERROR  ,"");
                break;
            //服务异常
            case IAccountManager.SERVER_FAIL:
               view.showErrow(IAccountManager.SERVER_FAIL  ,"");
                break;
        }
    }
}
