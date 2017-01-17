package com.sam_chordas.android.stockhawk.net.models.query;

import com.google.gson.annotations.SerializedName;

/**
 * Created by angelsolis on 7/31/16.
 */
public class QueryBase {
    @SerializedName("query")
    private Query query;

    public Query getQuery() {
        return query;
    }
}

