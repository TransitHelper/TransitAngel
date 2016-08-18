package com.transitangel.transitangel.api;

import com.transitangel.transitangel.model.sampleJsonModel;
import com.transitangel.transitangel.model.sampleXmlModel;

import java.util.List;

import retrofit2.http.GET;
import rx.Observable;

public interface TripHelperApi {

    @GET("stops?api_key=a24b8b61-63e2-4571-a41b-11490cd9ada9&operator_id=Caltrain&Format=JSON")
    Observable<sampleJsonModel> getJsonStationInfo();

    @GET("/station")
    Observable<List<sampleXmlModel>> getXmlStationInfo();

}
