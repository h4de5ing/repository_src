package com.github.h4de5ing.base;

public class DataUtils {
    public static String bytes2HexString(byte[] data) {
        return bytes2HexString(data, data.length);
    }

    public static String bytes2HexString(byte[] data, int length) {
        StringBuilder sb = new StringBuilder(data.length * 2);
        final char[] HEX = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        for (int i = 0; i < length; i++) {
            int value = data[i] & 0xff;
            sb.append(HEX[value / 16]).append(HEX[value % 16]).append(" ");
        }
        return sb.toString();
    }

    //数组拼接 {0x01} {0x02 0x03} -> {0x01,0x02,0x03}
    public static byte[] byteArrayAddByteArray(byte[] id, byte[] data) {
        byte[] resultData = new byte[id.length + data.length];
        System.arraycopy(id, 0, resultData, 0, id.length);
        resultData[id.length] = (byte) data.length;
        System.arraycopy(data, 0, resultData, id.length, data.length);
        return resultData;
    }
}
