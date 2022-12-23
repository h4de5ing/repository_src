package com.giftedcat.serialportlibrary;

import java.io.FileDescriptor;

public class SerialPort {
    static {
        System.loadLibrary("SerialPort");
    }

    protected native FileDescriptor open(String path, int baudRate, int flags);

    protected native void close();
}
