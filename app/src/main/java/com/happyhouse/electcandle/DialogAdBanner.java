package com.happyhouse.electcandle;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;

import static com.happyhouse.electcandle.MainActivity.displayX;
import static com.happyhouse.electcandle.MainActivity.displayY;

public class DialogAdBanner extends AlertDialog {

    public static final String TAG = "DialogAdBanner";
    private int layoutResource;
    private AdView mAdBannerView;
    public Button btnExit;
    private Button btnCancel;

    protected DialogAdBanner(Context context) {
        super(context);
    }

    public void setLayoutResource(int layoutResource){
        this.layoutResource = layoutResource;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
//        getWindow().setAttributes(lpWindow);
//        getWindow().setLayout((int)(displayX * 0.9), (int)(displayY * 0.5));
        getWindow().setGravity(Gravity.CENTER);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(this.layoutResource);
        setLayout();

    }

    private void setLayout(){
        mAdBannerView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdBannerView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                Log.d(MainActivity.TAG, "onAdLoaded : ");
            }

            @Override
            public void onAdFailedToLoad(LoadAdError error) {
                Log.d(MainActivity.TAG, "errorCode : " + error.getCode());
            }

            @Override
            public void onAdOpened() {
                Log.d(MainActivity.TAG, "onAdOpened : ");
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
                //광고가 열린다면 광고를 열고 어플을종료함.
                btnExit.performClick();
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when when the user is about to return
                // to the app after tapping on an ad.
                //finish();
            }
        });

        mAdBannerView.loadAd(adRequest);//베너 광고 //test ok //앱 아이디를 바꿔야 함.
        btnExit = findViewById(R.id.timer_exit);
        btnCancel = findViewById(R.id.timer_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });
    }

}
