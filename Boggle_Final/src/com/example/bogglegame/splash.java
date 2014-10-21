package com.example.bogglegame;


import android.os.Bundle;
import android.util.Log;
import android.app.Activity;
import android.content.Intent;

public class splash extends Activity {

  public static final long TIME = 1500;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.logo);

    Thread welcomeThread = new Thread() {

        @Override
        public void run() {
            try {
                sleep(TIME);
            } catch (Exception e) {
                Log.e(getClass().getName(), e.toString());
            } finally {
                startActivity(new Intent(splash.this,MainActivity.class));
                finish();
            }
        }
    };
    welcomeThread.start();
  }
}