package com.sam_chordas.android.stockhawk.net.models.query;

import com.google.gson.annotations.SerializedName;

/**
 * Created by angelsolis on 7/31/16.
 */
public class QueriesBase {
    @SerializedName("query")
    private Queries query;

    public Queries getQuery() {
        return query;
    }
}

