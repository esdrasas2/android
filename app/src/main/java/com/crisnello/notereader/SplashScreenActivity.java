package com.crisnello.notereader;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by crisnello on 10/05/17.
 */

public class SplashScreenActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        Handler handle = new Handler();
        handle.postDelayed(new Runnable() {
            @Override
            public void run() {
                mostrarLogin();
            }
        }, 2000);

    }

    private void mostrarLogin() {
        Intent intent = new Intent(SplashScreenActivity.this,
                AutoLoginActivity.class);
        startActivity(intent);
        finish();
    }

}
