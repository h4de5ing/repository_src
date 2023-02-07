package com.giftedcat.serialportlibrary.thread;

import android.os.SystemClock;

import com.giftedcat.serialportlibrary.SerialPortManager;
import com.giftedcat.serialportlibrary.utils.DataUtil;

import java.io.IOException;
import java.io.InputStream;

public abstract class SerialPortReadThread extends Thread {
    public abstract void onDataReceived(byte[] bytes);

    private InputStream mInputStream;
    private byte[] mReadBuffer;
    byte[] readBytes = null;
    private int readType;

    public SerialPortReadThread(InputStream inputStream, int readType) {
        mInputStream = inputStream;
        mReadBuffer = new byte[1024];
        this.readType = readType;
    }

    @Override
    public void run() {
        super.run();
        switch (readType) {
            case SerialPortManager.NORMAL:
                /** 常规读取*/
                normalRead();
                break;
            case SerialPortManager.SPLICING:
                /** 轮询读取*/
                splicingRead();
                break;
        }
    }

    /**
     * 一般使用,等待inputStream卡死返回数据
     */
    private void normalRead() {
        SerialPortManager.logger.info("normalRead");
        while (!isInterrupted()) {
            try {
                if (null == mInputStream) return;
                int size = mInputStream.read(mReadBuffer);
                if (0 >= size) return;
                byte[] readBytes = new byte[size];
                System.arraycopy(mReadBuffer, 0, readBytes, 0, size);
                onDataReceived(readBytes);
            } catch (IOException e) {
//                e.printStackTrace();
                return;
            }
        }
    }

    /**
     * 轮询读取，判断inputStream中是否还有数据，还有就拼接
     */
    private void splicingRead() {
        SerialPortManager.logger.info("splicingRead");
        while (!isInterrupted()) {
            if (null == mInputStream) return;
            int size = 0;
            try {
                int i = mInputStream.available();
                if (i == 0) size = 0;
                    // 流中有数据，则添加到临时数组中
                else size = mInputStream.read(mReadBuffer);
            } catch (IOException e) {
                //e.printStackTrace();
            }
            if (size > 0) {
                /** 发现有信息后就追加到临时变量*/
                readBytes = DataUtil.arrayAppend(readBytes, mReadBuffer, size);
                SerialPortManager.logger.info(size + "->" + DataUtil.bytesToHexString(readBytes, readBytes.length));
            } else {
                /** 没有需要追加的数据了，回调*/
                if (readBytes != null) {
                    byte sum = 0x00;
                    for (byte readByte : readBytes) sum += readByte;
                    if (sum != 0) onDataReceived(readBytes);
                }
                /** 清空，等待下个信息单元*/
                readBytes = null;
            }
            SystemClock.sleep(10);
        }
    }

    @Override
    public synchronized void start() {
        super.start();
    }

    public void release() {
        interrupt();
        if (null != mInputStream) {
            try {
                mInputStream.close();
                mInputStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
