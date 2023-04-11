package com.google.zxing.client.android;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.nio.charset.StandardCharsets;
import java.util.Hashtable;

public class ZXingUtils {
    private static final int BLACK = 0xff000000;

    public static Bitmap createQRCode(String str, int matrixWidth, int matrixHeight) throws WriterException {
        Hashtable<EncodeHintType, String> hint = new Hashtable<>();
        hint.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
        String newString = new String(str.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
        BitMatrix matrix = new MultiFormatWriter().encode(newString, BarcodeFormat.QR_CODE, matrixWidth, matrixHeight);
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (matrix.get(x, y)) {
                    pixels[y * width + x] = BLACK;
                }
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }
}
