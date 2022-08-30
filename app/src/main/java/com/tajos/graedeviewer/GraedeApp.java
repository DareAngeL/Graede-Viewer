package com.tajos.graedeviewer;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.os.HandlerCompat;

import com.google.firebase.FirebaseApp;
import com.tajos.graedeviewer.database.FirebaseDB;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GraedeApp extends Application {

    private static FirebaseDB db;
    private static GraedeApp instance;
    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private final Handler mainThreadHandler = HandlerCompat.createAsync(Looper.getMainLooper());

    public static GraedeApp instance() {
        if (instance == null) {
            instance = new GraedeApp();
        }

        return instance;
    }

    @Override
    public void onCreate() {
        FirebaseApp.initializeApp(this);
        db = new FirebaseDB(this);

        this.uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler((thread, ex) -> {
            Intent intent = new Intent(getApplicationContext(), Debug.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

            intent.putExtra("error", getStackTrace(ex));

            @SuppressLint("UnspecifiedImmutableFlag")
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 11111, intent, PendingIntent.FLAG_ONE_SHOT);

            AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
            am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 1000, pendingIntent);

            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(2);

            uncaughtExceptionHandler.uncaughtException(thread, ex);
        });
        super.onCreate();

    }

    @NonNull
    private String getStackTrace(Throwable th){
        final Writer result = new StringWriter();

        final PrintWriter printWriter = new PrintWriter(result);
        Throwable cause = th;

        while(cause != null){
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        final String stacktraceAsString = result.toString();
        printWriter.close();

        return stacktraceAsString;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public Handler getMainThreadHandler() {
        return mainThreadHandler;
    }

    public FirebaseDB getDatabase() {
        return db;
    }
}