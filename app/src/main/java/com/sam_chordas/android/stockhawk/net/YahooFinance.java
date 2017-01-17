package com.sam_chordas.android.stockhawk.net;

import android.support.annotation.StringDef;

import retrofit.RestAdapter;
import retrofit.client.OkClient;

/**
 * Created by angelsolis on 10/21/16.
 */

public class YahooFinance {

    private static YahooFinance mQueryInstance;
    private static YahooFinance mChartInstance;
    private YahooFinanceQueryAPI QUERY_API;

    public static final String CHART_API_TYPE = "CHART_API";
    public static final String QUERY_API_TYPE = "QUERY_API";

    @StringDef({CHART_API_TYPE, QUERY_API_TYPE})
    public @interface ApiType {
    }

    public static YahooFinanceQueryAPI queryApi(String baseUrl) {
        if (mQueryInstance == null) {
            mQueryInstance = new YahooFinance(baseUrl, QUERY_API_TYPE);
        }
        return mQueryInstance.getQueryAPI();
    }

    public YahooFinanceQueryAPI getQueryAPI() {
        return QUERY_API;
    }

    private YahooFinance(String baseUrl, @ApiType String type) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(baseUrl)
                .setClient(new OkClient())
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();

        switch (type) {
            case QUERY_API_TYPE:
                QUERY_API = restAdapter.create(YahooFinanceQueryAPI.class);
                break;
        }
    }
}
