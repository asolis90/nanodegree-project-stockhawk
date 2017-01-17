package com.sam_chordas.android.stockhawk.net.models.historical;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by angelsolis on 10/21/16.
 */

public class Serie {

    @SerializedName("Timestamp")
    private long Timestamp;
    @SerializedName("close")
    private int close;

    public long getDate() {
        return Timestamp;
    }

    public int getClose() {
        return close;
    }

}
