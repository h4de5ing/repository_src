package com.giftedcat.serialportlibrary.logger;

public interface ILogger {

    boolean isShowLog = false;
    boolean isShowStackTrace = false;
    String defaultTag = "";

    void showLog(boolean isShowLog);

    void showStackTrace(boolean isShowStackTrace);

    void debug(String message);

    void info(String message);

    void warning(String message);

    void error(String message);

    void monitor(String message);

    boolean isMonitorMode();

    String getDefaultTag();
}