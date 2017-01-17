package com.sam_chordas.android.stockhawk.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.sam_chordas.android.stockhawk.R;

/**
 * Created by angelsolis on 10/9/16.
 */

public class StockBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, context.getString(R.string.stock_not_found),Toast.LENGTH_SHORT).show();
    }
}