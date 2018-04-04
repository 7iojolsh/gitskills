package yyl.diyapp.com.mytexi.account;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;

import java.lang.ref.SoftReference;

import yyl.diyapp.com.mytexi.MyApplication;
import yyl.diyapp.com.mytexi.R;
import yyl.diyapp.com.mytexi.account.reaponse.Account;
import yyl.diyapp.com.mytexi.account.reaponse.LoginResponse;
import yyl.diyapp.com.mytexi.common.http.IHttpClient;
import yyl.diyapp.com.mytexi.common.http.IRequest;
import yyl.diyapp.com.mytexi.common.http.IResponse;
import yyl.diyapp.com.mytexi.common.http.Impl.BaseRequest;
import yyl.diyapp.com.mytexi.common.http.Impl.BaseResponse;
import yyl.diyapp.com.mytexi.common.http.Impl.OkHttpClientImpl;
import yyl.diyapp.com.mytexi.common.http.api.API;
import yyl.diyapp.com.mytexi.common.storage.SharedPreferencesDao;

/**
 * 登陆弹出框
 * Created by lsh on 2018/4/4.
 */

public class LoginDialog extends Dialog {

    private static final String TAG = "LoginDialog";
    private static final int LOGIN_SUC = 1;
    private static final int SERVER_FAIL = 2;
    private static final int PWD_ERR = 3;
    private TextView mPhone;
    private EditText mPw;
    private Button mBtnConfirm;
    private View mLoading;
    private TextView mTips;
    private String mPhoneStr;
    private MyHandler mHandler;
    private IHttpClient mHttpClient;

    /**
     * 接受子线程消息的Handler
     */
    static class MyHandler extends Handler {
        //软引用
        SoftReference<LoginDialog> dialogRef;

        public MyHandler(LoginDialog dialog) {
            dialogRef = new SoftReference<LoginDialog>(dialog);
        }

        @Override
        public void handleMessage(Message msg) {
            LoginDialog loginDialog = dialogRef.get();
            if (loginDialog == null) {
                return;
            }

            //处理ui 变化
            switch (msg.what) {
                case LOGIN_SUC:
                    loginDialog.showLoginSuc();
                    break;
                case PWD_ERR:
                    loginDialog.showPasswordError();
                    break;
                case SERVER_FAIL:
                    loginDialog.showServiceError();
                    break;

            }
        }
    }

    public LoginDialog(@NonNull Context context, String phone) {
        this(context, R.style.Dialog);
        this.mPhoneStr = phone;
        mHttpClient = new OkHttpClientImpl();
        mHandler = new MyHandler(this);
    }

    public LoginDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View root = inflater.inflate(R.layout.dialog_login_input, null);
        setContentView(root);
        initView();
    }

    private void initView() {
        mPhone = findViewById(R.id.phone);
        mPw = findViewById(R.id.password);
        mBtnConfirm = findViewById(R.id.btn_confirm);
        mLoading = findViewById(R.id.loading);
        mTips = findViewById(R.id.tips);
        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        mBtnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submit();
            }
        });
        mPhone.setText(mPhoneStr);
    }


    /**
     * 提交登陆
     */
    private void submit() {
        String password = mPw.getText().toString().trim();
        // 登陆的网络请求
        new Thread() {
            @Override
            public void run() {
                String url = API.Config.getDomain() + API.LOGIN;
                IRequest request = new BaseRequest(url);
                request.setBody("phone", mPhoneStr);
                String pwd = mPw.getText().toString().trim();
                request.setBody("password", pwd);
                IResponse response = mHttpClient.post(request, false);
                Log.d(TAG, response.getData());
                if (response.getCode() == BaseResponse.STATE_OK) {
                    LoginResponse bizRes =
                            new Gson().fromJson(response.getData(), LoginResponse.class);
                    if (bizRes.getCode() == LoginResponse.STATE_OK) {
                        //保存登陆信息
                        Account account = bizRes.getData();
                        // Todo : 加个密存储
                        SharedPreferencesDao preferencesDao =
                                new SharedPreferencesDao(MyApplication.getInstance(),
                                        SharedPreferencesDao.FILE_ACCOUNT);
                        //保存用户对象
                        preferencesDao.save(SharedPreferencesDao.KEY_ACCOUNT, account);
                        //通知ui
                        mHandler.sendEmptyMessage(LOGIN_SUC);
                    } else if (bizRes.getCode() == LoginResponse.STATE_PWD_ERR) {
                        mHandler.sendEmptyMessage(PWD_ERR);
                    } else {
                        mHandler.sendEmptyMessage(SERVER_FAIL);
                    }
                } else {
                    mHandler.sendEmptyMessage(SERVER_FAIL);
                }
            }
        }.start();

    }


    /**
     * 显示/隐藏 loading
     *
     * @param show
     */
    public void showOrHideLoading(boolean show) {
        if (show) {
            mLoading.setVisibility(View.VISIBLE);
            mBtnConfirm.setVisibility(View.GONE);
        } else {
            mLoading.setVisibility(View.GONE);
            mBtnConfirm.setVisibility(View.VISIBLE);
        }

    }

    /**
     * 处理登陆成功ui
     */
    public void showLoginSuc() {
        mLoading.setVisibility(View.GONE);
        mBtnConfirm.setVisibility(View.GONE);
        mTips.setVisibility(View.VISIBLE);
        mTips.setTextColor(getContext().getResources().getColor(R.color.color_text_normal));
        mTips.setText(getContext().getString(R.string.login_suc));
        dismiss();
    }

    /**
     * 显示服务起出错
     */
    public void showServiceError() {
        showOrHideLoading(false);
        mTips.setVisibility(View.VISIBLE);
        mTips.setTextColor(getContext().getResources().getColor(R.color.error_red));
        mTips.setText(getContext().getString(R.string.error_server));
    }

    /**
     * 密码错误
     */
    public void showPasswordError() {
        showOrHideLoading(false);
        mTips.setVisibility(View.VISIBLE);
        mTips.setTextColor(getContext().getResources().getColor(R.color.error_red));
        mTips.setText(getContext().getString(R.string.password_error));
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }
}
