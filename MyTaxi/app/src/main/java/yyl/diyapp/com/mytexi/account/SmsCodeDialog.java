package yyl.diyapp.com.mytexi.account;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dalimao.corelibrary.VerificationCodeInput;
import com.google.gson.Gson;

import java.lang.ref.SoftReference;

import yyl.diyapp.com.mytexi.R;
import yyl.diyapp.com.mytexi.common.http.IHttpClient;
import yyl.diyapp.com.mytexi.common.http.IRequest;
import yyl.diyapp.com.mytexi.common.http.IResponse;
import yyl.diyapp.com.mytexi.common.http.Impl.BaseRequest;
import yyl.diyapp.com.mytexi.common.http.Impl.BaseResponse;
import yyl.diyapp.com.mytexi.common.http.Impl.OkHttpClientImpl;
import yyl.diyapp.com.mytexi.common.http.api.API;
import yyl.diyapp.com.mytexi.common.http.biz.BaseBizResporse;
import yyl.diyapp.com.mytexi.common.utils.ToastUtil;

/**
 * Created by lsh on 2018/4/2.
 */

public class SmsCodeDialog extends Dialog {

    private static final String TAG = "SmsCodeDialog";
    private static final int SMS_SEND_SUC = 1;
    private static final int SMS_SEND_FAIL = -1;
    private static final int SMS_CHECK_SUC = 2;
    private static final int SMS_CHECK_FAIL = -2;
    private static final int USER_EXIST = 3;
    private static final int USER_NOT_EXIST = -3;
    private static final int SMS_SERVER_FAIL = 100;
    private String mPhone;
    private Button mResentBtn;
    private TextView mPhoneTv;
    private VerificationCodeInput mVerificationCodeInput;
    private IHttpClient mHttpClient;
    private MyHandler myHandler;
    private ProgressBar mLoading;
    private TextView mErrorView;
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



    /**
     * 接受子线程消息的Handler
     */
    static class MyHandler extends Handler {
        //软引用
        SoftReference<SmsCodeDialog> codeDialogSoftRef;

        public MyHandler(SmsCodeDialog smsCodeDialog) {
            codeDialogSoftRef = new SoftReference<SmsCodeDialog>(smsCodeDialog);

        }

        @Override
        public void handleMessage(Message msg) {
            SmsCodeDialog smsCodeDialog = codeDialogSoftRef.get();
            if (smsCodeDialog == null) {
                return;
            }
            //处理ui变化
            switch (msg.what) {
                case SmsCodeDialog.SMS_SEND_SUC:
                    smsCodeDialog.mCountDownTimer.start();
                    break;
                case SmsCodeDialog.SMS_SEND_FAIL:
                    ToastUtil.show(smsCodeDialog.getContext(),
                            smsCodeDialog.getContext().getString(R.string.sms_send_fail));
                    break;
                case SmsCodeDialog.SMS_CHECK_SUC:
                    //验证码校验成功
                    smsCodeDialog.showVifyState(true);
                    break;
                case SmsCodeDialog.SMS_CHECK_FAIL:
                    //验证码校验失败
                    smsCodeDialog.showVifyState(false);
                    break;
                case SmsCodeDialog.USER_EXIST:
                    //用户存在
                    smsCodeDialog.showUserExite(true);
                    break;
                case SmsCodeDialog.USER_NOT_EXIST:
                    //用户不存在
                    smsCodeDialog.showUserExite(false);
                    break;
                case SmsCodeDialog.SMS_SERVER_FAIL:
                    ToastUtil.show(smsCodeDialog.getContext(),
                            smsCodeDialog.getContext().getString(R.string.error_server));
                    break;
            }

        }
    }


    public SmsCodeDialog(@NonNull Context context, String phone) {
        this(context, R.style.Dialog);
        this.mPhone = phone;
        mHttpClient = new OkHttpClientImpl();
        myHandler = new MyHandler(this);
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
    }

