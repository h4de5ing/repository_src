package com.giftedcat.serialportlibrary.listener;

public interface OnSerialPortDataListener {
    void onDataReceived(byte[] bytes);
}
