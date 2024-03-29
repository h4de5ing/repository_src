package com.giftedcat.serialportlibrary;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.giftedcat.serialportlibrary.listener.OnOpenSerialPortListener;
import com.giftedcat.serialportlibrary.listener.OnSerialPortDataListener;
import com.giftedcat.serialportlibrary.logger.DefaultLogger;
import com.giftedcat.serialportlibrary.logger.ILogger;
import com.giftedcat.serialportlibrary.thread.SerialPortReadThread;
import com.giftedcat.serialportlibrary.utils.DataUtil;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by giftedcat on 2020/6/13.
 * SerialPortManager
 */

public class SerialPortManager extends SerialPort {
    public static ILogger logger = new DefaultLogger();
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;
    private FileDescriptor mFd;
    private OnOpenSerialPortListener mOnOpenSerialPortListener;
    private OnSerialPortDataListener mOnSerialPortDataListener;
    private HandlerThread mSendingHandlerThread;
    private Handler mSendingHandler;
    private SerialPortReadThread mSerialPortReadThread;
    public static final int NORMAL = 0;
    public static final int SPLICING = 1;

    /**
     * 数据读取方式
     */
    private static int readType;

    /**
     * 打开串口
     *
     * @param device   串口设备
     * @param baudRate 波特率
     * @return 打开是否成功
     */
    public SerialPortManager(File device, int baudRate) {
        this(device, baudRate, NORMAL);
    }

    public SerialPortManager(File device, int baudRate, int readType) {
        readType = readType;
        logger.info("openSerialPort: " + String.format("打开串口 %s  波特率 %s ,类型 %s", device.getPath(), baudRate, readType));
        // 校验串口权限
//        if (!device.canRead() || !device.canWrite()) {
//            boolean chmod777 = chmod777(device);
//            if (!chmod777) {
//                logger.info(TAG, "openSerialPort: 没有读写权限");
//                if (null != mOnOpenSerialPortListener) {
//                    mOnOpenSerialPortListener.onFail(device, OnOpenSerialPortListener.Status.NO_READ_WRITE_PERMISSION);
//                }
//                logger.info(TAG, device.getName() + "串口打开失败");
//            }
//        }
        try {
            mFd = open(device.getAbsolutePath(), baudRate, 0);
            mFileInputStream = new FileInputStream(mFd);
            mFileOutputStream = new FileOutputStream(mFd);
//            logger.info("openSerialPort: 串口已经打开 " + mFd);
            if (null != mOnOpenSerialPortListener) mOnOpenSerialPortListener.onSuccess(device);
            logger.info(device.getName() + "串口打开成功");
            startSendThread();
            startReadThread();
        } catch (Exception e) {
            logger.info(device.getName() + "串口打开失败");
            if (null != mOnOpenSerialPortListener) {
                mOnOpenSerialPortListener.onFail(device, OnOpenSerialPortListener.Status.OPEN_FAIL);
            }
            e.printStackTrace();
        }
    }

    public static void setNormal() {
        readType = SerialPortManager.NORMAL;
    }

    public static void setSplicing() {
        readType = SerialPortManager.SPLICING;
    }

    /**
     * 打开串口的日志
     */
    public static synchronized void openLog() {
        logger.showLog(true);
    }

    /**
     * 关闭串口
     */
    public void closeSerialPort() {
        if (null != mFd) {
            close();
            mFd = null;
        }
        // 停止发送消息的线程
        stopSendThread();
        // 停止接收消息的线程
        stopReadThread();

        if (null != mFileInputStream) {
            try {
                mFileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mFileInputStream = null;
        }

        if (null != mFileOutputStream) {
            try {
                mFileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mFileOutputStream = null;
        }
        mOnOpenSerialPortListener = null;
        mOnSerialPortDataListener = null;
    }

    /**
     * 添加打开串口监听
     *
     * @param listener listener
     * @return SerialPortManager
     */
    public SerialPortManager setOnOpenSerialPortListener(OnOpenSerialPortListener listener) {
        mOnOpenSerialPortListener = listener;
        return this;
    }

    /**
     * 添加数据通信监听
     *
     * @param listener listener
     * @return SerialPortManager
     */
    public SerialPortManager setOnSerialPortDataListener(OnSerialPortDataListener listener) {
        mOnSerialPortDataListener = listener;
        return this;
    }

    /**
     * 开启发送消息的线程
     */
    private void startSendThread() {
        // 开启发送消息的线程
        mSendingHandlerThread = new HandlerThread("mSendingHandlerThread");
        mSendingHandlerThread.start();
        // Handler
        mSendingHandler = new Handler(mSendingHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                byte[] sendBytes = (byte[]) msg.obj;
                if (null != mFileOutputStream && null != sendBytes && 0 < sendBytes.length) {
                    try {
                        mFileOutputStream.write(sendBytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    /**
     * 停止发送消息线程
     */
    private void stopSendThread() {
        mSendingHandler = null;
        if (null != mSendingHandlerThread) {
            mSendingHandlerThread.interrupt();
            mSendingHandlerThread.quit();
            mSendingHandlerThread = null;
        }
    }

    /**
     * 开启接收消息的线程
     */
    private void startReadThread() {
        mSerialPortReadThread = new SerialPortReadThread(mFileInputStream, readType) {
            @Override
            public void onDataReceived(byte[] bytes) {
                if (null != mOnSerialPortDataListener) {
                    mOnSerialPortDataListener.onDataReceived(bytes);
                }
            }
        };
        mSerialPortReadThread.start();
    }

    /**
     * 停止接收消息的线程
     */
    private void stopReadThread() {
        if (null != mSerialPortReadThread) {
            mSerialPortReadThread.release();
        }
    }

    /**
     * 发送十六进制的数据
     */
    public boolean sendHex(String sHex) {
        byte[] bOutArray = DataUtil.HexToByteArr(sHex);
        return sendBytes(bOutArray);
    }

    /**
     * 发送数据
     */
    public boolean sendTxt(String sTxt) {
        byte[] bOutArray = sTxt.getBytes();
        return sendBytes(bOutArray);
    }

    /**
     * 发送数据
     *
     * @param sendBytes 发送数据
     * @return 发送是否成功
     */
    public boolean sendBytes(byte[] sendBytes) {
        if (null != mFd && null != mFileInputStream && null != mFileOutputStream) {
            if (null != mSendingHandler) {
                Message message = Message.obtain();
                message.obj = sendBytes;
                return mSendingHandler.sendMessage(message);
            }
        }
        return false;
    }

    public void send(byte[] data) {
        try {
            if (null != mFileOutputStream) mFileOutputStream.write(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}