package yyl.diyapp.com.mytexi.splash;

import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import yyl.diyapp.com.mytexi.R;

/**
 * Created by lsh on 2018/3/30.
 */

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final AnimatedVectorDrawable anim = (AnimatedVectorDrawable) getResources().getDrawable(R.drawable.anim);
            final ImageView logo = (ImageView) findViewById(R.id.logo);
            logo.setImageDrawable(anim);
            anim.start();

        }
    }
}
