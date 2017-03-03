package com.udacity.stockhawk.ui;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private String mQuoteSymbol;
    private Uri mQuoteUri;
    private LineChart mLineChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (getIntent().getStringExtra(getString(R.string.detail_intent))!=null){
            mQuoteSymbol = getIntent().getStringExtra(getString(R.string.detail_intent));
        }
        mLineChart = (LineChart) findViewById(R.id.lineChart);
        mQuoteUri = Contract.Quote.makeUriForStock(mQuoteSymbol);
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
        if (cursor.moveToFirst() && cursor!=null){

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
