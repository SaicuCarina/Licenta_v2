package com.example.licenta_v2.model;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PlantApiService {
    @Headers({
            "Content-Type: application/json"
    })
    @POST("identification")
    Call<IdentificationResponse> identifyPlant(@Body PlantRequest request);

    @GET("identification/{access_token}")
    Call<IdentificationResultResponse> checkIdentificationStatus(
            @Path("access_token") String accessToken,
            @Header("Api-Key") String apiKey,
            @Query("details") String details,
            @Query("language") String language
    );
}