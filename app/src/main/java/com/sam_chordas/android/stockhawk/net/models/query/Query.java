package com.sam_chordas.android.stockhawk.net.models.query;

import com.google.gson.annotations.SerializedName;

/**
 * Created by angelsolis on 10/21/16.
 */

public class Query {
    int count;
    @SerializedName("results")
    private QuoteData data;

    public class QuoteData {
        Quote quote;
    }

    public int getCount() {
        return count;
    }

    public Quote getQuote() {
        return data.quote;
    }
}
