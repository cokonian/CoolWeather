package com.coolweather.app;

public interface HttpCallbackListener 
{
    void onFinish(String response);
    void onError(Exception e);
}
