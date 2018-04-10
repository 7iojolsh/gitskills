package yyl.diyapp.com.mytexi.account.presenter;

import yyl.diyapp.com.mytexi.account.model.IAccountManager;
import yyl.diyapp.com.mytexi.account.model.reaponse.SmsCodeResponse;
import yyl.diyapp.com.mytexi.account.model.reaponse.UserExistResponse;
import yyl.diyapp.com.mytexi.account.view.ISmsCodeDialogView;
import yyl.diyapp.com.mytexi.common.databus.RegisterBus;

/**
 * Created by lsh on 2018/4/8.
 */

public class SmsCodeDialogPresenterImpl implements ISmsCodeDialogPresenter {

    private ISmsCodeDialogView view;
    private IAccountManager accountManager;


    /**
     * 接受消息并处理
     */
    @RegisterBus
    public  void  onSmsCodeDialogRsaponse(SmsCodeResponse smsCodeResponse){
        switch (smsCodeResponse.getCode()){
            case IAccountManager.SMS_SEND_SUC  :
                System.out.println("state = onSmsCodeDialogRsaponse 发送成功");
               view.showCountDownTimer();
                break;
            case IAccountManager.SMS_SEND_FAIL  :
                System.out.println("state = onSmsCodeDialogRsaponse 发送失败");
               view.showErrow(IAccountManager.SMS_SEND_FAIL ,"");
                break;
            case IAccountManager.SMS_CHECK_SUC  :
                System.out.println("state = onSmsCodeDialogRsaponse 发送失败");
                view.showSmsCodeCheckState(true) ;
                break;
            case IAccountManager.SMS_CHECK_FAIL  :
                System.out.println("state = onSmsCodeDialogRsaponse 发送失败");
                view.showSmsCodeCheckState(false) ;
                break;
        }
    }
    @RegisterBus
    public void onSmsCodeResponse(UserExistResponse userExistResponse) {
        switch (userExistResponse.getCode()) {

            case IAccountManager.USER_EXIST:
                view.showUserExist(true);
                break;
            case IAccountManager.USER_NOT_EXIST:
                view.showUserExist(false);
                break;
            case IAccountManager.SERVER_FAIL:
                view.showErrow(IAccountManager.SERVER_FAIL, "");
                break;

        }
    }
    public SmsCodeDialogPresenterImpl(ISmsCodeDialogView view,
                                      IAccountManager accountManager) {
        this.view = view;
        this.accountManager = accountManager;
    }

    /**
     * 获取验证码
     *
     * @param phone
     */
    @Override
    public void requestSendSmsCode(String phone) {
        accountManager.fetchSMSCode(phone);
    }

    /**
     * 验证验证码
     *
     * @param phone
     * @param smsCode
     */
    @Override
    public void requestCheckSmsCode(String phone, String smsCode) {
        accountManager.checkSMSCode(phone , smsCode);
    }

    /**
     * 检查用户是否存在
     * @param phone
     */
    @Override
    public void requestCheckUserExist(String phone) {
        accountManager.checkUserExist(phone);
    }
}
