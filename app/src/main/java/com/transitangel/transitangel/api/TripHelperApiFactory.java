package com.transitangel.transitangel.api;

import android.support.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transitangel.transitangel.utils.TAConstants;
import com.transitangel.transitangel.utils.UrlProvider;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class TripHelperApiFactory {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private OkHttpClient mOkHttpClient;
    private Retrofit mRetrofitJson;
    private Retrofit mRetrofitXml;
    private TAConstants.TRANSIT_TYPE mTRANSITType;



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
    }

    private synchronized void initializeJsonRetrofit(TAConstants.TRANSIT_TYPE type) {
        mRetrofitJson = getJsonRetrofit(UrlProvider.getServerUrl(type));
    }

    public synchronized TripHelperApi getApiForJson(TAConstants.TRANSIT_TYPE type) {
        if (mRetrofitJson == null|| mTRANSITType != type) {
            initializeJsonRetrofit(type);
        }
        return mRetrofitJson.create(TripHelperApi.class);
    }

    @NonNull
    private Retrofit getJsonRetrofit(@NonNull String url) {
        return new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(JacksonConverterFactory.create(OBJECT_MAPPER))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(mOkHttpClient)
                .build();
    }

//    private synchronized void initializeRetrofitXml(TAConstants.TRANSIT_TYPE type) {
//        mRetrofitXml = getRetrofitXml(UrlProvider.getServerUrl(type));
//    }
//
//    public synchronized TripHelperApi getApiForXml(TAConstants.TRANSIT_TYPE type) {
//        if (mRetrofitXml== null || mTRANSITType != type) {
//            initializeRetrofitXml(type);
//        }
//        return mRetrofitXml.create(TripHelperApi.class);
//    }
//
//    @NonNull
//    private Retrofit getRetrofitXml(@NonNull String url) {
//        return new Retrofit.Builder()
//                .baseUrl(url)
//                .addConverterFactory(SimpleXmlConverterFactory.create())
//                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
//                .client(mOkHttpClient)
//                .build();
//    }

}
