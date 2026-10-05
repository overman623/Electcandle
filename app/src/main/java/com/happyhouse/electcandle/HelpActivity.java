package com.happyhouse.electcandle;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
public class HelpActivity extends AppCompatActivity {
    //    private InterstitialAd mInterstitialAd;
    private RequestManager requestManager;
    private Button btnHelpExit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        requestManager = Glide.with(this);
        ImageView imageView = findViewById(R.id.image_help);
        requestManager.load(R.drawable.app_help).into(imageView);
        btnHelpExit = findViewById(R.id.btn_help_exit);
        btnHelpExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
