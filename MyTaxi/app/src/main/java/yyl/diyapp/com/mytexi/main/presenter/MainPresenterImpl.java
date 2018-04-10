package yyl.diyapp.com.mytexi.main.presenter;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.ref.WeakReference;

import yyl.diyapp.com.mytexi.account.model.IAccountManager;
import yyl.diyapp.com.mytexi.account.model.reaponse.Account;
import yyl.diyapp.com.mytexi.account.model.reaponse.LoginResponse;
import yyl.diyapp.com.mytexi.common.databus.RegisterBus;
import yyl.diyapp.com.mytexi.main.view.IMainView;

/**
 * Created by lsh on 2018/4/8.
 */

public class MainPresenterImpl implements IMainPresenter {
    private IMainView view ;
    private IAccountManager accountManager ;

    @RegisterBus
    public  void  onMainRequset(LoginResponse loginResponse){
        switch (loginResponse.getCode()){
            //token失效
            case IAccountManager.TOKEN_INVALID :
                view.showErrow(IAccountManager.LOGIN_SUC  ,"" );
                break;
            //登陆成功
            case IAccountManager.LOGIN_SUC:
                view.showLoginSuc();
                break;
            //服务器错误
            case IAccountManager.SERVER_FAIL:
                view.showErrow(IAccountManager.SERVER_FAIL  ,"" );
                break;
        }
    }

    public static class MyHandler extends Handler{
        WeakReference<MainPresenterImpl> reference ;

        public MyHandler(MainPresenterImpl mainPresenter) {
            reference = new WeakReference<MainPresenterImpl>(mainPresenter) ;
        }

        @Override
        public void handleMessage(Message msg) {
            MainPresenterImpl mPresenter = reference.get();

            switch (msg.what){
                //token失效
                case IAccountManager.TOKEN_INVALID :
                    mPresenter.view.showErrow(IAccountManager.LOGIN_SUC  ,"" );
                    break;
                //登陆成功
                case IAccountManager.LOGIN_SUC:
                    mPresenter.view.showLoginSuc();
                    break;
                //服务器错误
                case IAccountManager.SERVER_FAIL:
                    mPresenter.view.showErrow(IAccountManager.SERVER_FAIL  ,"" );
                    break;
            }

        }
    }

    public MainPresenterImpl(IMainView view, IAccountManager accountManager) {
        this.view = view;
        this.accountManager = accountManager;
    }

    @Override
    public void LoginByToken() {
        accountManager.loginByToken();
    }
}
