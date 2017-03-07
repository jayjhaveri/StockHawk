package com.udacity.stockhawk.ui;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String TAG = DetailActivity.class.getSimpleName();

    public final static String DETAIL_URI = "detail_uri";

    private String mQuoteSymbol;
    private Uri mQuoteUri;

    @BindView(R.id.lineChart)
    LineChart mLineChart;

    @BindView(R.id.detail_price)
    TextView mTvPrice;

    @BindView(R.id.detail_symbol)
    TextView mTvSymbol;

    @BindView(R.id.detail_change)
    TextView mTvChange;

    private Uri mUri;

    private final int LOADER_ID = 101;

    private DecimalFormat dollarFormat;
    private DecimalFormat dollarFormatWithPlus;
    private DecimalFormat percentageFormat;
    private float rawAbsoluteChange;
    private String percentage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);
        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus.setPositivePrefix("+$");
        dollarFormatWithPlus.setMaximumFractionDigits(2);

        percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");

        if (getIntent().getStringExtra(getString(R.string.detail_intent)) != null) {
            mQuoteSymbol = getIntent().getStringExtra(getString(R.string.detail_intent));
        }

        if (getIntent().getData() != null) {
            mUri = getIntent().getData();
            mQuoteSymbol = mUri.getLastPathSegment();
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(mQuoteSymbol);
            getSupportActionBar().setElevation(0);
        }


        mQuoteUri = Contract.Quote.makeUriForStock(mQuoteSymbol);
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                mQuoteUri,
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst() && cursor != null) {
            //Get quote quoteHistory
            String quoteHistory = cursor.getString(Contract.Quote.POSITION_HISTORY);
            ArrayList<String> dates = new ArrayList<>();
            ArrayList<Float> quotePrices = new ArrayList<>();

            String datePattern = "dd/MM/yyyy";
            if (quoteHistory != null) {
                String[] str = quoteHistory.split("\\r?\\n|,");

                for (int i = 0; i < str.length; i++) {
                    if (i % 2 == 0) {
                        dates.add(getFormattedDate(Long.valueOf(str[i]), datePattern));
                    } else {
                        quotePrices.add(Float.valueOf(str[i]));
                    }
                }
            }
            //After getting prices prepare chart
            prepareChart(dates, quotePrices);

            mTvSymbol.setText(mQuoteSymbol);
            mTvSymbol.setTextColor(Color.WHITE);

            float price = cursor.getFloat(Contract.Quote.POSITION_PRICE);
            mTvPrice.setText(dollarFormat.format(price));
            mTvPrice.setTextColor(Color.WHITE);

            float change = cursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
            mTvChange.setTextColor(Color.WHITE);

            rawAbsoluteChange = cursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
            if (rawAbsoluteChange > 0) {
                mTvChange.setBackgroundResource(R.drawable.percent_change_pill_green);
            } else {
                mTvChange.setBackgroundResource(R.drawable.percent_change_pill_red);
            }

            //percentage change
            float percentageChange = cursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);
            percentage = percentageFormat.format(percentageChange / 100);

            if (PrefUtils.getDisplayMode(this)
                    .equals(getString(R.string.pref_display_mode_absolute_key))) {
                mTvChange.setText(dollarFormatWithPlus.format(change));
            } else {
                mTvChange.setText(percentage);
            }
            cursor.close();
        }
    }

    private void prepareChart(ArrayList<String> dates, ArrayList<Float> quotePrices) {
        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        for (int i = 0; i < quotePrices.size(); i++) {
            entries.add(new Entry(i, quotePrices.get(i)));
            labels.add(dates.get(i));
        }

        LineDataSet dataSet = new LineDataSet(entries, getString(R.string.stock_prices,mQuoteSymbol));
        LineData data = new LineData(dataSet);
        XAxis xAxis = mLineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.TOP);

        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.WHITE);

        final List<String> datesForFormatter = dates;

        xAxis.setValueFormatter(new IAxisValueFormatter() {


            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return datesForFormatter.get((int) value);
            }
        });

        YAxis leftAxis = mLineChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);

        YAxis rightAxis = mLineChart.getAxisRight();
        rightAxis.setTextColor(Color.WHITE);
        mLineChart.setData(data);
        mLineChart.getLegend().setTextColor(Color.WHITE);
        Description description = new Description();
        description.setText(mQuoteSymbol);
        description.setTextColor(Color.WHITE);
        mLineChart.setDescription(description);
//        mLineChart.animateXY(1000,1000);
        mLineChart.invalidate();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private static String getFormattedDate(long dateInMilliSeconds, String dateFormat) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return simpleDateFormat.format(new Date(dateInMilliSeconds));
    }


    private void setDisplayModeMenuItemIcon(MenuItem item) {
        if (PrefUtils.getDisplayMode(this)
                .equals(getString(R.string.pref_display_mode_absolute_key))) {
            item.setIcon(R.drawable.ic_percentage);
            item.setTitle(R.string.percentage_change);
            mTvChange.setText(percentage);

        } else {
            item.setTitle(R.string.dollar_change);
            item.setIcon(R.drawable.ic_dollar);
            mTvChange.setText(dollarFormatWithPlus.format(rawAbsoluteChange));
        }
    }

}
