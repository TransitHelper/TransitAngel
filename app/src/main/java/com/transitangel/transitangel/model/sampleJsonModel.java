package com.transitangel.transitangel.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class sampleJsonModel {
    public String getResponseTimestamp() {
        return mResponseTimestamp;
    }

    public void setResponseTimestamp(String responseTimestamp) {
        mResponseTimestamp = responseTimestamp;
    }
    
    @JsonProperty("ResponseTimestamp")
    private String mResponseTimestamp;
}
