package com.fruit.pro.update;

import android.os.Bundle;
import android.provider.SyncStateContract;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.fruit.updatelib.AppUtils;
import com.fruit.updatelib.Constants;
import com.fruit.updatelib.UpdateChecker;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Button btn1 = (Button) findViewById(R.id.button1);
    Button btn2 = (Button) findViewById(R.id.button2);
    Button btn3 = (Button) findViewById(R.id.button3);

    btn1.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        UpdateChecker.checkForDialog(MainActivity.this, getJsonInfo());
      }
    });
    btn2.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        UpdateChecker.checkForNotification(MainActivity.this, getJsonInfo());
      }
    });
    btn3.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        UpdateChecker.checkForDownloadImmediate(MainActivity.this, getJsonInfo());
      }
    });


    TextView textView = (TextView) findViewById(R.id.textView1);

    textView.setText("当前版本信息: versionName = " + AppUtils.getVersionName(this) + " versionCode = " + AppUtils.getVersionCode(this));
  }

  private String getJsonInfo() {

    try {
      JSONObject object = new JSONObject();
      object.put(Constants.APK_DOWNLOAD_URL, "https://raw.githubusercontent.com/feicien/android-auto-update/develop/extras/android-auto-update-v1.1.apk");
      object.put(Constants.APK_VERSION_CODE, 2);
      object.put(Constants.APK_UPDATE_CONTENT, "1. 新增XX功能;<br/>2. 修复了Bug;<br/>3. 优化了性能。");
      return object.toString();
    } catch (JSONException e) {
      e.printStackTrace();
      return "";
    }
  }

}
