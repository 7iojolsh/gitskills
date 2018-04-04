package yyl.diyapp.com.mytexi.account;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;

import java.lang.ref.SoftReference;
import java.lang.reflect.GenericArrayType;

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
import yyl.diyapp.com.mytexi.common.http.biz.BaseBizResporse;
import yyl.diyapp.com.mytexi.common.storage.SharedPreferencesDao;
import yyl.diyapp.com.mytexi.common.utils.DevUtil;
import yyl.diyapp.com.mytexi.common.utils.ToastUtil;

/**
 * 密码创建修改
 * Created by lsh on 2018/4/3.
 */

public class CreatPasswordDialog extends Dialog {

    private static final String TAG = "CreatPasswordDialog";
    private static final int REGISTER_SUC = 1;
    private static final int SERVER_FAIL = 100;
    private static final int LOGIN_SUC = 2;
    private TextView mTitle;
    private TextView mPhone;
    private EditText mPw;
    private EditText mPw1;
    private Button mBtnConfirm;
    private View mLoading;
    private TextView mTips;
    private IHttpClient mHttpClient;
    private String mPhoneStr;
    private MyHandler mHandler;

    /**
     * 接受子线程消息Handler
     */
    static class MyHandler extends Handler {
        //软引用
        SoftReference<CreatPasswordDialog> codeDialogRef;

        public MyHandler(CreatPasswordDialog codeDialog) {
            codeDialogRef = new SoftReference<CreatPasswordDialog>(codeDialog);
        }

        @Override
        public void handleMessage(Message msg) {
            CreatPasswordDialog dialog = codeDialogRef.get();
            if (dialog == null) {
                return;
            }
            //处理ui变化
            switch (msg.what) {
                case REGISTER_SUC:
                    dialog.showRegisterSuc();
                    break;
                case LOGIN_SUC:
                    dialog.showLoginSuc();
                    break;
                case SERVER_FAIL:
                    dialog.showServerError();
                    break;


            }
        }
    }



    /**
     * 注册成功方法
     */
    private void showRegisterSuc() {
        mLoading.setVisibility(View.VISIBLE);
        mBtnConfirm.setVisibility(View.GONE);
        mTips.setVisibility(View.VISIBLE);
        mTips.setTextColor(getContext().getResources().getColor(R.color.color_text_normal));
        mTips.setText(getContext().getString(R.string.register_suc_and_loging));
        // 请求网络,完成自动登陆
        new Thread(){
            @Override
            public void run() {
                String url  = API.Config.getDomain() + API.LOGIN ;
                IRequest request = new BaseRequest(url);
                request.setBody("phone",mPhoneStr);
                String pwd = mPw.getText().toString().trim();
                request.setBody("password" , pwd);
                IResponse response = mHttpClient.post(request, false);
                Log.d(TAG , response.getData());
                if(response.getCode() == BaseResponse.STATE_OK){
                    LoginResponse bizRes =
                            new Gson().fromJson(response.getData(), LoginResponse.class);
                    if(bizRes.getCode() == LoginResponse.STATE_OK){
                        //保存登陆信息
                        Account account = bizRes.getData() ;
                        SharedPreferencesDao preferencesDao =
                                new SharedPreferencesDao(MyApplication.getInstance() ,
                                        SharedPreferencesDao.FILE_ACCOUNT) ;
                        //保存用户对象
                        preferencesDao.save(SharedPreferencesDao.KEY_ACCOUNT  ,account );
                        //通知ui
                        mHandler.sendEmptyMessage(LOGIN_SUC) ;
                    }else{
                        mHandler.sendEmptyMessage(SERVER_FAIL) ;
                    }
                }else {
                    mHandler.sendEmptyMessage(SERVER_FAIL) ;
                }
            }
        }.start();
    }

    /**
     * 登陆成功
     */
    private void showLoginSuc() {
        dismiss();
        ToastUtil.show(getContext() , getContext().getString(R.string.login_suc));
    }

    /**
     * 错误信息提示
     */
    private void showServerError() {
        mTips.setTextColor(getContext().getResources().getColor(R.color.error_red));
        mTips.setText(getContext().getString(R.string.error_server));
    }

    public CreatPasswordDialog(@NonNull Context context, String phone) {
        this(context, R.style.Dialog);
        this.mPhoneStr = phone;
        mHttpClient = new OkHttpClientImpl();
        mHandler = new MyHandler(this);
    }

    public CreatPasswordDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View root = inflater.inflate(R.layout.dialog_create_pw, null);
        setContentView(root);
        initView();
    }

    private void initView() {
        mPhone = (TextView) findViewById(R.id.phone);
        mPw = (EditText) findViewById(R.id.pw);
        mPw1 = (EditText) findViewById(R.id.pw1);
        mBtnConfirm = (Button) findViewById(R.id.btn_confirm);
        mLoading = findViewById(R.id.loading);
        mTips = (TextView) findViewById(R.id.tips);
        mTitle = findViewById(R.id.dialog_title) ;
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
     * 提交注册
     */
    private void submit() {
        if(checkPassword()){
            final String pwd = mPw.getText().toString().trim();
            final String phonePhone = mPhoneStr ;
            //请求网络, 提交注册
            new Thread(){
                @Override
                public void run() {
                    String url  = API.Config.getDomain() + API.REGISTER ;
                    IRequest request = new BaseRequest(url);
                    request.setBody("phone",phonePhone);
                    request.setBody("password" , pwd);
                    request.setBody("uid" , DevUtil.UUID(getContext()));
                    IResponse response = mHttpClient.post(request, false);
                    Log.d(TAG , response.getData());
                    if(response.getCode() == BaseResponse.STATE_OK){
                        BaseBizResporse bizRes =
                                new Gson().fromJson(response.getData(), BaseBizResporse.class);
                        if(bizRes.getCode() == BaseBizResporse.STATE_OK){
                                mHandler.sendEmptyMessage(REGISTER_SUC) ;
                        }else{
                            mHandler.sendEmptyMessage(SERVER_FAIL) ;
                        }
                    }else {
                        mHandler.sendEmptyMessage(SERVER_FAIL) ;
                    }
                }
            }.start();

        }

    }


    /**
     * 检查密码是否满足需求
     * @return
     */
    private boolean checkPassword() {
        String password = mPw.getText().toString();
        String password1 = mPw1.getText().toString();
        if(TextUtils.isEmpty(password)){
            mTips.setVisibility(View.VISIBLE);
            mTips.setText(getContext().getString(R.string.password_is_null));
            mTips.setTextColor(getContext().getResources().getColor(R.color.error_red));
            return false;
        }
        //判断两次密码是否相同
        if(!password.equals(password1)){
            mTips.setVisibility(View.VISIBLE);
            mTips.setText(getContext().getString(R.string.password_is_not_equal));
            mTips.setTextColor(getContext().getResources().getColor(R.color.error_red));
            return false ;
        }
        return true;
    }
}
