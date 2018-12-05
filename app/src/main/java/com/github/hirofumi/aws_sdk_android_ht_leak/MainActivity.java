package com.github.hirofumi.aws_sdk_android_ht_leak;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.internal.StaticCredentialsProvider;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    AWSIotMqttManager mqttManager = new AWSIotMqttManager("dummy-client-id", "nonexistent.iot.us-east-1.example.invalid");
    mqttManager.setReconnectRetryLimits(0, 0);
    mqttManager.setMaxAutoReconnectAttempts(-1);
    mqttManager.connect(
        new StaticCredentialsProvider(new BasicAWSCredentials("", "")),
        new AWSIotMqttClientStatusCallback() {
          @Override
          public void onStatusChanged(AWSIotMqttClientStatus status, Throwable throwable) {}
        }
    );

    (new Thread() {
      @Override
      public void run() {
        String ulimit = "ulimit -n";
        try {
          Log.e(TAG, "`" + ulimit + "` = " + execAndReadLines(ulimit)[0]);
        } catch (IOException e) {
          Log.e(TAG, "failed to run: " + ulimit, e);
        }
        while (true) {
          String lsof = "lsof -p " + android.os.Process.myPid();
          try {
            String[] lines = execAndReadLines(lsof);
            Log.e(TAG, "length of `" + lsof + "` = " + lines.length + ", number of active threads = " + Thread.activeCount());
            Thread.sleep(1000L);
          } catch (Exception e) {
            Log.e(TAG, "failed to run: " + lsof, e);
          }
        }
      }
    }).start();
  }

  private String[] execAndReadLines(String command) throws IOException {
    Process p = Runtime.getRuntime().exec(command);
    try (
        InputStream is = p.getInputStream();
        InputStreamReader isr = new java.io.InputStreamReader(is);
        java.io.BufferedReader br = new java.io.BufferedReader(isr);
    ) {
      ArrayList<String> list = new ArrayList<>();
      while (true) {
        String s = br.readLine();
        if (s == null) {
          return list.toArray(new String[0]);
        }
        list.add(s);
      }
    } finally {
      p.destroy();
    }
  }

}
