package com.sam_chordas.android.stockhawk.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Created by angelsolis on  11/24/16.
 */
public class MyStocksRemoteViewsService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new MyStocksRemoteViewsFactory(getApplicationContext(), intent);
    }
}