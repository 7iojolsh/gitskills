package com.dalimao.mytaxi.account.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.dalimao.mytaxi.MyApplication;
import com.dalimao.mytaxi.account.model.AccountManagerImpl;
import com.dalimao.mytaxi.account.model.IAccountManager;
import com.dalimao.mytaxi.account.presenter.CreatPasswordDialogPresenterImpl;
import com.dalimao.mytaxi.account.presenter.ICreatPasswordDialogPresenter;
import com.dalimao.mytaxi.common.databus.RxBus;
import com.dalimao.mytaxi.common.http.IHttpClient;
import com.dalimao.mytaxi.common.http.Impl.OkHttpClientImpl;
import com.dalimao.mytaxi.common.storage.SharedPreferencesDao;
import com.dalimao.mytaxi.common.utils.ToastUtil;
import com.dalimao.mytaxi.main.view.MainActivity;

import com.dalimao.mytaxi.R;

/**
 * 密码创建修改
 * Created by lsh on 2018/4/3.
 */

public class CreatPasswordDialog extends Dialog implements ICreatPasswordDialogView {

    private static final String TAG = "CreatPasswordDialogPresenterImpl";
    private TextView mTitle;
    private TextView mPhone;
    private EditText mPw;
    private EditText mPw1;
    private Button mBtnConfirm;
    private View mLoading;
    private TextView mTips;
    private String mPhoneStr;
    private ICreatPasswordDialogPresenter mPresenter ;
    private Object mContext;
    public CreatPasswordDialog(@NonNull Context context, String phone) {
        this(context, R.style.Dialog);
        this.mPhoneStr = phone;
        IHttpClient iHttpClient  = new OkHttpClientImpl();
        SharedPreferencesDao preferencesDao = new SharedPreferencesDao(MyApplication.getInstance(),
                SharedPreferencesDao.FILE_ACCOUNT);
        AccountManagerImpl accountManager = new AccountManagerImpl(iHttpClient ,preferencesDao);
        mPresenter = new CreatPasswordDialogPresenterImpl(this, accountManager);
        mContext = context ;
    }

    public CreatPasswordDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View root = inflater.inflate(R.layout.dialog_create_pw, null);
        setContentView(root);
        initView();
        RxBus.getInstance().register(mPresenter);
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

    @Override
    public void showLoading() {
        showOrHideLoading(true);
    }

    private void showOrHideLoading(boolean show) {
        if (show) {
            mLoading.setVisibility(View.VISIBLE);
            mBtnConfirm.setVisibility(View.GONE);
        } else {
            mLoading.setVisibility(View.GONE);
            mBtnConfirm.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showErrow(int code, String msg) {
        showOrHideLoading(false);
        switch (code) {
            case IAccountManager.PW_ERROR:
                showLoginFail();
                break;
            case IAccountManager.SERVER_FAIL:
                showServerError();
                break;
        }
    }

    /**
     * 注册成功方法
     */
    @Override
    public void showRegisterSuc() {
        mLoading.setVisibility(View.VISIBLE);
        mBtnConfirm.setVisibility(View.GONE);
        mTips.setVisibility(View.VISIBLE);
        mTips.setTextColor(getContext().getResources().getColor(R.color.color_text_normal));
        mTips.setText(getContext().getString(R.string.register_suc_and_loging));
        // 请求网络，完成自动登录
        mPresenter.requestLogin(mPhoneStr, mPw.getText().toString());
    }

    /**
     * 登陆成功
     */
    @Override
    public void showLoginSuc() {
       ToastUtil.show(getContext() , "我执行了");
        dismiss();
     /*   ToastUtil.show(getContext(),
                getContext().getString(R.string.login_suc));*/
        if (mContext instanceof MainActivity) {
            ((MainActivity)mContext).showLoginSuc();
        }

    }

    @Override
    public void showPasswordNull() {
        mTips.setVisibility(View.VISIBLE);
        mTips.setText(getContext().getString(R.string.password_is_null));
        mTips.setTextColor(getContext().
                getResources().getColor(R.color.error_red));
    }

    @Override
    public void showPasswordNotEqual() {
        mTips.setVisibility(View.VISIBLE);
        mTips.setText(getContext()
                .getString(R.string.password_is_not_equal));
        mTips.setTextColor(getContext()
                .getResources().getColor(R.color.error_red));
    }

    /**
     * 错误信息提示
     */
    private void showServerError() {
        mTips.setTextColor(getContext().getResources().getColor(R.color.error_red));
        mTips.setText(getContext().getString(R.string.error_server));
    }



    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }



    /**
     * 提交注册
     */
    private void submit() {
        if(checkPassword()){
            final String pwd = mPw.getText().toString().trim();
            final String phonePhone = mPhoneStr ;
            //请求网络, 提交注册
            mPresenter.requestRegister(phonePhone ,pwd );
        }

    }


    /**
     * 检查密码是否满足需求
     * @return
     */
    private boolean checkPassword() {
        String password = mPw.getText().toString();
        String password1 = mPw1.getText().toString();
        return  mPresenter.checkPw(password , password1) ;
    }
    private void showLoginFail() {
        dismiss();
        ToastUtil.show(getContext(),
                getContext().getString(R.string.error_server));
    }

    @Override
    public void dismiss() {
        super.dismiss();
        RxBus.getInstance().unRegister(mPresenter);
    }
}
