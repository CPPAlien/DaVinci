package cn.hadcn.davinci.base;

import android.content.Context;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * PtLog
 * Created by 90Chris on 2016/3/30.
 */
public class VinciLog {
    private static String LOG_TAG = "PtLog";
    private static boolean enableLog = false;//true: put on log display in logcat, put off it when release
    private static boolean enableLogFile = false; //true: save log at local, false, do not save log

    private static File logFile;
    private static Context mContext;

    /**
     * init, you should call it before any PtLog used
     * @param isEnable log is displayed
     * @param tag log tag
     * @param context context
     */
    public static void init(boolean isEnable, String tag, Context context) {
        LOG_TAG = tag;
        enableLog = isEnable;
        mContext = context;
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(context);
    }

    /**
     * start saving log in the file, it can be called multiple times
     * and will save id different file
     * the log saved under /Android/data/{package_name}/log
     */
    public static void startLogSave()  {
        if ( !enableLog ) return;
        String fileName = "log.txt";
        enableLogFile = true;
        File dir = mContext.getCacheDir();
        logFile = new File(dir, fileName);
    }

    public static String getLogPath() {
        return logFile.getAbsolutePath();
    }

    public static boolean deleteLog() {
        return logFile.delete();
    }

    private static final int RETURN_NOLOG = 99;

    public static int d( String msg ) {
        if ( !enableLog ) return RETURN_NOLOG;
        String con = dressUpTag() + ":" + msg;
        appendLog("[DEBUG]:" + LOG_TAG + ":" + con);
        return Log.d(LOG_TAG, con);
    }

    public static int e( String msg ) {
        if ( !enableLog ) return RETURN_NOLOG;
        String con = dressUpTag() + ":" + msg;
        appendLog("[ERROR]:" + LOG_TAG + ":" + con);
        return Log.e(LOG_TAG, con);
    }

    public static int e( String msg, Throwable ex ) {
        if ( !enableLog ) return RETURN_NOLOG;
        String con = dressUpTag() + ":" + msg;

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        printWriter.close();
        String result = writer.toString();

        appendLog("[ERROR]:" + LOG_TAG + ":" + con + ":" + result);
        return Log.e(LOG_TAG, con, ex);
    }

    public static int i( String msg ) {
        if ( !enableLog ) return RETURN_NOLOG;
        String con = dressUpTag() + ":" + msg;
        appendLog("[WARNING]:" + LOG_TAG + ":" + con);
        return Log.i(LOG_TAG, con);
    }

    public static int w( String msg ) {
        if ( !enableLog ) return RETURN_NOLOG;
        String con = dressUpTag() + ":" + msg;
        appendLog("[WARNING]:" + LOG_TAG + ":" + con);
        return Log.w(LOG_TAG, con);
    }

    /**
     * after saveLog opened, call the method, append the text in log file
     * @param text content
     */
    public static void appendLog(String text) {
        if ( !enableLogFile || logFile == null ) {
            return;
        }

        try
        {
            SimpleDateFormat sdFormatter = new SimpleDateFormat("[MM-dd HH:mm:ss]", Locale.CHINA);
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(sdFormatter.format(System.currentTimeMillis()));

            buf.append(text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String dressUpTag() {
        String className;
        int lineNum;
        String methodName;
        String fireName;
        StackTraceElement thisMethodStack = (new Exception()).getStackTrace()[2];
        className = thisMethodStack.getClassName();
        lineNum = thisMethodStack.getLineNumber();
        methodName = thisMethodStack.getMethodName();
        fireName = thisMethodStack.getFileName();

        int lastIndex = className.lastIndexOf(".");
        className = className.substring(lastIndex + 1, className.length());

        return className + "." + methodName + "(" + fireName + ":" + lineNum + ")";
    }
}
