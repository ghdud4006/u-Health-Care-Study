package com.example.lsy.mythread33;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    TextView textView;
    Handler handler2 = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView); //text view id 찾아줌

        Button button = (Button) findViewById(R.id.button); //button1 id 찾아줌
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //첫번재 button click 시 스레드 만들어 실행
                //sol 3 . runable 객체를 직접 import (thread를 상속받지 않고 직접 객체 정의)
                new Thread(new Runnable() {
                    int value = 0;
                    boolean running = false;

                    public void run() {
                        running = true;
                        while(running) {
                            value += 1;

                            handler2.post(new Runnable() { //handler2로 전달 handler 내부에서 실행되는 code가 됨
                                //따라서 UI 에 접근 할 수 있다 (main thread 에서 실행되기 때문에)
                                //훨씬 짧은 code가 될 수 있다 (쉽다)
                                public void run() {
                                    textView.setText("현재 값 : " + value);
                                }
                            });

                            try { //예외 처리
                                Thread.sleep(1000);
                            } catch(Exception e) {}
                        }
                    }
                }).start();
            }
        });

        Button button2 = (Button) findViewById(R.id.button2); //button2 id 찾아줌
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // textView.setText("현재 값 : " + value); //현재값 보여줌
            }
        });

    }
}
