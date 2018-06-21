package com.fruit.updatelib;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadService extends IntentService {
  public static final int MSG_DOWNLOAD_SUCCESS = 1;
  public static final int MSG_DOWNLOAD_FAILED = 2;
  // 10-10 19:14:32.618: D/DownloadService(1926): 测试缓存：41234 32kb
  // 10-10 19:16:10.892: D/DownloadService(2069): 测试缓存：41170 1kb
  // 10-10 19:18:21.352: D/DownloadService(2253): 测试缓存：39899 10kb
  private static final int BUFFER_SIZE = 10 * 1024; // 8k ~ 32K
  private static final String TAG = "DownloadService";

  private static final int NOTIFICATION_ID = 0;

  private NotificationManager mNotifyManager;
  private Builder mBuilder;

  private Messenger mMessenger;

  public DownloadService() {
    super("DownloadService");
  }

  @Override
  public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
    System.out.println("intent = [" + intent + "], flags = [" + flags + "], startId = [" + startId + "]");
    if (mMessenger == null) {
      mMessenger = (Messenger) intent.getExtras().get(Constants.APK_DOWNLOAD_MESSENGER);
    }
    return super.onStartCommand(intent, flags, startId);
  }

  @Override
  protected void onHandleIntent(Intent intent) {

    mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    mBuilder = new NotificationCompat.Builder(this);

    String appName = getString(getApplicationInfo().labelRes);
    int icon = getApplicationInfo().icon;

    mBuilder.setContentTitle(appName).setSmallIcon(icon);
    String urlStr = intent.getStringExtra(Constants.APK_DOWNLOAD_URL);
    InputStream in = null;
    FileOutputStream out = null;
    try {
      URL url = new URL(urlStr);
      HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

      urlConnection.setRequestMethod("GET");
      urlConnection.setDoOutput(false);
      urlConnection.setConnectTimeout(10 * 1000);
      urlConnection.setReadTimeout(10 * 1000);
      urlConnection.setRequestProperty("Connection", "Keep-Alive");
      urlConnection.setRequestProperty("Charset", "UTF-8");
      urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");

      urlConnection.connect();
      long bytetotal = urlConnection.getContentLength();
      long bytesum = 0;
      int byteread = 0;
      in = urlConnection.getInputStream();
      String apkName = urlStr.substring(urlStr.lastIndexOf("/") + 1, urlStr.length());
      File dir = getCacheDir();
      File apkFile = new File(dir, apkName);
      System.out.println("apkFile.getPath() = " + apkFile.getPath());
      out = new FileOutputStream(apkFile);
      byte[] buffer = new byte[BUFFER_SIZE];

      int oldProgress = 0;

      while ((byteread = in.read(buffer)) != -1) {
        bytesum += byteread;
        out.write(buffer, 0, byteread);

        int progress = (int) (bytesum * 100L / bytetotal);
        // 如果进度与之前进度相等，则不更新，如果更新太频繁，否则会造成界面卡顿
        if (progress != oldProgress) {
          updateProgress(progress);
        }
        oldProgress = progress;
      }
      // 下载完成

      sendDownloadMsg(MSG_DOWNLOAD_SUCCESS, apkFile.getAbsolutePath());

      mNotifyManager.cancel(NOTIFICATION_ID);

    } catch (Exception e) {
      e.printStackTrace();
      Log.e(TAG, "download apk file error");
      sendDownloadMsg(MSG_DOWNLOAD_FAILED, "");
      mNotifyManager.cancel(NOTIFICATION_ID);
    } finally {
      if (out != null) {
        try {
          out.close();
        } catch (IOException ignored) {

        }
      }
      if (in != null) {
        try {
          in.close();
        } catch (IOException ignored) {

        }
      }
    }
  }

  private void updateProgress(int progress) {
    //"正在下载:" + progress + "%"
    mBuilder.setContentText(this.getString(R.string.android_auto_update_download_progress, progress)).setProgress(100, progress, false);
    //setContentInent如果不设置在4.0+上没有问题，在4.0以下会报异常
    PendingIntent pendingintent = PendingIntent.getActivity(this, 0, new Intent(), PendingIntent.FLAG_CANCEL_CURRENT);
    mBuilder.setContentIntent(pendingintent);
    mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());
  }

  private void sendDownloadMsg(int what, String filePath) {
    try {
      Message message = Message.obtain();
      message.what = what;
      message.obj = filePath;
      mMessenger.send(message);
    } catch (RemoteException e) {
      e.printStackTrace();
    }
  }

}
