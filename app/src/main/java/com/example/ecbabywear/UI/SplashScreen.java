package com.example.ecbabywear.UI;

import static com.example.ecbabywear.ApplicationClass.navigateToActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.ecbabywear.ApplicationClass;
import com.example.ecbabywear.R;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed((Runnable) () -> {
            navigateToActivity(this, SignUp.class);
            finish();
        },3000);
    }
}