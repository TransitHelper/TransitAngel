package com.transitangel.transitangel.api;

import android.content.Context;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class TripHelplerRequestInterceptor implements Interceptor {
    private Context mContext;

    public TripHelplerRequestInterceptor(Context context) {
        mContext = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();
        Request newRequest = builder.build();
        Response response = chain.proceed(newRequest);
        return response;
    }

}
