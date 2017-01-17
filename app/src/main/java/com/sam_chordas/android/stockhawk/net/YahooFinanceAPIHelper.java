package com.sam_chordas.android.stockhawk.net;


/**
 * Created by angelsolis on 10/21/16.
 */

public class YahooFinanceAPIHelper {

    public static final String RANGE_ONE_YEAR = "one_year";
    public static final String RANGE_ONE_MONTH = "one_month";
    public static final String RANGE_ONE_WEEK = "one_week";
    public static String QUERY_QUOTES = "select * from yahoo.finance.quotes where symbol in (_data)";
    public static String QUERY_HISTORICAL_DATA = "select * from yahoo.finance.historicaldata where symbol=\"_symbol\" and startDate = \"_start_date\" and endDate = \"_end_date\"";
}
