package com.fruit.updatelib;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Messenger;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * @author feicien (ithcheng@gmail.com)
 * @since 2016-07-05 19:21
 */
class CheckUpdateTask extends AsyncTask<String, Void, String> {
  private static final int NOTIFICATION_ID = 0x003;
  private static final String CHANNEL_ID = "update_2";
  private static final String CHANNEL_NAME = "app_update_2";


  private ProgressDialog dialog;
  private Context mContext;
  private int mType;
  private boolean mShowProgressDialog;
  private final Handler mHandler;
  private static final String url = Constants.UPDATE_URL;

  CheckUpdateTask(Context context, int type, boolean showProgressDialog, Handler handler) {

    this.mContext = context;
    this.mType = type;
    this.mShowProgressDialog = showProgressDialog;
    mHandler = handler;
  }


  protected void onPreExecute() {
    if (mShowProgressDialog) {
      dialog = new ProgressDialog(mContext);
      dialog.setMessage(mContext.getString(R.string.android_auto_update_dialog_checking));
      dialog.show();
    }
  }


  @Override
  protected void onPostExecute(String result) {

    if (dialog != null && dialog.isShowing()) {
      dialog.dismiss();
    }

    if (!TextUtils.isEmpty(result)) {
      parseJson(result);
    }
  }

  private void parseJson(String result) {
    try {

      JSONObject obj = new JSONObject(result);
      String updateMessage = obj.getString(Constants.APK_UPDATE_CONTENT);
      String apkUrl = obj.getString(Constants.APK_DOWNLOAD_URL);
      int apkCode = obj.getInt(Constants.APK_VERSION_CODE);

      int versionCode = AppUtils.getVersionCode(mContext);

      if (apkCode > versionCode) {
        if (mType == Constants.TYPE_NOTIFICATION) {
          showNotification(mContext, updateMessage, apkUrl);
        } else if (mType == Constants.TYPE_DIALOG) {
          showDialog(mContext, updateMessage, apkUrl);
        } else if (mType == Constants.TYPE_DOWNLOAD_IMMEDIATE) {
          download(mContext, apkUrl);
        }
      } else if (mShowProgressDialog) {
        Toast.makeText(mContext, mContext.getString(R.string.android_auto_update_toast_no_new_update), Toast.LENGTH_SHORT).show();
      }

    } catch (JSONException e) {
      Log.e(Constants.TAG, "parse json error");
    }
  }

  private void download(Context context, String apkUrl) {
    Intent intent = new Intent(context.getApplicationContext(), DownloadService.class);
    intent.putExtra(Constants.APK_DOWNLOAD_URL, apkUrl);
    intent.putExtra(Constants.APK_DOWNLOAD_MESSENGER, new Messenger(mHandler));
    context.startService(intent);
  }


  /**
   * Show dialog
   */
  private void showDialog(Context context, String content, String apkUrl) {
    UpdateDialog.show(context, content, apkUrl, mHandler);
  }

  /**
   * Show Notification
   */
  private void showNotification(Context context, String content, String apkUrl) {
    Intent intent = new Intent(context, DownloadService.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.putExtra(Constants.APK_DOWNLOAD_URL, apkUrl);
    intent.putExtra(Constants.APK_DOWNLOAD_MESSENGER, new Messenger(mHandler));
    PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    Notification.Builder builder;
    if (Build.VERSION.SDK_INT >= 26) {
      createNotificationChannel(notificationManager);
      builder = new Notification.Builder(context, CHANNEL_ID);
//      mBuilder.setDefaults()
//      mBuilder.setVibrate(new long[]{0});
    } else {
      builder = new Notification.Builder(context);
    }

    int smallIcon = context.getApplicationInfo().icon;
    Notification notify = builder
        .setTicker(context.getString(R.string.android_auto_update_notify_ticker))
        .setContentTitle(context.getString(R.string.android_auto_update_notify_content))
        .setContentText(content)
        .setSmallIcon(smallIcon)
        .setContentIntent(pendingIntent).build();

    notify.flags = android.app.Notification.FLAG_AUTO_CANCEL;

    notificationManager.notify(NOTIFICATION_ID, notify);
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  private void createNotificationChannel(NotificationManager notificationManager) {
    NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
    channel.enableVibration(false);
    channel.setVibrationPattern(new long[]{0});
    notificationManager.createNotificationChannel(channel);
  }
  @Override
  protected String doInBackground(String... args) {
    if (args == null || args.length == 0) return HttpUtils.get(url);
    System.out.println("args[0] = " + args[0]);
    return args[0];
  }

}
