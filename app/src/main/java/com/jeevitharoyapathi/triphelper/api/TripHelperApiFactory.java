package com.jeevitharoyapathi.triphelper.api;

import android.support.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeevitharoyapathi.triphelper.utils.Constants;
import com.jeevitharoyapathi.triphelper.utils.UrlProvider;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class TripHelperApiFactory {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private OkHttpClient mOkHttpClient;
    private Retrofit mRetrofit;

    static {
        OBJECT_MAPPER.setVisibility(
                OBJECT_MAPPER.getSerializationConfig().getDefaultVisibilityChecker()
                        .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                        .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                        .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                        .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public TripHelperApiFactory(TripHelplerRequestInterceptor interceptor) {
        mOkHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .addInterceptor(interceptor).build();
        initializeRetrofit();
    }

    private synchronized void initializeRetrofit(Constants.TRANSIT_TYPE type) {
        mRetrofit = getRetrofit(UrlProvider.getServerUrl(type));
    }

    public synchronized TripHelperApi getApi(Constants.TRANSIT_TYPE type) {
        if (mRetrofit == null) {
            initializeRetrofit(type);
        }
        return mRetrofit.create(TripHelperApi.class);
    }

    @NonNull
    private Retrofit getRetrofit(@NonNull String url) {
        return new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(JacksonConverterFactory.create(OBJECT_MAPPER))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(mOkHttpClient)
                .build();
    }

}
