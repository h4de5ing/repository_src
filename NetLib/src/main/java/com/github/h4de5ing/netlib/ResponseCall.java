package com.github.h4de5ing.netlib;


import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

public class ResponseCall<T> extends CallBackUtil<String> {

    @Override
    public String onParseResponse(Call call, Response response) {
        try {
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public void onFailure(Call call, Exception e) {

    }

    @Override
    public void onResponse(String response) {
    }
}
