package com.sam_chordas.android.stockhawk.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.net.YahooFinance;
import com.sam_chordas.android.stockhawk.net.YahooFinanceQueryAPI;
import com.sam_chordas.android.stockhawk.net.YahooFinanceAPIHelper;
import com.sam_chordas.android.stockhawk.net.models.query.QueriesBase;
import com.sam_chordas.android.stockhawk.rest.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by angelsolis on 11/23/16.
 */

public class StockDetailsActivity extends AppCompatActivity implements TabHost.OnTabChangeListener {

    private final String TAG = "StockDetailsActivity";

    private Context mContext;
    private static String ARG_NAME = "name";
    private static String ARG_SYMBOL = "symbol";
    private static String ARG_BID_PRICE = "bid_price";
    private static String ARG_CHANGE = "change";
    private static String ARG_PERCENT_CHANGE = "percent_change";
    private static String ARG_IS_UP = "is_up";
    private final String ONE_WEEK = "1 Week";
    private final String ONE_MONTH = "1 Month";
    private final String ONE_YEAR = "1 Year";

    @Bind(R.id.activity_coordinator_layout)
    CoordinatorLayout coordinatorLayout;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.activity_stock_details_name)
    TextView textViewName;
    @Bind(R.id.activity_stock_details_change)
    TextView textViewChange;
    @Bind(R.id.activity_stock_details_percent_change)
    TextView textViewPercentChange;
    @Bind(R.id.activity_stock_details_bid)
    TextView textViewBid;
    @Bind(R.id.stock_chart_network_error)
    TextView textViewNetworkError;
    @Bind(android.R.id.tabhost)
    TabHost mTabHost;
    @Bind(R.id.stock_chart)
    LineChartView mChart;
    @Bind(android.R.id.tabcontent)
    View mTabContent;

    private String mName;
    private String mSymbol;
    private String mChange;
    private String mPercentChange;
    private String mBidPrice;
    private int mIsUp;
    private String mCurrRange;

    public static void launch(Activity activity, String name, String symbol, String bidPrice, String change, String percentChange, int isUp, View view) {
        Intent intent = new Intent(activity, StockDetailsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(ARG_NAME, name);
        bundle.putString(ARG_SYMBOL, symbol);
        bundle.putString(ARG_BID_PRICE, bidPrice);
        bundle.putString(ARG_CHANGE, change);
        bundle.putString(ARG_PERCENT_CHANGE, percentChange);
        bundle.putInt(ARG_IS_UP, isUp);
        intent.putExtras(bundle);
        ActivityOptionsCompat options;
        if (view != null) {
            options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    activity, new Pair<>(view, ""));
        } else {
            // set options without shared element transition
            options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity);
        }
        ActivityCompat.startActivity(activity, intent, options.toBundle());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_stock_details);
        ButterKnife.bind(this);

        if (savedInstanceState == null) {
            mName = getIntent().getExtras().getString(ARG_NAME);
            mSymbol = getIntent().getExtras().getString(ARG_SYMBOL);
            mBidPrice = getIntent().getExtras().getString(ARG_BID_PRICE);
            mChange = getIntent().getExtras().getString(ARG_CHANGE);
            mPercentChange = getIntent().getExtras().getString(ARG_PERCENT_CHANGE);
            mIsUp = getIntent().getExtras().getInt(ARG_IS_UP);
        }

        setActionBar();
        setCurrentData();
        initTabs();
        loadData();
    }

    private void setCurrentData() {
        textViewName.setText(mName);
        textViewChange.setText(mChange);
        handleChangeBackground();
        textViewPercentChange.setText(mPercentChange);
        textViewBid.setText(mBidPrice);
    }

    private void handleChangeBackground() {
        if (mIsUp == 1) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                textViewPercentChange.setBackgroundDrawable(
                        mContext.getResources().getDrawable(R.drawable.percent_change_pill_green));
            } else {
                textViewPercentChange.setBackground(
                        mContext.getResources().getDrawable(R.drawable.percent_change_pill_green));
            }
        } else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                textViewPercentChange.setBackgroundDrawable(
                        mContext.getResources().getDrawable(R.drawable.percent_change_pill_red));
            } else {
                textViewPercentChange.setBackground(
                        mContext.getResources().getDrawable(R.drawable.percent_change_pill_red));
            }
        }
    }

    private void loadData() {
        Date date = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, -1);
        Date start = c.getTime();

        switch (mCurrRange) {
            case YahooFinanceAPIHelper.RANGE_ONE_WEEK:
                c.add(Calendar.DATE, -6);
                break;
            case YahooFinanceAPIHelper.RANGE_ONE_MONTH:
                c.add(Calendar.DATE, -30);
                break;
            case YahooFinanceAPIHelper.RANGE_ONE_YEAR:
                c.add(Calendar.DATE, -364);
                break;
        }

        Date end = c.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String startDate = sdf.format(start);
        String endDate = sdf.format(end);

        String data = YahooFinanceAPIHelper.QUERY_HISTORICAL_DATA.replace("_symbol", mSymbol);
        data = data.replace("_start_date", endDate);
        data = data.replace("_end_date", startDate);

        if (isConnected()) {
            textViewNetworkError.setVisibility(View.GONE);
            YahooFinance.queryApi(YahooFinanceQueryAPI.STOCK_BASE_URL).getStocksData(data,
                    new Callback<QueriesBase>() {
                        @Override
                        public void success(QueriesBase queriesBase, Response response) {
                            handleQueries(queriesBase);
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Log.d(TAG, error.getMessage());
                        }
                    });
        } else {
            showNoNetWorkSnackBar();
            textViewNetworkError.setVisibility(View.VISIBLE);
            mChart.setVisibility(View.GONE);
        }
    }

    private void handleQueries(QueriesBase queriesBase) {
        final List<AxisValue> axisValuesX = new ArrayList<>();
        final List<PointValue> pointValues = new ArrayList<>();
        for (int i = 0; i < queriesBase.getQuery().getQuotes().length; i++) {
            String date = queriesBase.getQuery().getQuotes()[i].getDate();
            String close = queriesBase.getQuery().getQuotes()[i].getClose();

            int x = queriesBase.getQuery().getQuotes().length - 1 - i;

            PointValue pointValue = new PointValue(x, Float.valueOf(close));
            pointValue.setLabel(date);
            pointValues.add(pointValue);

            if (i == 0) {
                AxisValue axisValueX = new AxisValue(x);
                axisValueX.setLabel(date);
                axisValuesX.add(axisValueX);
            }

            // Set labels for x-axis (we have to reduce its number to avoid overlapping text).
            if (i != 0 && i % (queriesBase.getQuery().getQuotes().length / 3) == 0) {
                AxisValue axisValueX = new AxisValue(x);
                axisValueX.setLabel(date);
                axisValuesX.add(axisValueX);
            }
        }

        // prepare data for chart
        Line line = new Line(pointValues).setColor(ContextCompat.getColor(getApplicationContext(), R.color.material_blue_500)).setCubic(false);
        List<Line> lines = new ArrayList<>();
        lines.add(line);
        LineChartData lineChartData = new LineChartData();
        lineChartData.setLines(lines);

        // init x-axis
        Axis axisX = new Axis(axisValuesX);
        axisX.setHasLines(true);
        axisX.setMaxLabelChars(4);
        lineChartData.setAxisXBottom(axisX);

        // init y-axis
        Axis axisY = new Axis();
        axisY.setAutoGenerated(true);
        axisY.setHasLines(true);
        axisY.setMaxLabelChars(4);
        lineChartData.setAxisYLeft(axisY);

        // update chart
        mChart.setLineChartData(lineChartData);

        // show chart
        mChart.setVisibility(View.VISIBLE);
        mTabContent.setVisibility(View.VISIBLE);
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    public void showNoNetWorkSnackBar() {
        Snackbar.make(coordinatorLayout, R.string.network_toast, Snackbar.LENGTH_LONG).show();
    }

    private boolean isConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    public void setActionBar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(mSymbol);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.global, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_change_units) {
            // this is for changing stock changes from percent value to dollar value
            Utils.showPercent = !Utils.showPercent;
            this.getContentResolver().notifyChange(QuoteProvider.Quotes.CONTENT_URI, null);
        }

        return super.onOptionsItemSelected(item);
    }

    private void initTabs() {
        mTabHost.setup();

        TabHost.TabSpec tabSpec;
        tabSpec = mTabHost.newTabSpec(ONE_WEEK);
        tabSpec.setIndicator(ONE_WEEK);
        tabSpec.setContent(android.R.id.tabcontent);
        mTabHost.addTab(tabSpec);

        tabSpec = mTabHost.newTabSpec(ONE_MONTH);
        tabSpec.setIndicator(ONE_MONTH);
        tabSpec.setContent(android.R.id.tabcontent);
        mTabHost.addTab(tabSpec);

        tabSpec = mTabHost.newTabSpec(ONE_YEAR);
        tabSpec.setIndicator(ONE_YEAR);
        tabSpec.setContent(android.R.id.tabcontent);
        mTabHost.addTab(tabSpec);

        mTabHost.setOnTabChangedListener(this);
        mCurrRange = YahooFinanceAPIHelper.RANGE_ONE_WEEK;
        mTabHost.setCurrentTab(0);
    }

    @Override
    public void onTabChanged(String tabId) {
        if (!mCurrRange.equals(tabId)) {
            switch (tabId) {
                case ONE_WEEK:
                    mCurrRange = YahooFinanceAPIHelper.RANGE_ONE_WEEK;
                    mTabHost.setCurrentTab(0);
                    break;
                case ONE_MONTH:
                    mCurrRange = YahooFinanceAPIHelper.RANGE_ONE_MONTH;
                    mTabHost.setCurrentTab(1);
                    break;
                case ONE_YEAR:
                    mCurrRange = YahooFinanceAPIHelper.RANGE_ONE_YEAR;
                    mTabHost.setCurrentTab(2);
                    break;
            }
            loadData();
        }
    }
}
