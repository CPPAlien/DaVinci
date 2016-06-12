package cn.hadcn.davinci.log;

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
 * VinciLog
 * Created by 90Chris on 2016/3/30.
 */
public class VinciLog {
    private static final int DEBUG_CODE = 8;
    private static final int INFO_CODE = 4;
    private static final int WARN_CODE = 2;
    private static final int ERROR_CODE = 1;

    private static String LOG_TAG = "VinciLog";
    private static boolean enableLogFile = false; //true: save log at local, false, do not save log
    private static LogLevel logLevel = LogLevel.NONE;

    private static File logFile;
    private static Context mContext;

    /**
     * init, you should call it before any PtLog used
     * @param level log level
     * @param tag log tag
     * @param context context
     */
    public static void init(LogLevel level, String tag, Context context) {
        LOG_TAG = tag;
        logLevel = level;
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
        if ( logLevel == LogLevel.NONE ) return;
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

    public static int d( String msg, Object... args ) {
        if ( (logLevel.getValue() & DEBUG_CODE) == 0 ) return RETURN_NOLOG;
        String con = dressUpTag() + ":" + buildMessage(msg, args);
        appendLog("[DEBUG]:" + LOG_TAG + ":" + con);
        return Log.d(LOG_TAG, con);
    }

    public static int i( String msg, Object... args ) {
        if ( (logLevel.getValue() & INFO_CODE) == 0 ) return RETURN_NOLOG;
        String con = dressUpTag() + ":" + buildMessage(msg, args);
        appendLog("[INFO]:" + LOG_TAG + ":" + con);
        return Log.i(LOG_TAG, con);
    }

    public static int w( String msg, Object... args ) {
        if ( (logLevel.getValue() & WARN_CODE) == 0 ) return RETURN_NOLOG;
        String con = dressUpTag() + ":" + buildMessage(msg, args);
        appendLog("[WARNING]:" + LOG_TAG + ":" + con);
        return Log.w(LOG_TAG, con);
    }

    public static int e( String msg, Object... args ) {
        if ( (logLevel.getValue() & ERROR_CODE) == 0 ) return RETURN_NOLOG;
        String con = dressUpTag() + ":" + buildMessage(msg, args);
        appendLog("[ERROR]:" + LOG_TAG + ":" + con);
        return Log.e(LOG_TAG, con);
    }

    public static int e( String msg, Throwable ex, Object... args ) {
        if ( (logLevel.getValue() & ERROR_CODE) == 0 ) return RETURN_NOLOG;
        String con = dressUpTag() + ":" + buildMessage(msg, args);

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        printWriter.close();
        String result = writer.toString();

        appendLog("[ERROR]:" + LOG_TAG + ":" + con + ":" + result);
        return Log.e(LOG_TAG, con, ex);
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
        String caller = className + "." + methodName + "(" + fireName + ":" + lineNum + ")";
        return String.format(Locale.US, "[%d] %s", Thread.currentThread().getId(), caller);
    }

    private static String buildMessage(String format, Object... args) {
        return (args == null) ? format : String.format(Locale.US, format, args);
    }
}
