package msa.finance.currency.activities.main;

import android.arch.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import msa.finance.currency.R;
import msa.finance.currency.adapters.CurrencyRecyclerViewAdapter;
import msa.finance.currency.data.Rate;
import msa.finance.currency.data.repository.LatestRatesRepository;
import msa.finance.currency.dialogs.AboutDialogFragment;
import msa.finance.currency.dialogs.CurrencyListDialogFragment;
import msa.finance.currency.dialogs.SettingsDialogFragment;

public class MainActivity extends AppCompatActivity implements CurrencyListDialogFragment.CurrencyListEditFinishedListener {

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

    private CurrencyRecyclerViewAdapter mCurrencyRecyclerViewAdapter;

    private ArrayList<String> mCurrencyCodeList;

    private List<String> mCurrenciesToShowList;

    private Snackbar mAPIAvailabilitySnackbar;

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

        mAPIAvailabilitySnackbar = Snackbar.make(constraintLayout, R.string.error_api_not_available, Snackbar.LENGTH_INDEFINITE);
        renewUpdateInterval();
    }

    private void renewUpdateInterval() {
        LatestRatesRepository.updateIntervalMillis =
                PreferenceManager.getDefaultSharedPreferences(this).getInt(getString(R.string.pref_key_update_interval), 2) * 1000;
    }


    private void configureNavigationView() {
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.nav_settings:
                    SettingsDialogFragment.newInstance().addOnClickListener(v -> {
                        renewUpdateInterval();
                        mCurrencyRecyclerViewAdapter.notifyDataSetChanged();
                    }).show(getSupportFragmentManager(), "SettingsDialog");
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
        currencyRecyclerView.setAdapter(mCurrencyRecyclerViewAdapter);
        currencyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        currencyRecyclerView.setItemAnimator(new DefaultItemAnimator());
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

        mCurrenciesViewModel.getLatestRates().observe(this, latestRatesResponse -> {
            if (latestRatesResponse != null) {
                mCurrenciesViewModel.getAPIAvailability().setValue(latestRatesResponse.isSuccessful());
                if (latestRatesResponse.isSuccessful()) {
                    mCurrencyRecyclerViewAdapter.setBaseRateCode(latestRatesResponse.getBaseCurrency());

                    List<Rate> rateList = new ArrayList<>();
                    for (String c : mCurrenciesToShowList) {
                        rateList.add(new Rate(c, latestRatesResponse.getRate(c)));
                    }

                    mCurrencyRecyclerViewAdapter.checkIfDataSetChanged(rateList);

                    baseCurrencyTextView.setText(String.format("BASE: %s", latestRatesResponse.getBaseCurrency()));
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
                    timeTextView.setText(simpleDateFormat.format(latestRatesResponse.getTimestamp()));

                    progressBar.setProgress(latestRatesResponse.getProgressUntilNextCall());

                    mCurrencyCodeList = latestRatesResponse.getCurrencyCodeList();
                }
            } else
                mCurrenciesViewModel.getAPIAvailability().setValue(false);
        });

        mCurrenciesViewModel.getAPIAvailability().observe(this, available -> {
            if (available != null) {
                if (!available) {
                    mAPIAvailabilitySnackbar.show();
                    progressBar.setProgress(0);
                } else if (mAPIAvailabilitySnackbar.isShown()) mAPIAvailabilitySnackbar.dismiss();
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
    public void onFinishEditing(List<String> newCurrenciesToShowList) {
        mCurrenciesToShowList = newCurrenciesToShowList;
    }
}
