package kollus.test.media.utils;

import android.util.Log;

import kollus.test.media.BuildConfig;


public class LogUtil {

    public static boolean INCLUDE = BuildConfig.DEBUG;

    private static String mLogPreFix = "[kollus.test]";

    private enum LogPrintType {
        VERBOSE {
            void printLog(String tag, String msg, Throwable tr) {
                if (tr != null) {
                    Log.v(tag, msg, tr);
                } else {
                    Log.v(tag, msg);
                }
            }
        },
        DEBUG {
            void printLog(String tag, String msg, Throwable tr) {
                if (tr != null) {
                    Log.d(tag, msg, tr);
                } else {
                    Log.d(tag, msg);
                }
            }
        },
        INFO {
            void printLog(String tag, String msg, Throwable tr) {
                if (tr != null) {
                    Log.i(tag, msg, tr);
                } else {
                    Log.i(tag, msg);
                }
            }
        },
        WARN {
            void printLog(String tag, String msg, Throwable tr) {
                if (tr != null) {
                    Log.w(tag, msg, tr);
                } else {
                    Log.w(tag, msg);
                }
            }
        },
        ERROR {
            void printLog(String tag, String msg, Throwable tr) {
                if (tr != null) {
                    Log.e(tag, msg, tr);
                } else {
                    Log.e(tag, msg);
                }
            }
        };

        abstract void printLog(String tag, String msg, Throwable tr);
    }


    private static void logPrint(LogPrintType logPrintType, String tag, String msg, Throwable tr, boolean isAlwaysShow) {
        if (INCLUDE || isAlwaysShow) {
            StackTraceElement element = new Throwable().getStackTrace()[2];
            String currClassName = element.getClassName();
            String currClassSimpleName = "";
            if (currClassName != null) {
                int nextIndexOfLastDot = currClassName.lastIndexOf(".") + 1;
                if ((nextIndexOfLastDot > 0) && (nextIndexOfLastDot < currClassName.length())) {
                    currClassSimpleName = currClassName.substring(nextIndexOfLastDot);
                }
            }
            String buildLogMsg = /*mLogPreFix + */ "[" + currClassSimpleName + "] " + element.getMethodName() + "()" + "[" + element.getLineNumber() + "]" + " >> " + msg;
            logPrintType.printLog(tag, buildLogMsg, tr);
        }
    }

    public static void v(String tag, String msg) {
        logPrint(LogPrintType.VERBOSE, tag, msg, null, false);
    }

    public static void v(String tag, String msg, boolean isAlwaysShow) {
        logPrint(LogPrintType.VERBOSE, tag, msg, null, isAlwaysShow);
    }

    public static void d(String tag, String msg, boolean isAlwaysShow) {
        logPrint(LogPrintType.DEBUG, tag, msg, null, isAlwaysShow);
    }

    public static void d(String tag, String msg) {
        logPrint(LogPrintType.DEBUG, tag, msg, null, false);
    }

    public static void i(String tag, String msg) {
        logPrint(LogPrintType.INFO, tag, msg, null, false);
    }

    public static void i(String tag, String msg, boolean isAlwaysShow) {
        logPrint(LogPrintType.INFO, tag, msg, null, isAlwaysShow);
    }

    public static void w(String tag, String msg) {
        logPrint(LogPrintType.WARN, tag, msg, null, false);
    }

    public static void w(String tag, String msg, boolean isAlwaysShow) {
        logPrint(LogPrintType.WARN, tag, msg, null, isAlwaysShow);
    }

    public static void e(String tag, String msg) {
        logPrint(LogPrintType.ERROR, tag, msg, null, false);
    }

    public static void e(String tag, String msg, boolean isAlwaysShow) {
        logPrint(LogPrintType.ERROR, tag, msg, null, isAlwaysShow);
    }
}