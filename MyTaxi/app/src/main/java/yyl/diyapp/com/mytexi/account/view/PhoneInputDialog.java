package yyl.diyapp.com.mytexi.account.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import yyl.diyapp.com.mytexi.R;
import yyl.diyapp.com.mytexi.common.utils.FormatUtil;

/**
 * Created by lsh on 2018/4/2.
 */

public class PhoneInputDialog extends Dialog {

    private View mRoot;
    private Button mButton;
    private EditText mPhone;

    public PhoneInputDialog(@NonNull Context context) {
        this(context, R.style.Dialog);
    }


    private PhoneInputDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRoot = inflater.inflate(R.layout.dialog_phone_input, null);
        setContentView(mRoot);
        initListener();

    }

    private void initListener() {
        mButton = findViewById(R.id.btn_next);
        //设置为不可编辑
        mButton.setEnabled(false);
        mPhone = findViewById(R.id.phone);
        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        //监听输入手机号码 是否合法
        mPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                check();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                String phone= mPhone.getText().toString();
                SmsCodeDialog smsCodeDialog = new SmsCodeDialog(getContext() , phone);
                smsCodeDialog.show();
            }
        });

    }

    /**
     * 检查手机号吗是否合法
     */
    private void check() {
        String phone = mPhone.getText().toString();
        boolean legal = FormatUtil.checkMobile(phone);
        if(legal){
            mButton.setEnabled(true);
        }else
        {
            mButton.setEnabled(false);
        }

    }
}
