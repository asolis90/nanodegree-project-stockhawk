package com.sam_chordas.android.stockhawk.service;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.RemoteException;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.net.YahooFinance;
import com.sam_chordas.android.stockhawk.net.YahooFinanceQueryAPI;
import com.sam_chordas.android.stockhawk.net.YahooFinanceAPIHelper;
import com.sam_chordas.android.stockhawk.net.models.query.QueriesBase;
import com.sam_chordas.android.stockhawk.net.models.query.QueryBase;
import com.sam_chordas.android.stockhawk.net.models.query.Quote;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;

/**
 * Created by sam_chordas on 9/30/15.
 * The GCMTask service is primarily for periodic tasks. However, OnRunTask can be called directly
 * and is used for the initialization and adding task as well.
 */
public class StockTaskService extends GcmTaskService {
    private String LOG_TAG = StockTaskService.class.getSimpleName();

    private OkHttpClient client = new OkHttpClient();
    private Context mContext;
    private StringBuilder mStoredSymbols = new StringBuilder();
    private boolean isUpdate;

    private final String _DATA_REPLACE_TAG = "_data";
    private final String _SYMBOL = "symbol";
    private final String _ADD = "add";
    private final String NOT_FOUND_ACTION = "com.sam_chordas.android.stockhawk.STOCK_WAS_NOT_FOUND";
    private final String DEFAULT_STOCKS = "\"YHOO\",\"AAPL\",\"GOOG\",\"MSFT\"";

    public StockTaskService() {
    }

    public StockTaskService(Context context) {
        mContext = context;
    }

    String fetchData(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    int result;

    @Override
    public int onRunTask(TaskParams params) {
        Cursor initQueryCursor;
        if (mContext == null) {
            mContext = this;
        }

        result = GcmNetworkManager.RESULT_FAILURE;
        switch (params.getTag()) {
            case "init":
            case "periodic":
                isUpdate = true;
                initQueryCursor = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                        new String[]{"Distinct " + QuoteColumns.SYMBOL}, null,
                        null, null);
                String data = "";
                int count = 0;
                if (initQueryCursor.getCount() == 0 || initQueryCursor == null) {
                    data = YahooFinanceAPIHelper.QUERY_QUOTES.replace(_DATA_REPLACE_TAG, DEFAULT_STOCKS);
                    count = 4;
                } else if (initQueryCursor != null) {
                    DatabaseUtils.dumpCursor(initQueryCursor);
                    initQueryCursor.moveToFirst();
                    for (int i = 0; i < initQueryCursor.getCount(); i++) {
                        mStoredSymbols.append("\"" +
                                initQueryCursor.getString(initQueryCursor.getColumnIndex(_SYMBOL)) + "\",");
                        initQueryCursor.moveToNext();
                    }
                    mStoredSymbols.replace(mStoredSymbols.length() - 1, mStoredSymbols.length(), "");
                    data = YahooFinanceAPIHelper.QUERY_QUOTES.replace(_DATA_REPLACE_TAG, mStoredSymbols.toString());
                    count = initQueryCursor.getCount();
                }
                if (count > 1) {
                    YahooFinance.queryApi(YahooFinanceQueryAPI.STOCK_BASE_URL).getStocksData(data, new Callback<QueriesBase>() {
                        @Override
                        public void success(QueriesBase queriesBase, retrofit.client.Response response) {

                            result = GcmNetworkManager.RESULT_SUCCESS;
                            try {
                                ContentValues contentValues = new ContentValues();
                                // update ISCURRENT to 0 (false) so new data is current
                                if (isUpdate) {
                                    contentValues.put(QuoteColumns.ISCURRENT, 0);
                                    mContext.getContentResolver().update(QuoteProvider.Quotes.CONTENT_URI, contentValues,
                                            null, null);
                                }
                                ArrayList<ContentProviderOperation> cpo = Utils.quoteJsonToContentVals(queriesBase.getQuery().getQuotes());
                                if (cpo != null && cpo.size() != 0) {
                                    mContext.getContentResolver().applyBatch(QuoteProvider.AUTHORITY, cpo);
                                } else {
                                    result = GcmNetworkManager.RESULT_FAILURE;
                                    Intent intent = new Intent();
                                    intent.setAction(NOT_FOUND_ACTION);
                                    mContext.sendBroadcast(intent);
                                }
                            } catch (RemoteException | OperationApplicationException e) {
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
                        }
                    });
                } else {
                    YahooFinance.queryApi(YahooFinanceQueryAPI.STOCK_BASE_URL).getStockData(data, new Callback<QueryBase>() {
                        @Override
                        public void success(QueryBase queryBase, retrofit.client.Response response) {
                            result = GcmNetworkManager.RESULT_SUCCESS;
                            try {
                                ContentValues contentValues = new ContentValues();
                                // update ISCURRENT to 0 (false) so new data is current
                                if (isUpdate) {
                                    contentValues.put(QuoteColumns.ISCURRENT, 0);
                                    mContext.getContentResolver().update(QuoteProvider.Quotes.CONTENT_URI, contentValues,
                                            null, null);
                                }
                                Quote[] quotes = new Quote[1];
                                quotes[0] = queryBase.getQuery().getQuote();
                                ArrayList<ContentProviderOperation> cpo = Utils.quoteJsonToContentVals(quotes);
                                if (cpo != null && cpo.size() != 0) {
                                    mContext.getContentResolver().applyBatch(QuoteProvider.AUTHORITY, cpo);
                                } else {
                                    result = GcmNetworkManager.RESULT_FAILURE;
                                    Intent intent = new Intent();
                                    intent.setAction(NOT_FOUND_ACTION);
                                    mContext.sendBroadcast(intent);
                                }
                            } catch (RemoteException | OperationApplicationException e) {
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
                        }
                    });
                }
                break;

            case "add":
                isUpdate = false;
                // get symbol from params.getExtra and build query
                String stockInput = params.getExtras().getString(_SYMBOL);
                data = YahooFinanceAPIHelper.QUERY_QUOTES.replace(_DATA_REPLACE_TAG, "\"" + stockInput + "\"");
                YahooFinance.queryApi(YahooFinanceQueryAPI.STOCK_BASE_URL).getStockData(data, new Callback<QueryBase>() {
                    @Override
                    public void success(QueryBase queryBase, retrofit.client.Response response) {

                        result = GcmNetworkManager.RESULT_SUCCESS;
                        try {
                            ContentValues contentValues = new ContentValues();
                            // update ISCURRENT to 0 (false) so new data is current
                            if (isUpdate) {
                                contentValues.put(QuoteColumns.ISCURRENT, 0);
                                mContext.getContentResolver().update(QuoteProvider.Quotes.CONTENT_URI, contentValues,
                                        null, null);
                            }
                            Quote[] quotes = new Quote[1];
                            quotes[0] = queryBase.getQuery().getQuote();
                            ArrayList<ContentProviderOperation> cpo = Utils.quoteJsonToContentVals(quotes);
                            if (cpo != null && cpo.size() != 0) {
                                mContext.getContentResolver().applyBatch(QuoteProvider.AUTHORITY, cpo);
                            } else {
                                result = GcmNetworkManager.RESULT_FAILURE;
                                Intent intent = new Intent();
                                intent.setAction(NOT_FOUND_ACTION);
                                mContext.sendBroadcast(intent);
                            }
                        } catch (RemoteException | OperationApplicationException e) {

                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                    }
                });
                break;
        }
        return result;
    }
}
