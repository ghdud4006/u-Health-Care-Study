package com.example.lsy.mythread3;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    TextView textView;
    int value = 0; //점차 증가 스레드 내에서 증가

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView); //text view

        Button button = (Button) findViewById(R.id.button); //button1
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //실행행 buton click 시 스레드 만들어 실행
                BackgroundThread thread = new BackgroundThread(); // 스레드 객체 생성
                thread.start();
            }
        });

        Button button2 = (Button) findViewById(R.id.button2); //button2
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText("현재 값 : " + value); //현재값 보여줌
            }
        });
    }

    class BackgroundThread extends Thread { //Thread 상속 받아 사용
        boolean running = false;
        public void run() { //Thread 내의 메소드 run
            running = true;
            while(running) {
                value += 1;
                try { //예외 처리
                    Thread.sleep(1000);
                } catch(Exception e) {}
            }
        }
    }
}

