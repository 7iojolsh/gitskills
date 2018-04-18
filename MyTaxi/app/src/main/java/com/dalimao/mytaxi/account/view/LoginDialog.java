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
import com.dalimao.mytaxi.account.presenter.ILoginDialogPresenter;
import com.dalimao.mytaxi.account.presenter.LoginDialogPresenterImpl;
import com.dalimao.mytaxi.common.databus.RxBus;
import com.dalimao.mytaxi.common.http.IHttpClient;
import com.dalimao.mytaxi.common.http.Impl.OkHttpClientImpl;
import com.dalimao.mytaxi.common.storage.SharedPreferencesDao;
import com.dalimao.mytaxi.common.utils.ToastUtil;

import com.dalimao.mytaxi.R;

/**
 * 登陆弹出框
 * Created by lsh on 2018/4/4.
 */

public class LoginDialog extends Dialog  implements ILoginDialogView {

    private static final String TAG = "LoginDialog";
    private TextView mPhone;
    private EditText mPw;
    private Button mBtnConfirm;
    private View mLoading;
    private TextView mTips;
    private String mPhoneStr;
    private ILoginDialogPresenter mPresenter ;


    public LoginDialog(@NonNull Context context, String phone) {
        this(context, R.style.Dialog);
        this.mPhoneStr = phone;
        IHttpClient httpClient = new OkHttpClientImpl() ;
        SharedPreferencesDao preferencesDao =
                new SharedPreferencesDao(MyApplication.getInstance() ,
                        SharedPreferencesDao.FILE_ACCOUNT);
        IAccountManager iAccountManager = new AccountManagerImpl(httpClient ,preferencesDao );
        mPresenter = new LoginDialogPresenterImpl(this , iAccountManager) ;
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

        // 注册presenter
        RxBus.getInstance().register(mPresenter);
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
        mPresenter.requestLogin(mPhoneStr ,password);
    }
    /**
     * 处理登陆成功ui
     */
    @Override
    public void showLoginSuc() {
        showOrHideLoading(false);
        mTips.setVisibility(View.VISIBLE);
        mTips.setTextColor(getContext().getResources().getColor(R.color.color_text_normal));
        mTips.setText(getContext().getString(R.string.login_suc));
        ToastUtil.show(getContext() , getContext().getString(R.string.login_suc));
        dismiss();
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
     * 显示服务起出错
     */
    private void showServiceError() {
        showOrHideLoading(false);
        mTips.setVisibility(View.VISIBLE);
        mTips.setTextColor(getContext().getResources().getColor(R.color.error_red));
        mTips.setText(getContext().getString(R.string.error_server));
    }

    /**
     * 密码错误
     */
    private void showPasswordError() {
        showOrHideLoading(false);
        mTips.setVisibility(View.VISIBLE);
        mTips.setTextColor(getContext().getResources().getColor(R.color.error_red));
        mTips.setText(getContext().getString(R.string.password_error));
    }

    @Override
    public void showLoading() {
        showOrHideLoading(true );
    }

    @Override
    public void showErrow(int code, String msg) {
        switch (code) {
            case IAccountManager.PW_ERROR:
                showPasswordError();
                break;
            case IAccountManager.SERVER_FAIL:
                showServiceError();
                break;
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        // z注销Presenter
        RxBus.getInstance().unRegister(mPresenter);
    }
}
