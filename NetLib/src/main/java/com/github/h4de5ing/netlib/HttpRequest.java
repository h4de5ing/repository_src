package com.github.h4de5ing.netlib;

import android.annotation.SuppressLint;
import android.util.Log;

import com.github.h4de5ing.netlib.exception.ResponseCodeErrorException;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

//TODO 用kt重写一遍 并将所有错误方方式返回
//TODO 返回错误原因Result
public class HttpRequest {
    public static String sendGet(String url, Map<String, Object> params, Map<String, String> header) throws Exception {
        StringBuilder result = new StringBuilder();
        BufferedReader in;
        String param = "";
        if (params != null && params.size() > 0) {
            param = param + "?";
            for (String key : params.keySet()) param = param + key + "=" + params.get(key) + "&";
            param = param.substring(0, param.length() - 1);
        }
        URL realUrl = new URL(url + param);
        HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
        conn.setRequestProperty("accept", "*/*");
        conn.setRequestProperty("connection", "Keep-Alive");
        conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
        if (header != null && header.size() > 0)
            for (String key : header.keySet()) conn.setRequestProperty(key, header.get(key));
        Map<String, List<String>> map = conn.getHeaderFields();
        System.out.println(url);
        for (String key : map.keySet()) System.out.println(key + "--->" + map.get(key));
        conn.connect();
        if (conn.getResponseCode() == 200) {
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) result.append(line).append("\n");
            in.close();
        } else {
            throw new ResponseCodeErrorException(conn.getResponseCode() + " " + conn.getResponseMessage());
        }
        return result.toString();
    }

    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @param url   发送请求的 URL
     * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return 所代表远程资源的响应结果
     */
    public static String sendPost(String url, Map<String, Object> param, Map<String, String> header) throws Exception {
        StringBuilder result = new StringBuilder();
        PrintWriter out;
        BufferedReader in;
        StringBuilder paramStr = new StringBuilder();
        if (param != null && param.size() > 0) {
            for (String key : param.keySet())
                paramStr.append(key).append("=").append(param.get(key)).append("&");
        }
        URL realUrl = new URL(url);
        URLConnection conn = realUrl.openConnection();
        conn.setRequestProperty("accept", "*/*");
        conn.setRequestProperty("connection", "Keep-Alive");
        conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
        if (header != null && header.size() > 0) {
            for (String key : header.keySet()) conn.setRequestProperty(key, header.get(key));
        }
        conn.setDoOutput(true);
        conn.setDoInput(true);
        out = new PrintWriter(conn.getOutputStream());
        out.print(paramStr);
        out.flush();
        System.out.println(url);
        System.out.println(paramStr);
        Map<String, List<String>> map = conn.getHeaderFields();
        for (String key : map.keySet()) System.out.println(key + "--->" + map.get(key));
        in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = in.readLine()) != null) result.append(line);
        out.close();
        in.close();
        return result.toString();
    }

    public static String postJson(String url, String json, Map<String, String> header) throws Exception {
        StringBuilder result = new StringBuilder();
        URL realUrl = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
        conn.setRequestProperty("connection", "Keep-Alive");
        conn.setRequestProperty("Charset", "UTF-8");
        conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
        conn.setRequestProperty("Content-Length", String.valueOf(json.getBytes().length));
        conn.setRequestProperty("Content-type", "application/json");
        if (header != null && header.size() > 0) {
            for (String key : header.keySet()) conn.setRequestProperty(key, header.get(key));
        }
        conn.setDoOutput(true);
        conn.setDoInput(true);
        OutputStream out = conn.getOutputStream();
        out.write(json.getBytes());
        out.flush();
        out.close();
        Map<String, List<String>> map = conn.getHeaderFields();
        System.out.println("网址:" + url);
        System.out.println("json:" + json);
        for (String key : map.keySet()) System.out.println(key + "--->" + map.get(key));
        if (conn.getResponseCode() == 200) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) result.append(line).append("\n");
            reader.close();
        } else {
            throw new ResponseCodeErrorException(conn.getResponseCode() + " " + conn.getResponseMessage());
        }
        return result.toString();
    }

    public static String uploadFile(String urlStr, Map<String, File> fileMap, Map<String, Object> params, Map<String, String> header) throws Exception {
        String result = "";
        HttpURLConnection conn;
        String BOUNDARY = "---------------------------123821742118716"; //boundary就是request头和上传文件内容的分隔符
        for (String key : params.keySet())
            System.out.println("上传参数：" + key + "->" + params.get(key));
        for (String key : header.keySet())
            System.out.println("上传头：" + key + "->" + header.get(key));
        URL url = new URL(urlStr);
        conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(30000);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setUseCaches(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
        if (header != null && header.size() > 0) {
            for (String key : header.keySet()) conn.setRequestProperty(key, header.get(key));
        }
        OutputStream out = new DataOutputStream(conn.getOutputStream());
        if (params != null) {
            StringBuilder strBuf = new StringBuilder();
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String inputName = entry.getKey();
                Object inputValue = entry.getValue();
                strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
                strBuf.append("Content-Disposition: form-data; name=\"").append(inputName).append("\"\r\n\r\n");
                strBuf.append(inputValue);
            }
            out.write(strBuf.toString().getBytes());
        }
        if (fileMap != null) {
            for (Map.Entry<String, File> entry : fileMap.entrySet()) {
                String inputName = entry.getKey();
                File file = entry.getValue();
                String filename = file.getName();
                String strBuf = "\r\n" + "--" + BOUNDARY + "\r\n" +
                        "Content-Disposition: form-data; name=\"" + inputName + "\"; filename=\"" + filename + "\"\r\n" +
                        "Content-Type:multipart/form-data\r\n\r\n";
                out.write(strBuf.getBytes());
                DataInputStream in = new DataInputStream(new FileInputStream(file));
                int bytes;
                byte[] bufferOut = new byte[1024];
                while ((bytes = in.read(bufferOut)) != -1) out.write(bufferOut, 0, bytes);
                in.close();
            }
        }

        byte[] endData = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();
        out.write(endData);
        out.flush();
        out.close();
        System.out.println("网址:" + url);
        Map<String, List<String>> map = conn.getHeaderFields();
        for (String key : map.keySet()) System.out.println("head:" + key + "--->" + map.get(key));
        StringBuilder strBuf = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) strBuf.append(line).append("\n");
        result = strBuf.toString();
        reader.close();
        conn.disconnect();
        return result;
    }

    public static String sendPut(String urlStr, String json, Map<String, String> header) throws Exception {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        if (header != null && header.size() > 0)
            for (String key : header.keySet()) conn.setRequestProperty(key, header.get(key));
        conn.setDoOutput(true);
        conn.setRequestMethod("PUT");
        conn.setUseCaches(false);
        conn.setRequestProperty("Accept-Charset", "utf-8");
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        OutputStream out = conn.getOutputStream();
        if (json != null) {
            out.write(json.getBytes());
            String footer = "\r\n" + "--" + "----------" + "--" + "\r\n";
            out.write(footer.getBytes());
            out.flush();
            out.close();
        }
        Map<String, List<String>> map = conn.getHeaderFields();
        System.out.println("网址:" + url);
        System.out.println("json:" + json);
        for (String key : map.keySet()) System.out.println(key + "--->" + map.get(key));
        if (conn.getResponseCode() == 200) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) result.append(line).append("\n");
            reader.close();
        } else {
            throw new ResponseCodeErrorException(conn.getResponseCode() + " " + conn.getResponseMessage());
        }
        return result.toString();
    }

    public static String sendDelete(String urlStr, String json, Map<String, String> header) throws Exception {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        if (header != null && header.size() > 0)
            for (String key : header.keySet()) conn.setRequestProperty(key, header.get(key));
        conn.setDoOutput(true);
        conn.setRequestMethod("DELETE");
        conn.setUseCaches(false);
        conn.setRequestProperty("Accept-Charset", "utf-8");
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        OutputStream out = conn.getOutputStream();
        if (json != null) {
            out.write(json.getBytes());
            String footer = "\r\n" + "--" + "----------" + "--" + "\r\n";
            out.write(footer.getBytes());
            out.flush();
            out.close();
        }
        Map<String, List<String>> map = conn.getHeaderFields();
        System.out.println("网址:" + url);
        System.out.println("json:" + json);

        for (String key : map.keySet()) System.out.println(key + "--->" + map.get(key));
        if (conn.getResponseCode() == 200) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) result.append(line).append("\n");
            reader.close();
        } else {
            throw new ResponseCodeErrorException(conn.getResponseCode() + " " + conn.getResponseMessage());
        }
        return result.toString();
    }

    public interface FileDownloadComplete {
        void complete(File file);
    }

    @SuppressLint({"SetWorldReadable", "SetWorldWritable"})
    public static void downloadFile(String downloadUrl, String fileSavePath, FileDownloadComplete complete) {
        File downloadFile = null;
        HttpURLConnection connection = null;
        try {
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
            URL url = new URL(downloadUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(60000);
            connection.setDoInput(true);
            InputStream is = connection.getInputStream();
            final File temp = new File(fileSavePath);
            if (temp.exists()) temp.delete();
            temp.createNewFile();
            temp.setReadable(true, false);
            temp.setWritable(true, false);
            downloadFile = temp;
            OutputStream os = new FileOutputStream(temp);
            byte[] buf = new byte[8 * 1024];
            int len;
            try {
                while ((len = is.read(buf)) != -1) os.write(buf, 0, len);
                os.flush();
                ((FileOutputStream) os).getFD().sync();
            } finally {
                closeSilently(os);
                closeSilently(is);
            }
            complete.complete(temp);
            Log.d("downloadFile", "download complete url=" + downloadUrl + ", fileSize= " + temp.length());
        } catch (Exception e) {
            Log.w("downloadFile", e);
            if (downloadFile != null) downloadFile.delete();
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    public static void closeSilently(Object closeable) {
        try {
            if (closeable != null)
                if (closeable instanceof Closeable)
                    ((Closeable) closeable).close();
        } catch (IOException ignored) {
        }
    }
}