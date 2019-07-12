package msa.finance.currency.activities.main;

import android.arch.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import msa.finance.currency.R;
import msa.finance.currency.adapters.CurrencyRecyclerViewAdapter;
import msa.finance.currency.data.Rate;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.currency_recyclerview)
    RecyclerView currencyRecyclerView;

    @BindView(R.id.fab)
    FloatingActionButton floatingActionButton;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.base_currency_textview)
    TextView baseCurrencyTextView;

    @BindView(R.id.time_textview)
    TextView timeTextView;

    @BindView(R.id.progress_circular)
    ProgressBar mProgressBar;

    @BindView(R.id.nav_view)
    NavigationView navigationView;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    private CurrenciesViewModel mCurrenciesViewModel;

    private CurrencyRecyclerViewAdapter mCurrencyRecyclerViewAdapter;

    private ArrayList<String> mCurrencyCodeList;

    public static Set<String> currenciesToShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeCurrenciesToShow();

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        configureToolbar();
        configureNavigationView();
        configureRecyclerView();
        configureFloatingActionButton();
        initializeViewModel();
    }

    private void configureNavigationView() {
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.nav_settings:
                    break;
                case R.id.nav_about:
                    break;
            }
            return true;
        });
    }

    private void initializeCurrenciesToShow() {
        currenciesToShow = new HashSet<>();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String currencies = sharedPref.getString(getString(R.string.pref_key_currencies_to_show), "USD,TRY,GBP");
        if (currencies != null)
            currenciesToShow.addAll(Arrays.asList(currencies.split(",")));
    }

    private void configureToolbar() {
        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_24dp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void configureRecyclerView() {
        mCurrencyRecyclerViewAdapter = new CurrencyRecyclerViewAdapter(this);
        currencyRecyclerView.setAdapter(mCurrencyRecyclerViewAdapter);
        currencyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        currencyRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private void configureFloatingActionButton() {
        floatingActionButton.setOnClickListener(view -> {
            CurrencyListDialogFragment.newInstance(mCurrencyCodeList).show(getSupportFragmentManager(), "CurrencyListDialog");
        });
    }

    private void initializeViewModel() {
        mCurrenciesViewModel = ViewModelProviders.of(this).get(CurrenciesViewModel.class);
        mCurrenciesViewModel.getLatestRates().observe(this, latestRatesResponse -> {
            if (latestRatesResponse != null) {
                mCurrencyRecyclerViewAdapter.setBaseRateCode(latestRatesResponse.getBaseCurrency());

                List<Rate> rateList = new ArrayList<>();
                for (String c : currenciesToShow) {
                    rateList.add(new Rate(c, latestRatesResponse.getRate(c)));
                }

                mCurrencyRecyclerViewAdapter.setRateList(rateList);

                mCurrencyRecyclerViewAdapter.notifyDataSetChanged();

                baseCurrencyTextView.setText(String.format("BASE: %s", latestRatesResponse.getBaseCurrency()));
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
                timeTextView.setText(simpleDateFormat.format(latestRatesResponse.getTimestamp()));

                mProgressBar.setProgress(latestRatesResponse.getProgressUntilNextCall());

                mCurrencyCodeList = latestRatesResponse.getCurrencyCodeList();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
