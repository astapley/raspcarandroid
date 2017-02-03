package com.astapley.raspcarandroid.api;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface RaspCarService {

    @GET("/drive/{throttle}/{steering}")
    Observable<ApiDrive> drive(@Path("throttle") int throttle, @Path("steering") int steering);
}
