/*
 * Copyright (C) 2016 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.gms.fit.samples.stepcounter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fit.samples.common.logger.Log;
import com.google.android.gms.fit.samples.common.logger.LogView;
import com.google.android.gms.fit.samples.common.logger.LogWrapper;
import com.google.android.gms.fit.samples.common.logger.MessageOnlyLogFilter;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This sample demonstrates combining the Recording API and History API of the Google Fit platform
 * to record steps, and display the daily current step count. It also demonstrates how to
 * authenticate a user with Google Play Services.
 */
public class MainActivity extends AppCompatActivity {

  TextView tvData;

  public static final String TAG = "StepCounter";
  private static final int REQUEST_OAUTH_REQUEST_CODE = 0x1001;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    tvData = (TextView)findViewById(R.id.title_text_view);
    Button btn = (Button)findViewById(R.id.httpTest);

    btn.setOnClickListener(new View.OnClickListener() {


      @Override
      public void onClick(View view) {
        new JSONTask().execute("http://13.125.151.92:9000/post");//AsyncTask 시작시킴
      }
    });
    // This method sets up our custom logger, which will print all log messages to the device
    // screen, as well as to adb logcat.
    initializeLogging();

    FitnessOptions fitnessOptions =
        FitnessOptions.builder()
            .addDataType(DataType.z)
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
            .build();
    if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
      GoogleSignIn.requestPermissions(
          this,
          REQUEST_OAUTH_REQUEST_CODE,
          GoogleSignIn.getLastSignedInAccount(this),
          fitnessOptions);
    } else {
      subscribe();
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode == Activity.RESULT_OK) {
      if (requestCode == REQUEST_OAUTH_REQUEST_CODE) {
        subscribe();
      }
    }
  }

  /** Records step data by requesting a subscription to background step data. */
  public void subscribe() {
    // To create a subscription, invoke the Recording API. As soon as the subscription is
    // active, fitness data will start recording.
    Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this))
        .subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE)
        .addOnCompleteListener(
            new OnCompleteListener<Void>() {
              @Override
              public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                  Log.i(TAG, "Successfully subscribed!");
                } else {
                  Log.w(TAG, "There was a problem subscribing.", task.getException());
                }
              }
            });
  }

  /**
   * Reads the current daily step total, computed from midnight of the current day on the device's
   * current timezone.
   */
  long total;
  private void readData() {
    Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
        .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
        .addOnSuccessListener(
            new OnSuccessListener<DataSet>() {
              @Override
              public void onSuccess(DataSet dataSet) {
                total =
                    dataSet.isEmpty()
                        ? 0
                        : dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                //total = 500;
                Log.i(TAG, "Total steps: " + total);
              }
            })
        .addOnFailureListener(
            new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "There was a problem getting the step count.", e);
              }
            });




  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the main; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.action_read_data) {
      readData();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  /** Initializes a custom log class that outputs both to in-app targets and logcat. */
  private void initializeLogging() {
    // Wraps Android's native log framework.
    LogWrapper logWrapper = new LogWrapper();
    // Using Log, front-end to the logging chain, emulates android.util.log method signatures.
    Log.setLogNode(logWrapper);
    // Filter strips out everything except the message text.
    MessageOnlyLogFilter msgFilter = new MessageOnlyLogFilter();
    logWrapper.setNext(msgFilter);
    // On screen logging via a customized TextView.
    LogView logView = (LogView) findViewById(R.id.sample_logview);

    // Fixing this lint error adds logic without benefit.
    // noinspection AndroidLintDeprecation
    logView.setTextAppearance(R.style.Log);

    logView.setBackgroundColor(Color.WHITE);
    msgFilter.setNext(logView);
    Log.i(TAG, "Ready");
  }

  public class JSONTask extends AsyncTask<String, String, String> {

    @Override
    protected String doInBackground(String... urls) {
      try {
        //JSONObject를 만들고 key value 형식으로 값을 저장해준다.
        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("step", total);
        jsonObject.accumulate("name", "Kim");

        HttpURLConnection con = null;
        BufferedReader reader = null;

        try{
          //URL url = new URL("http://192.168.25.16:3000/users");
          URL url = new URL(urls[0]);
          //연결을 함
          con = (HttpURLConnection) url.openConnection();

          con.setRequestMethod("POST");//POST방식으로 보냄
          con.setRequestProperty("Cache-Control", "no-cache");//캐시 설정
          con.setRequestProperty("Content-Type", "application/json");//application JSON 형식으로 전송
          con.setRequestProperty("Accept", "text/html");//서버에 response 데이터를 html로 받음
          con.setDoOutput(true);//Outstream으로 post 데이터를 넘겨주겠다는 의미
          con.setDoInput(true);//Inputstream으로 서버로부터 응답을 받겠다는 의미
          con.connect();

          //서버로 보내기위해서 스트림 만듬
          OutputStream outStream = con.getOutputStream();
          //버퍼를 생성하고 넣음
          BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));
          writer.write(jsonObject.toString());
          writer.flush();
          writer.close();//버퍼를 받아줌

          //서버로 부터 데이터를 받음
          InputStream stream = con.getInputStream();

          reader = new BufferedReader(new InputStreamReader(stream));

          StringBuffer buffer = new StringBuffer();

          String line = "";
          while((line = reader.readLine()) != null){
            buffer.append(line);
          }

          return buffer.toString();//서버로 부터 받은 값을 리턴해줌

        } catch (MalformedURLException e){
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        } finally {
          if(con != null){
            con.disconnect();
          }
          try {
            if(reader != null){
              reader.close();//버퍼를 닫아줌
            }
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }

      return null;
    }

    @Override
    protected void onPostExecute(String result) {
      super.onPostExecute(result);
      tvData.setText(result);//서버로 부터 받은 값을 출력해주는 부
    }
  }
}
