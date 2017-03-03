package com.udacity.stockhawk.ui;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import timber.log.Timber;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String TAG = DetailActivity.class.getSimpleName();

    private String mQuoteSymbol;
    private Uri mQuoteUri;
    private LineChart mLineChart;

    private final int LOADER_ID = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (getIntent().getStringExtra(getString(R.string.detail_intent)) != null) {
            mQuoteSymbol = getIntent().getStringExtra(getString(R.string.detail_intent));
        }
        mLineChart = (LineChart) findViewById(R.id.lineChart);
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
            Timber.d(TAG, cursor.getString(Contract.Quote.POSITION_SYMBOL));
            ArrayList<String> dates = new ArrayList<>();
            ArrayList<Float> quotePrices = new ArrayList<>();

            String datePattern = "dd/MM/yyyy";
            Timber.e(quoteHistory);
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
                Timber.d(String.valueOf(value));
                return datesForFormatter.get((int) value);
            }
        });

        YAxis leftAxis = mLineChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);

        YAxis rightAxis = mLineChart.getAxisRight();
        rightAxis.setTextColor(Color.WHITE);
        mLineChart.setData(data);
        mLineChart.getLegend().setTextColor(Color.WHITE);
        mLineChart.setContentDescription("Chart of "+mQuoteSymbol);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private static String getFormattedDate(long dateInMilliSeconds, String dateFormat) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return simpleDateFormat.format(new Date(dateInMilliSeconds));
    }
}
