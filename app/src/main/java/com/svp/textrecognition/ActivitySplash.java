package com.svp.textrecognition;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class ActivitySplash extends AppCompatActivity {

    private ImageView imgLogo;
    Animation animFadeOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        imgLogo = (ImageView) findViewById(R.id.imgLogo);

        animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out_anim);


        animFadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation arg0) {
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }

            @Override
            public void onAnimationEnd(Animation arg0) {
                imgLogo.setVisibility(View.GONE);
                Intent intent = new Intent(ActivitySplash.this, ActivityHome.class);
                startActivity(intent);
                finish();
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                imgLogo.startAnimation(animFadeOut);
            }
        }, 1500);

    }
}