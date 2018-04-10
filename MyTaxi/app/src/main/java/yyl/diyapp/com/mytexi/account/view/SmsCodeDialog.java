package yyl.diyapp.com.mytexi.account.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dalimao.corelibrary.VerificationCodeInput;

import yyl.diyapp.com.mytexi.MyApplication;
import yyl.diyapp.com.mytexi.R;
import yyl.diyapp.com.mytexi.account.model.IAccountManager;
import yyl.diyapp.com.mytexi.account.model.AccountManagerImpl;
import yyl.diyapp.com.mytexi.account.presenter.ISmsCodeDialogPresenter;
import yyl.diyapp.com.mytexi.account.presenter.SmsCodeDialogPresenterImpl;
import yyl.diyapp.com.mytexi.common.databus.RxBus;
import yyl.diyapp.com.mytexi.common.http.IHttpClient;
import yyl.diyapp.com.mytexi.common.http.Impl.OkHttpClientImpl;
import yyl.diyapp.com.mytexi.common.storage.SharedPreferencesDao;
import yyl.diyapp.com.mytexi.common.utils.ToastUtil;

/**
 * Created by lsh on 2018/4/2.
 */

public class SmsCodeDialog extends Dialog implements ISmsCodeDialogView {

    private static final String TAG = "SmsCodeDialog";
    private String mPhone;
    private Button mResentBtn;
    private TextView mPhoneTv;
    private VerificationCodeInput mVerificationCodeInput;
    private ProgressBar mLoading;
    private TextView mErrorView;
    private ISmsCodeDialogPresenter mPresenter ;
    /**
     * 验证码倒计时
     */
    private CountDownTimer mCountDownTimer = new CountDownTimer(10000, 1000) {//总共10s，1s
        @Override
        public void onTick(long millisUntilFinished) {
            mResentBtn.setEnabled(false);
            mResentBtn.setText(getContext().getString(R.string.after_time_resend, millisUntilFinished / 1000));
        }

        @Override
        public void onFinish() {
            mResentBtn.setEnabled(true);
            mResentBtn.setText(getContext().getString(R.string.resend));
            cancel();
        }
    };


    public SmsCodeDialog(@NonNull Context context, String phone) {
        this(context, R.style.Dialog);
        this.mPhone = phone;
        IHttpClient iHttpClient  = new OkHttpClientImpl();
        SharedPreferencesDao preferencesDao = new SharedPreferencesDao(MyApplication.getInstance(), SharedPreferencesDao.FILE_ACCOUNT);
        AccountManagerImpl accountManager = new AccountManagerImpl(iHttpClient ,preferencesDao);
        mPresenter = new SmsCodeDialogPresenterImpl(this  ,accountManager);

    }

    public SmsCodeDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View root = inflater.inflate(R.layout.dialog_smscode_input, null);
        setContentView(root);
        mPhoneTv = findViewById(R.id.phone);
        String template = (String) getContext().getResources().getText(R.string.sending);
        mPhoneTv.setText(String.format(template, mPhone));
        mResentBtn = findViewById(R.id.btn_resend);
        mVerificationCodeInput = (VerificationCodeInput) findViewById(R.id.verificationCodeInput);
        mLoading = findViewById(R.id.loading);
        mErrorView = findViewById(R.id.error);
        mErrorView.setVisibility(View.GONE);
        initListener();
        requestSendSmsCode();
        RxBus.getInstance().register(mPresenter);
    }

    /**
     * 请求下发验证码
     */
    private void requestSendSmsCode() {
        mPresenter.requestSendSmsCode(mPhone);
    }

    private void initListener() {
        //设置关闭按钮的监听器
        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        //重新发送验证码监听器
        mResentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resend();
            }
        });
        mVerificationCodeInput.setOnCompleteListener(new VerificationCodeInput.Listener() {
            @Override
            public void onComplete(String code) {
                commit(code);
            }
        });
    }

    private void commit(final String code) {
        mPresenter.requestCheckSmsCode( mPhone, code);
    }

    @Override
    public void showLoading() {
        mLoading.setVisibility(View.VISIBLE);
    }

    @Override
    public void showErrow(int code, String msg) {
        mLoading.setVisibility(View.GONE);
      /*  IAccountManager-*/

        switch (code) {
            //验证码发送失败
            case IAccountManager.SMS_SEND_FAIL:
                ToastUtil.show(getContext(), getContext().getString(R.string.sms_send_fail));
                break;
            //验证码校验失败
            case IAccountManager.SMS_CHECK_FAIL:
                mErrorView.setVisibility(View.VISIBLE);
                mVerificationCodeInput.setEnabled(true);
                break;
            // 服务器异常
            case IAccountManager.SERVER_FAIL:
                ToastUtil.show(getContext(), getContext().getString(R.string.error_server));
                break;

        }
    }

    private void resend() {
        String template = getContext().getResources().getString(R.string.sending);
        mPhoneTv.setText(String.format(template, mPhone));
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mCountDownTimer.cancel();
    }


    @Override
    public void showCountDownTimer() {
        mPhoneTv.setText(String.format(getContext().getString(R.string.sms_code_send_phone), mPhone));
        mCountDownTimer.start();
        mResentBtn.setEnabled(false);

    }


    @Override
    public void showSmsCodeCheckState(boolean suc) {
        if (!suc) {
            //提示验证码错误
            mErrorView.setVisibility(View.VISIBLE);
            mVerificationCodeInput.setEnabled(true);
            mLoading.setVisibility(View.GONE);
        } else {
            mErrorView.setVisibility(View.GONE);
            mLoading.setVisibility(View.VISIBLE);
            mPresenter.requestCheckUserExist(mPhone);

        }
    }

    /**
     * 用户是否存在
     *
     * @param exite
     */
    @Override
    public void showUserExist(boolean exite) {
        mLoading.setVisibility(View.GONE);
        mErrorView.setVisibility(View.GONE);
        dismiss();
        if (!exite) {
            CreatPasswordDialog dialog = new CreatPasswordDialog(getContext(), mPhone);
            dialog.show();
        } else {
            //用户不存在
            LoginDialog loginDialog = new LoginDialog(getContext(), mPhone);
            loginDialog.show();
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        RxBus.getInstance().unRegister(mPresenter);
    }

}
