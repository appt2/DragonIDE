package com.dragon.ide;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.util.Log;
import com.dragon.ide.ui.activities.DebugActivity;
import com.dragon.ide.utils.Environments;
import com.google.android.material.color.DynamicColors;
import editor.tsd.editors.AceEditor;

public class MyApplication extends Application {
  private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;
  private static Context mApplicationContext;

  public static Context getContext() {
    return mApplicationContext;
  }

  @Override
  public void onCreate() {
    mApplicationContext = getApplicationContext();
    // Initiate all static imports of Environments
    Environments.init();

    // Apply dynamic colors
    DynamicColors.applyToActivitiesIfAvailable(this);
    super.onCreate();

    Thread.setDefaultUncaughtExceptionHandler(
        new Thread.UncaughtExceptionHandler() {
          @Override
          public void uncaughtException(Thread thread, Throwable throwable) {
            Intent intent = new Intent(getApplicationContext(), DebugActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("error", Log.getStackTraceString(throwable));
            PendingIntent pendingIntent =
                PendingIntent.getActivity(
                    getApplicationContext(), 11111, intent, PendingIntent.FLAG_ONE_SHOT);

            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 1000, pendingIntent);

            Process.killProcess(Process.myPid());
            System.exit(1);

            uncaughtExceptionHandler.uncaughtException(thread, throwable);
          }
        });

    AceEditor.install(this);
  }
}
