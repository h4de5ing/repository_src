package com.github.h4de5ing.baseui;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * UncaughtException处理类,当程序发生Uncaught异常的时候,有该类来接管程序,并记录发送错误报告.
 * catch program crash log
 * 捕获程序崩溃日志
 *
 * @author sunzhk
 */
public class CrashHandler implements UncaughtExceptionHandler {
    public static String logpath = "/sdcard/crash/";
    public static final String TAG = "gh0st";
    /**
     * 单实例
     */
    private static CrashHandler instance;
    /**
     * 系统默认处理类
     */
    private static UncaughtExceptionHandler mDefaultHandler;
    /**
     * 是否需要自动退出程序
     */
    private boolean autoExit = true;
    /**
     * 是否需要自动重启
     */
    private boolean autoRestart = true;
    /**
     * 上下文
     */
    private Context mContext;
    /**
     * 上传回调
     */
    private UploadCollapseLog mUploadCallBack;
    /**
     * 用于存储设备信息与异常信息
     */
    private HashMap<String, String> infos = new HashMap<String, String>();
    /**
     * 格式化日期
     */
    private DateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss", Locale.CHINA);

    /**
     * 私有构造方法
     */
    private CrashHandler() {
    }

    /**
     * 获取单例
     */
    public static CrashHandler getInstance() {
        if (instance == null) {
            instance = new CrashHandler();
            mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
            Thread.setDefaultUncaughtExceptionHandler(instance);
        }
        return instance;
    }

    /**
     * @param context
     */
    public CrashHandler init(Context context) {
        init(context, true, null);
        return instance;
    }

    /**
     * @param context
     * @param autoExit
     */
    public CrashHandler init(Context context, boolean autoExit) {
        init(context, autoExit, null);
        return instance;
    }

    /**
     * 获取上下文和回调
     *
     * @param context
     */
    public CrashHandler init(Context context, boolean autoExit, UploadCollapseLog uploadCallBack) {
        mContext = context;
        this.autoExit = autoExit;
        mUploadCallBack = uploadCallBack;
        logpath = context.getExternalFilesDir("LOG").getAbsolutePath();
        L.i("日志路径：" + logpath);
        return instance;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            try {
                Thread.sleep(3000);
            } catch (Exception e) {
            }
            if (autoExit) {
                exit();
            }
        }
    }

    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        Log.e(TAG, "捕获到的崩溃异常", ex);
        collectDeviceInfo(mContext);
        new Thread() {
            @Override
            public void run() {
                super.run();
                Looper.prepare();
                Toast.makeText(mContext.getApplicationContext(), "由于发生了一个未知错误，应用崩溃，我们对此引起的不便表示抱歉", Toast.LENGTH_LONG).show();
                //showDialog(mContext);
                Looper.loop();
            }
        }.start();
        //保存日志文件
        saveCatchInfo2File(ex);
        return true;
    }

    private void showDialog(final Context context) {
        final Dialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("温馨提示");
        builder.setMessage("由于发生了一个未知错误，应用崩溃，我们对此引起的不便表示抱歉");
        //builder.setMessage("由于发生了一个未知错误，应用已关闭，我们对此引起的不便表示抱歉！您可以将错误信息上传到我们的服务器，帮助我们尽快解决该问题，谢谢！");
        builder.setNegativeButton("重启应用", (dialog12, which) -> {
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        });
        dialog = builder.create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.setCanceledOnTouchOutside(false);//设置点击屏幕其他地方，dialog不消失
        dialog.setCancelable(false);//设置点击返回键和HOme键，dialog不消失
        dialog.show();
    }

    public void collectDeviceInfo(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (packageInfo != null) {
                String versionName = packageInfo.versionName == null ? "null" : packageInfo.versionName;
                String versionCode = String.valueOf(packageInfo.versionCode);
                infos.put("versionName", versionName);
                infos.put("versionCode", versionCode);
            }
        } catch (Exception e) {
            Log.e(TAG, "an error occured when collect package info", e);
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
                Log.d(TAG, field.getName() + " : " + field.get(null));
            } catch (Exception e) {
                Log.e(TAG, "an error occured when collect crash info", e);
            }
        }
    }

    /**
     * 保存错误信息到文件
     *
     * @param ex
     * @return 返回文件名称
     */
    private void saveCatchInfo2File(Throwable ex) {
        try {
            StringBuffer sb = new StringBuffer();
            for (Map.Entry<String, String> entry : infos.entrySet()) {
                sb.append(entry.getKey());
                sb.append("=");
                sb.append(entry.getValue());
                sb.append("\n");
            }
            Writer writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            ex.printStackTrace(printWriter);
            Throwable cause = ex.getCause();
            while (cause != null) {
                cause.printStackTrace();
                cause = cause.getCause();
            }
            printWriter.close();
            String result = writer.toString();
            sb.append(result);
            L.writeLog(sb.toString(), "crash");
        } catch (Exception e) {
            Log.e(TAG, "an error occured while writing file...", e);
        }
    }

    public void exit() {
        if (autoRestart) {
            setRestart();
        }
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }

    private void setRestart() {
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        PackageManager packageManager = mContext.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(mContext.getPackageName());
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0x000001, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, 1000, pendingIntent);
    }

    /**
     * 设置是否需要自动重启
     *
     * @param autoRestart
     */
    public CrashHandler setAutoRestart(boolean autoRestart) {
        this.autoRestart = autoRestart;
        autoExit = autoExit | this.autoRestart;
        return instance;
    }

    public interface UploadCollapseLog {
        void upload(String logPath);
    }

    //删除3天前的日志文件
    public static void moveFileToReady(String fromDir) {
        File srcDir = new File(fromDir);
        if (!srcDir.exists()) {
            return;
        }
        File[] files = srcDir.listFiles();
        if (files == null || files.length <= 0) {
            return;
        }
        Date today = new Date();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                try {
                    File ff = files[i];
                    long time = ff.lastModified();
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(time);
                    Date lastModified = cal.getTime();
                    long days = getDistDates(today, lastModified);
                    if (days >= 3) {
                        files[i].delete();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static long getDistDates(Date startDate, Date endDate) {
        long totalDate = 0;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        long timeStart = calendar.getTimeInMillis();
        calendar.setTime(endDate);
        long timeEnd = calendar.getTimeInMillis();
        totalDate = Math.abs((timeEnd - timeStart)) / (1000 * 60 * 60 * 24);
        return totalDate;
    }
}
