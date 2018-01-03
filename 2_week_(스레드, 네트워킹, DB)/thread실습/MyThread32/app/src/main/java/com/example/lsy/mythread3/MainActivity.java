package com.example.lsy.mythread3;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    TextView textView;
    ValueHandler handler = new ValueHandler(); //handler 객체 생성

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView); //text view id 찾아줌

        Button button = (Button) findViewById(R.id.button); //button1 id 찾아줌
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //첫번재 button click 시 스레드 만들어 실행
                BackgroundThread thread = new BackgroundThread(); // 스레드 객체 생성
                thread.start(); //thread start -> 객체 내의 run method 실행
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
    class BackgroundThread extends Thread { //Thread 상속
        int value = 0;
        boolean running = false;
        public void run() { //Thread 내의 메소드 run
            running = true;
            while(running) {
                value += 1;
                //textView.setText("현재 값 : " + value); //오류 발생 이 code 사용할 수 없음 main thread만 접근할 수 있음  -> handler사용해야함

                Message message = handler.obtainMessage(); //msg 객체 생성
                Bundle bundle = new Bundle(); //bundle객체 생성
                bundle.putInt("value", value); //bundle에 value넣어줌
                message.setData(bundle); //bundle이라는 객체 msg객체에 넣어줌
                handler.sendMessage(message); //handler에게 msg 보냄

                try { //예외 처리
                    Thread.sleep(1000);
                } catch(Exception e) {}
            }
        }
    }

    class ValueHandler extends Handler {
        @Override
        public void handleMessage(Message msg) { // message 받아서 UI 접근 msg받으면 자동으로 실행됨
            super.handleMessage(msg);

            Bundle bundle = msg.getData(); //data 같은 object형식에 받아옴
            int value = bundle.getInt("value"); //int 값에 value 받아옴
            textView.setText("현재 값 : " + value); // handler내에서는 정상적으로 사용할 수 있다 !!!
        }
    }
}

