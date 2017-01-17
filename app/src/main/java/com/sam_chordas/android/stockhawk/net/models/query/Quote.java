package com.sam_chordas.android.stockhawk.net.models.query;

/**
 * Created by angelsolis on 10/21/16.
 */

public class Quote {
    private String symbol;
    private String AverageDailyVolume;
    private String Change;
    private String DaysLow;
    private String DaysHigh;
    private String YearLow;
    private String YearHigh;
    private String MarketCapitalization;
    private String LastTradePriceOnly;
    private String DaysRange;
    private String Name;
    private String Symbol;
    private String ChangeinPercent;
    private String Bid;
    private String Date;
    private String Close;

    public String getClose() {
        return Close;
    }

    public String getDate() {
        return Date;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getAverageDailyVolume() {
        return AverageDailyVolume;
    }

    public String getChange() {
        return Change;
    }

    public String getDaysLow() {
        return DaysLow;
    }

    public String getDaysHigh() {
        return DaysHigh;
    }

    public String getYearLow() {
        return YearLow;
    }

    public String getYearHigh() {
        return YearHigh;
    }

    public String getMarketCapitalization() {
        return MarketCapitalization;
    }

    public String getLastTradePriceOnly() {
        return LastTradePriceOnly;
    }

    public String getDaysRange() {
        return DaysRange;
    }

    public String getName() {
        return Name;
    }

    public String getChangeinPercent() {
        return ChangeinPercent;
    }

    public String getBid() {
        return Bid;
    }
}
