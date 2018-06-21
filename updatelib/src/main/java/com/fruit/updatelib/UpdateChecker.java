package com.fruit.updatelib;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class UpdateChecker {


  public static void checkForDialog(Context context, Handler handler) {
    if (context != null) {
      new CheckUpdateTask(context, Constants.TYPE_DIALOG, true, handler).execute();
    } else {
      Log.e(Constants.TAG, "The arg context is null");
    }
  }


  public static void checkForNotification(Context context, Handler handler) {
    if (context != null) {
      new CheckUpdateTask(context, Constants.TYPE_NOTIFICATION, false, handler).execute();
    } else {
      Log.e(Constants.TAG, "The arg context is null");
    }

  }

  public static void checkForDialog(Context context, String updateJson, Handler handler) {
    if (context != null) {
      new CheckUpdateTask(context, Constants.TYPE_DIALOG, true, handler).execute(updateJson);
    } else {
      Log.e(Constants.TAG, "The arg context is null");
    }
  }


  public static void checkForNotification(Context context, String updateJson, Handler handler) {
    if (context != null) {
      new CheckUpdateTask(context, Constants.TYPE_NOTIFICATION, false, handler).execute(updateJson);
    } else {
      Log.e(Constants.TAG, "The arg context is null");
    }

  }

  /**
   * 立即下载，并在通知栏显示下载进度
   * @param context
   * @param updateJson
   * @param handler
   */
  public static void checkForDownloadImmediate(Context context, String updateJson, Handler handler) {
    if (context != null) {
      new CheckUpdateTask(context, Constants.TYPE_DOWNLOAD_IMMEDIATE, false, handler).execute(updateJson);
    } else {
      Log.e(Constants.TAG, "The arg context is null");
    }

  }


}
