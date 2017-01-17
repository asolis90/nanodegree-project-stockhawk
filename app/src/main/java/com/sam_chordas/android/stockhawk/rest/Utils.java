package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;

import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.net.models.query.Quote;

import java.util.ArrayList;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

    private static String LOG_TAG = Utils.class.getSimpleName();

    public static boolean showPercent = true;

    public static ArrayList quoteJsonToContentVals(Quote[] quotes) {
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        if (quotes != null) {
            if (quotes.length == 1) {
                ContentProviderOperation obj = buildBatchOperation(quotes[0]);
                if (obj != null)
                    batchOperations.add(obj);
            } else {
                for (int i = 0; i < quotes.length; i++) {
                    Quote quote = quotes[i];
                    ContentProviderOperation obj = buildBatchOperation(quote);
                    if (obj != null)
                        batchOperations.add(obj);
                }
            }
        }

        return batchOperations;
    }

    public static String truncateBidPrice(String bidPrice) {
        if (bidPrice != null) {
            return String.format("%.2f", Float.parseFloat(bidPrice));
        }
        return "N/A";
    }

    public static String truncateChange(String change, boolean isPercentChange) {
        if(change != null) {
            String weight = change.substring(0, 1);
            String ampersand = "";
            if (isPercentChange) {
                ampersand = change.substring(change.length() - 1, change.length());
                change = change.substring(0, change.length() - 1);
            }
            change = change.substring(1, change.length());
            double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
            change = String.format("%.2f", round);
            StringBuffer changeBuffer = new StringBuffer(change);
            changeBuffer.insert(0, weight);
            changeBuffer.append(ampersand);
            change = changeBuffer.toString();
            return change;
        }
        return "N/A";
    }

    public static ContentProviderOperation buildBatchOperation(Quote quote) {
        String change;
        if (quote != null) {
            if (quote.getName() != null || quote.getBid() != null || quote.getChange() != null || quote.getChangeinPercent() != null) {
                ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                        QuoteProvider.Quotes.CONTENT_URI);

                change = quote.getChange();
                builder.withValue(QuoteColumns.NAME, quote.getName());
                builder.withValue(QuoteColumns.SYMBOL, quote.getSymbol());

                builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(quote.getBid()));

                builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(
                        quote.getChangeinPercent(), true));
                builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
                builder.withValue(QuoteColumns.ISCURRENT, 1);
                if (change.charAt(0) == '-') {
                    builder.withValue(QuoteColumns.ISUP, 0);
                } else {
                    builder.withValue(QuoteColumns.ISUP, 1);
                }
                return builder.build();
            }
        }

        return null;
    }
}
