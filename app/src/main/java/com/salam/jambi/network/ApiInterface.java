package com.salam.jambi.network;

import com.salam.jambi.model.Programs;
import com.salam.jambi.model.RadioChannelData;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterface {
    @GET(HttpParams.SHEET_API_END_POINT)
    Call<Programs> getProgramList(@Query("id") String sheetId, @Query("sheet") String sheetName);

    @GET(HttpParams.SHEET_API_END_POINT)
    Call<RadioChannelData> getChannelData(@Query("id") String sheetId, @Query("sheet") String sheetName);
}
