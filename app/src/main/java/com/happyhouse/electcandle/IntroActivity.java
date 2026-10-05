package com.happyhouse.electcandle;

import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import static com.happyhouse.electcandle.MainActivity.INTRO;

public class IntroActivity extends AppCompatActivity {
    private Handler handler;
//    private InterstitialAd mInterstitialAd;
    private RequestManager requestManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        requestManager = Glide.with(this);
        ImageView imageView = findViewById(R.id.image_intro);
        requestManager.load(R.drawable.intro).into(imageView);
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
/*                mInterstitialAd.setAdListener(new AdListener() {
                    @Override
                    public void onAdClosed() {
                        // Code to be executed when when the user is about to return
                        // to the app after tapping on an ad.
                        finish();
                    }
                });*/
//                mInterstitialAd.show();
                setResult(INTRO);
                finish();
            }
        }, 2000);

    }


    @Override
    protected void onDestroy() {
        Log.d("IntroActivity", "Ondestroy");
        super.onDestroy();
    }
}
