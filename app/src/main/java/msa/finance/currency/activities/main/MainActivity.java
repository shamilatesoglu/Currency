package msa.finance.currency.activities.main;

import android.arch.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import msa.finance.currency.R;
import msa.finance.currency.adapters.CurrencyRecyclerViewAdapter;
import msa.finance.currency.data.Rate;
import msa.finance.currency.data.repository.SettingsRepository;
import msa.finance.currency.dialogs.AboutDialogFragment;
import msa.finance.currency.dialogs.BaseCurrencyListDialogFragment;
import msa.finance.currency.dialogs.CurrencyListDialogFragment;
import msa.finance.currency.dialogs.SettingsDialogFragment;

public class MainActivity extends AppCompatActivity implements CurrencyListDialogFragment.CurrencyListEditFinishedListener,
        BaseCurrencyListDialogFragment.BaseCurrencyEditFinishedListener,
        SettingsDialogFragment.BaseCurrencyPreferenceClickListener {

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
    ProgressBar progressBar;

    @BindView(R.id.nav_view)
    NavigationView navigationView;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @BindView(R.id.content)
    ConstraintLayout constraintLayout;

    private CurrenciesViewModel mCurrenciesViewModel;
    private SettingsViewModel mSettingsViewModel;
    private CurrencyRecyclerViewAdapter mCurrencyRecyclerViewAdapter;
    private ArrayList<String> mCurrencyCodeList;
    private List<String> mCurrenciesToShowList;
    private Snackbar mAPIAvailabilitySnackbar;
    private Map<String, Map<String, Double>> mCurrencyToHistoryMap = new HashMap<>();

    @OnClick(R.id.base_currency_textview)
    public void onClickBaseCurrencyTextView(View v) {
        BaseCurrencyListDialogFragment.newInstance(mCurrencyCodeList).show(getSupportFragmentManager(), "BaseCurrencyDialog");
    }

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

        initializeSettings();

        initializeViewModel();

        mAPIAvailabilitySnackbar = Snackbar.make(constraintLayout, R.string.error_api_not_available, Snackbar.LENGTH_INDEFINITE);
    }

    private void initializeSettings() {
        mSettingsViewModel = ViewModelProviders.of(this).get(SettingsViewModel.class);
        mSettingsViewModel.getSettings().observe(this, settings -> {
            if (settings != null) {
                baseCurrencyTextView.setText(String.format("BASE: %s", settings.getBaseCurrencyCode()));
                mCurrencyRecyclerViewAdapter.notifyDataSetChanged();
            }
        });

        mSettingsViewModel.getSettings().setValue(new SettingsRepository.Settings(
                PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.pref_key_base_currency), "EUR"),
                PreferenceManager.getDefaultSharedPreferences(this).getInt(getString(R.string.pref_key_precision), 5),
                PreferenceManager.getDefaultSharedPreferences(this).getInt(getString(R.string.pref_key_update_interval), 2)));

    }

    private void configureNavigationView() {
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.nav_settings:
                    SettingsDialogFragment.newInstance().show(getSupportFragmentManager(), "SettingsDialog");
                    break;
                case R.id.nav_about:
                    AboutDialogFragment.newInstance().show(getSupportFragmentManager(), "AboutDialog");
                    break;
            }
            drawerLayout.closeDrawers();
            return true;
        });
    }

    private void initializeCurrenciesToShow() {
        mCurrenciesToShowList = new ArrayList<>();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String currencies = sharedPref.getString(getString(R.string.pref_key_currencies_to_show), "USD,GBP,TRY");
        if (currencies != null)
            mCurrenciesToShowList.addAll(Arrays.asList(currencies.split(",")));
    }

    private void configureToolbar() {
        setSupportActionBar(toolbar);
        try {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_24dp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void configureRecyclerView() {
        mCurrencyRecyclerViewAdapter = new CurrencyRecyclerViewAdapter(this);
        mCurrencyRecyclerViewAdapter.setHistoricalRatesMap(mCurrencyToHistoryMap);
        currencyRecyclerView.setAdapter(mCurrencyRecyclerViewAdapter);
        currencyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        currencyRecyclerView.setItemAnimator(new DefaultItemAnimator());
        currencyRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    floatingActionButton.hide();
                } else floatingActionButton.show();
            }
        });
    }

    private void configureFloatingActionButton() {
        floatingActionButton.setOnClickListener(view -> {
            CurrencyListDialogFragment.newInstance(mCurrencyCodeList)
                    .setInitialCurrenciesToShowList(mCurrenciesToShowList)
                    .show(getSupportFragmentManager(), "CurrencyListDialog");
        });
    }

    private void initializeViewModel() {
        mCurrenciesViewModel = ViewModelProviders.of(this).get(CurrenciesViewModel.class);

        mCurrenciesViewModel.getLatestRates().observe(this, exchangeRatesAPIResponse -> {
            if (exchangeRatesAPIResponse != null) {
                mCurrenciesViewModel.getAPIAvailability().setValue(exchangeRatesAPIResponse.isSuccessful());
                if (exchangeRatesAPIResponse.isSuccessful()) {
                    mCurrencyRecyclerViewAdapter.setBaseCurrencyCode(exchangeRatesAPIResponse.getBaseCurrency());

                    List<Rate> rateList = new ArrayList<>();
                    for (String c : mCurrenciesToShowList) {
                        Double rate = exchangeRatesAPIResponse.getRate(c);
                        if (rate != null) {
                            SettingsRepository.Settings settings = SettingsRepository.getInstance().getSettings().getValue();
                            if (settings != null) {
                                if (!c.equals(settings.getBaseCurrencyCode())) {
                                    rateList.add(new Rate(c, exchangeRatesAPIResponse.getRate(c)));
                                }
                            }
                        }
                    }

                    mCurrencyRecyclerViewAdapter.checkIfDataSetChanged(rateList);

                    baseCurrencyTextView.setText(String.format("BASE: %s", exchangeRatesAPIResponse.getBaseCurrency()));
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
                    timeTextView.setText(simpleDateFormat.format(exchangeRatesAPIResponse.getTimestamp()));

                    progressBar.setProgress(exchangeRatesAPIResponse.getProgressUntilNextCall());

                    mCurrencyCodeList = exchangeRatesAPIResponse.getCurrencyCodeList();
                } else mCurrenciesViewModel.getAPIAvailability().setValue(false);
            } else
                mCurrenciesViewModel.getAPIAvailability().setValue(false);
        });

        mCurrenciesViewModel.getAPIAvailability().observe(this, available -> {
            if (available == null || !available) {
                mAPIAvailabilitySnackbar.show();
                progressBar.setProgress(0);
            } else if (mAPIAvailabilitySnackbar.isShown()) mAPIAvailabilitySnackbar.dismiss();
        });

        mCurrenciesViewModel.getHistoricalRates().observe(this, historicalRatesResponse -> {
            if (historicalRatesResponse != null) {
                Map<String, Map<String, Double>> rawHistoricalRatesMap = historicalRatesResponse.getHistoricalRates();
                Map<String, Map<String, Double>> processedHistoricalRatesMap = new HashMap<>();
                Set<String> currencyCodeSet = historicalRatesResponse.getCurrencyCodeSet();
                for (String currencyCode : currencyCodeSet) {
                    Map<String, Double> currentCurrencyToHistoricalRatesMap = new HashMap<>();
                    for (String dateString : rawHistoricalRatesMap.keySet()) {
                        currentCurrencyToHistoricalRatesMap.put(dateString, rawHistoricalRatesMap.get(dateString).get(currencyCode));
                    }
                    processedHistoricalRatesMap.put(currencyCode, currentCurrencyToHistoricalRatesMap);
                }
                if (!mCurrencyToHistoryMap.equals(processedHistoricalRatesMap)) {
                    mCurrencyToHistoryMap.clear();
                    mCurrencyToHistoryMap.putAll(processedHistoricalRatesMap);
                    mCurrencyRecyclerViewAdapter.notifyDataSetChanged();
                }
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

    @Override
    public void onFinishEditingCurrencyList(List<String> newCurrenciesToShowList) {
        mCurrenciesToShowList = newCurrenciesToShowList;
    }

    @Override
    public void onFinishEditingBaseCurrency(String newBaseCurrencyCode) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mSettingsViewModel.getSettings().setValue(new SettingsRepository.Settings(
                newBaseCurrencyCode,
                preferences.getInt(getString(R.string.pref_key_precision), 5),
                preferences.getInt(getString(R.string.pref_key_update_interval), 2))
        );
    }

    @Override
    public void onBaseCurrencyPreferenceClick() {
        BaseCurrencyListDialogFragment.newInstance(mCurrencyCodeList).show(getSupportFragmentManager(), "BaseCurrencyDialog");
    }
}
