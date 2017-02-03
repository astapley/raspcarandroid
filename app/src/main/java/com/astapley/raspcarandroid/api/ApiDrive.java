package com.astapley.raspcarandroid.api;

import com.google.gson.annotations.SerializedName;

public class ApiDrive {

    @SerializedName("throttle") private String throttle;
    @SerializedName("steering") private String steering;

    public String getThrottle() {
        return throttle;
    }

    public String getSteering() {
        return steering;
    }
}
