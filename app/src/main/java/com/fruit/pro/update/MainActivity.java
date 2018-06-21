package com.fruit.pro.update;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.SyncStateContract;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.fruit.updatelib.AppUtils;
import com.fruit.updatelib.Constants;
import com.fruit.updatelib.DownloadService;
import com.fruit.updatelib.UpdateChecker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;


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
        UpdateChecker.checkForDialog(MainActivity.this, getJsonInfo(), mHandler);
      }
    });
    btn2.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        UpdateChecker.checkForNotification(MainActivity.this, getJsonInfo(), mHandler);
      }
    });
    btn3.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        UpdateChecker.checkForDownloadImmediate(MainActivity.this, getJsonInfo(), mHandler);
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


  @SuppressLint("HandlerLeak")
  private Handler mHandler = new Handler() {
    public void handleMessage(Message msg) {
      switch (msg.what) {
        case DownloadService.MSG_DOWNLOAD_SUCCESS:
          onDownloadSuccess((String) msg.obj);
          break;

        case DownloadService.MSG_DOWNLOAD_FAILED:
          System.out.println("文件下载失败");
          break;

        default:
          break;
      }
    }
  };

  private void onDownloadSuccess(String filePath) {
    if (TextUtils.isEmpty(filePath)) {
      System.out.println("文件下载失败");
      return;
    }
    System.out.println("文件下载成功");
    install(filePath);
  }


  private void installAPk(String filePath) {
    //如果没有设置SDCard写权限，或者没有sdcard,apk文件保存在内存中，需要授予权限才能安装
    File file = new File(filePath);
    try {
      String[] command = {"chmod", "777", file.getPath()};
      ProcessBuilder builder = new ProcessBuilder(command);
      builder.start();
    } catch (IOException e) {
      e.printStackTrace();
    }
    Intent intent = new Intent(Intent.ACTION_VIEW);
    intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(intent);
  }

  /**
   * 通过隐式意图调用系统安装程序安装APK
   */
  public void install(String filePath) {
    File file = new File(filePath);
    try {
      String[] command = {"chmod", "777", file.getPath()};
      ProcessBuilder builder = new ProcessBuilder(command);
      builder.start();
    } catch (IOException e) {
      e.printStackTrace();
    }
    Intent intent = new Intent(Intent.ACTION_VIEW);
    // 由于没有在Activity环境下启动Activity,设置下面的标签
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    if (Build.VERSION.SDK_INT >= 24) { //判读版本是否在7.0以上 //参数1 上下文, 参数2 Provider主机地址 和配置文件中保持一致 参数3 共享的文件
      String authority = getPackageName() + ".fileprovider";
      System.out.println("authority = " + authority);
      Uri apkUri = FileProvider.getUriForFile(this, authority, file); //添加这一句表示对目标应用临时授权该Uri所代表的文件
      intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
      intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
    } else {
      intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
    }
    startActivity(intent);
  }

}
