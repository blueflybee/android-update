package com.fruit.updatelib;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Messenger;
import android.text.Html;

class UpdateDialog {


  static void show(final Context context, String content, final String downloadUrl, final Handler handler) {
    if (isContextValid(context)) {
      AlertDialog.Builder builder = new AlertDialog.Builder(context);
      builder.setTitle(R.string.android_auto_update_dialog_title);
      builder.setMessage(Html.fromHtml(content))
          .setPositiveButton(R.string.android_auto_update_dialog_btn_download, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
              goToDownload(context, downloadUrl, handler);
            }
          })
          .setNegativeButton(R.string.android_auto_update_dialog_btn_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
          });

      AlertDialog dialog = builder.create();
      //点击对话框外面,对话框不消失
      dialog.setCanceledOnTouchOutside(false);
      dialog.show();
    }
  }

  private static boolean isContextValid(Context context) {
    return context instanceof Activity && !((Activity) context).isFinishing();
  }


  private static void goToDownload(Context context, String downloadUrl, Handler handler) {
    Intent intent = new Intent(context.getApplicationContext(), DownloadService.class);
    intent.putExtra(Constants.APK_DOWNLOAD_URL, downloadUrl);
    intent.putExtra(Constants.APK_DOWNLOAD_MESSENGER, new Messenger(handler));
    context.startService(intent);
  }
}
