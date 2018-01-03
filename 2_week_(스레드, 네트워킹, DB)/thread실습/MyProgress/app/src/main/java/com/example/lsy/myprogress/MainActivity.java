package com.example.lsy.myprogress;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

public class MainActivity extends AppCompatActivity {
    ProgressBar progressBar;

    Handler handler = new Handler();

    CompletionThread completionThread;

    String msg = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ProgressThread thread = new ProgressThread();
                //thread.start();

                //sol2 n초 뒤 부터
                handler.postDelayed(new Runnable() {
                    public void run() {
                        ProgressThread thread = new ProgressThread();
                        thread.start();
                    }
                }, 5000); //5초 후 실행
            }
        });

        completionThread = new CompletionThread();
        completionThread.start();
    }

    class ProgressThread extends Thread {
        int value = 0;

        public void run() {
            while(true) {
                if(value > 100) {
                    break;
                }
                value += 1;

                handler.post(new Runnable() {
                   @Override
                    public void run() {
                       progressBar.setProgress(value);
                   }
                });

                //use sleep
                try {
                    Thread.sleep(50);// 0.05초 sleep
                } catch(Exception e) {}
            }

            completionThread.completionHandler.post(new Runnable() { //sub 스레드의 handler를 post
                public void run() {
                    msg = "OK";
                    Log.d("MainActivity", "메시지 : " + msg); //log창에 메시지 출력
                }
            });
        }
    }

    class CompletionThread extends Thread {
        Handler completionHandler = new Handler(); //thread 내에 별도의 handler 만들어 사용

        public void run() {
            Looper.prepare();
            Looper.loop(); // thread 계속 대기
        }
    }
}
