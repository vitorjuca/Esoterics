package com.br.esoterics.esoadmin.network;

import com.br.esoterics.esoadmin.visualmode.Constants;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by vaniajuca on 06/11/17.
 */

public interface ApiService {

    @GET(Constants.DOMAIN + "&sensor=false")
    Call<String> fetchRouteToTheCenter(
            @Query("origin")  String origin,
            @Query("destination") String destination
    );
}
