package com.sam_chordas.android.stockhawk.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;

/**
 * Created by angelsolis on 11/24/16.
 */

public class MyStocksRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private Cursor mCursor = null;
    private Context mContext;

    public MyStocksRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
        if (mCursor != null) {
            mCursor.close();
        }

        final long identityToken = Binder.clearCallingIdentity();

        mCursor = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                new String[]{
                        QuoteColumns._ID,
                        QuoteColumns.NAME,
                        QuoteColumns.SYMBOL,
                        QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE,
                        QuoteColumns.CHANGE,
                        QuoteColumns.ISUP
                },
                QuoteColumns.ISCURRENT + " = ?", new String[]{"1"}, null);
        Binder.restoreCallingIdentity(identityToken);
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public int getCount() {
        return mCursor == null ? 0 : mCursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (position == AdapterView.INVALID_POSITION || mCursor == null || !mCursor.moveToPosition(position)) {
            return null;
        }

        // layout
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget_my_stocks_item);

        // set data in views
        views.setTextViewText(R.id.stock_symbol, mCursor.getString(mCursor.getColumnIndex(QuoteColumns.SYMBOL)));
        if (mCursor.getInt(mCursor.getColumnIndex(QuoteColumns.ISUP)) == 1) {
            views.setInt(R.id.change, mContext.getResources().getString(R.string.tv_set_background_resource), R.drawable.percent_change_pill_green);
        } else {
            views.setInt(R.id.change, mContext.getResources().getString(R.string.tv_set_background_resource), R.drawable.percent_change_pill_red);
        }

        if (Utils.showPercent) {
            views.setTextViewText(R.id.change, mCursor.getString(mCursor.getColumnIndex(QuoteColumns.PERCENT_CHANGE)));
        } else {
            views.setTextViewText(R.id.change, mCursor.getString(mCursor.getColumnIndex(QuoteColumns.CHANGE)));
        }

        final Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString(QuoteColumns.NAME, mCursor.getString(mCursor.getColumnIndex(QuoteColumns.NAME)));
        bundle.putString(QuoteColumns.SYMBOL, mCursor.getString(mCursor.getColumnIndex(QuoteColumns.SYMBOL)));
        bundle.putString(QuoteColumns.BIDPRICE, mCursor.getString(mCursor.getColumnIndex(QuoteColumns.BIDPRICE)));
        bundle.putString(QuoteColumns.CHANGE, mCursor.getString(mCursor.getColumnIndex(QuoteColumns.CHANGE)));
        bundle.putString(QuoteColumns.PERCENT_CHANGE, mCursor.getString(mCursor.getColumnIndex(QuoteColumns.PERCENT_CHANGE)));
        bundle.putInt(QuoteColumns.ISUP, mCursor.getInt(mCursor.getColumnIndex(QuoteColumns.ISUP)));

        intent.putExtras(bundle);
        views.setOnClickFillInIntent(R.id.widget_list_item, intent);

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        if (mCursor != null && mCursor.moveToPosition(position)) {
            final int QUOTES_ID_COL = 0;
            return mCursor.getLong(QUOTES_ID_COL);
        }
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