    /**
     * 请求下发验证码
     */
    private void requestSendSmsCode() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                String url = API.Config.getDomain() + API.GET_SMS_CODE;
                IRequest request = new BaseRequest(url);
                request.setBody("phone", mPhone);
                IResponse response = mHttpClient.get(request, false);
                Log.d(TAG, response.getData());
                if (response.getCode() == BaseResponse.STATE_OK) {
                    BaseBizResporse bizRes =
                            new Gson().fromJson(response.getData(), BaseBizResporse.class);
                    if (bizRes.getCode() == BaseBizResporse.STATE_OK) {
                        myHandler.sendEmptyMessage(SMS_SEND_SUC);

                    } else {
                        myHandler.sendEmptyMessage(SMS_SEND_FAIL);
                    }
                } else {
                    myHandler.sendEmptyMessage(SMS_SEND_FAIL);
                }


            }
        }.start();

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
        showLoading();
        //网络请求 校验验证码
        new Thread() {
            @Override
            public void run() {
                super.run();
                String url = API.Config.getDomain() + API.CHECK_SMS_CODE;
                IRequest request = new BaseRequest(url);
                request.setBody("phone", mPhone);
                request.setBody("code", code);
                IResponse response = mHttpClient.get(request, false);
                Log.d(TAG, response.getData());
                if (response.getCode() == BaseResponse.STATE_OK) {
                    BaseBizResporse bizRes =
                            new Gson().fromJson(response.getData(), BaseBizResporse.class);
                    if (bizRes.getCode() == BaseBizResporse.STATE_OK) {
                        myHandler.sendEmptyMessage(SMS_CHECK_SUC);

                    } else {
                        myHandler.sendEmptyMessage(SMS_CHECK_FAIL);
                    }
                } else {
                    myHandler.sendEmptyMessage(SMS_CHECK_FAIL);
                }


            }
        }.start();
    }

    public void showVifyState(boolean suc){
        if(!suc){
            //提示验证码错误
            mErrorView.setVisibility(View.VISIBLE);
            mVerificationCodeInput.setEnabled(true);
            mLoading.setVisibility(View.GONE);
        }else{
            mErrorView.setVisibility(View.GONE);
            mLoading.setVisibility(View.VISIBLE);
            //检查用户是否存在
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    String url = API.Config.getDomain() + API.CHECK_USER_EXIST;
                    IRequest request = new BaseRequest(url);
                    request.setBody("phone", mPhone);
                    IResponse response = mHttpClient.get(request, false);
                    Log.d(TAG, response.getData());
                    if (response.getCode() == BaseResponse.STATE_OK) {
                        BaseBizResporse bizRes =
                                new Gson().fromJson(response.getData(), BaseBizResporse.class);
                        if (bizRes.getCode() == BaseBizResporse.STATE_USER_EXIST) {
                            myHandler.sendEmptyMessage(USER_EXIST);

                        } else  if (bizRes.getCode() == BaseBizResporse.STATE_USER_NOT_EXIST){
                            myHandler.sendEmptyMessage(USER_NOT_EXIST);
                        }
                    } else {
                        myHandler.sendEmptyMessage(SMS_SERVER_FAIL);
                    }


                }
            }.start();
        }
    }

    /**
     * 用户是否存在
     * @param exite
     */
    public void showUserExite(boolean exite){
        mLoading.setVisibility(View.GONE);
        mErrorView.setVisibility(View.GONE);
        dismiss();
        if(!exite){
            CreatPasswordDialog dialog = new CreatPasswordDialog(getContext() , mPhone) ;
            dialog.show();
        }else{
            //用户不存在
            LoginDialog loginDialog = new LoginDialog(getContext() , mPhone) ;
            loginDialog.show();
        }
    }
    private void showLoading() {
        mLoading.setVisibility(View.VISIBLE);

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
    public void dismiss() {
        super.dismiss();
    }
}
