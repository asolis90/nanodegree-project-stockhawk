package com.sam_chordas.android.stockhawk.net;

import com.sam_chordas.android.stockhawk.net.models.query.QueriesBase;
import com.sam_chordas.android.stockhawk.net.models.query.QueryBase;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by angelsolis on 10/21/16.
 */

public interface YahooFinanceQueryAPI {

    String STOCK_BASE_URL = "https://query.yahooapis.com/v1/public";

    interface params {
        String QUERY = "q";
    }

    @GET("/yql?format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys")
    void getStocksData(
            @Query(params.QUERY) String q,
            Callback<QueriesBase> callback
    );

    @GET("/yql?format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys")
    void getStockData(
            @Query(params.QUERY) String q,
            Callback<QueryBase> callback
    );
}
