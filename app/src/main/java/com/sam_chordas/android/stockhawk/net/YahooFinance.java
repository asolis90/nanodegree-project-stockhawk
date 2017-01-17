package com.sam_chordas.android.stockhawk.net;

import retrofit.RestAdapter;
import retrofit.client.OkClient;

/**
 * Created by angelsolis on 10/21/16.
 */

public class YahooFinance {

    private static YahooFinance mQueryInstance;

    private YahooFinanceQueryAPI QUERY_API;

    public static YahooFinanceQueryAPI queryApi(String baseUrl) {
        if (mQueryInstance == null) {
            mQueryInstance = new YahooFinance(baseUrl);
        }
        return mQueryInstance.getQueryAPI();
    }

    public YahooFinanceQueryAPI getQueryAPI() {
        return QUERY_API;
    }

    private YahooFinance(String baseUrl) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(baseUrl)
                .setClient(new OkClient())
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();

        QUERY_API = restAdapter.create(YahooFinanceQueryAPI.class);
    }
}
