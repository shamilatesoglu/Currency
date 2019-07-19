package msa.finance.currency.data.repository;

import android.arch.lifecycle.MutableLiveData;
import android.os.Handler;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

import msa.finance.currency.data.retrofit.ExchangeRatesAPIRetrofitFactory;
import msa.finance.currency.data.retrofit.ExchangeRatesService;
import msa.finance.currency.data.retrofit.historical.HistoricalRatesResponse;
import msa.finance.currency.data.retrofit.latest.LatestExchangeRatesResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExchangeRatesRepository {
    private static ExchangeRatesRepository sExchangeRatesRepository;
    private ExchangeRatesService mRatesService;
    private MutableLiveData<LatestExchangeRatesResponse> mExchangeRatesAPIResponseMutableLiveData = new MutableLiveData<>();
    private MutableLiveData<HistoricalRatesResponse> mHistoricalRatesResponseMutableLiveData = new MutableLiveData<>();

    private ExchangeRatesRepository() {
        mRatesService = ExchangeRatesAPIRetrofitFactory.getRetrofitInstance().create(ExchangeRatesService.class);
    }

    public static ExchangeRatesRepository getInstance() {
        return (sExchangeRatesRepository == null) ? sExchangeRatesRepository = new ExchangeRatesRepository() : sExchangeRatesRepository;
    }

    public MutableLiveData<LatestExchangeRatesResponse> getLatestRates() {
        makeCall();
        return mExchangeRatesAPIResponseMutableLiveData;
    }

    public MutableLiveData<Boolean> getAPIAvailability() {
        MutableLiveData<Boolean> booleanMutableLiveData = new MutableLiveData<>();

        SettingsRepository.Settings settings = SettingsRepository.getInstance().getSettings().getValue();
        mRatesService.getLatestExchangeRates(settings.getBaseCurrencyCode()).enqueue(new Callback<LatestExchangeRatesResponse>() {
            @Override
            public void onResponse(Call<LatestExchangeRatesResponse> call, Response<LatestExchangeRatesResponse> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null)
                        booleanMutableLiveData.setValue(response.body().isSuccessful());
                    else booleanMutableLiveData.setValue(false);
                } else booleanMutableLiveData.setValue(false);
            }

            @Override
            public void onFailure(Call<LatestExchangeRatesResponse> call, Throwable t) {
                booleanMutableLiveData.setValue(false);
            }
        });
        return booleanMutableLiveData;
    }

    private void post(long millisPassed) {
        AtomicLong millis = new AtomicLong(millisPassed);
        SettingsRepository.Settings settings = SettingsRepository.getInstance().getSettings().getValue();
        int updateIntervalMillis = settings.getUpdateInterval() * 1000;
        new Handler().postDelayed(() -> {
            millis.addAndGet(updateIntervalMillis / 100);
            updateProgressUntilNextCall((int) (((float) millis.get() / ((float) updateIntervalMillis)) * 100));
            if (millis.get() >= updateIntervalMillis) {
                makeCall();
            } else post(millis.get());
        }, updateIntervalMillis / 100);
    }

    private void updateProgressUntilNextCall(int progress) {
        LatestExchangeRatesResponse latestExchangeRatesResponse = mExchangeRatesAPIResponseMutableLiveData.getValue();
        if (latestExchangeRatesResponse != null) {
            SettingsRepository.Settings settings = SettingsRepository.getInstance().getSettings().getValue();
            latestExchangeRatesResponse.setProgressUntilNextCall(progress);
            latestExchangeRatesResponse.setBaseCurrency(settings.getBaseCurrencyCode());
            mExchangeRatesAPIResponseMutableLiveData.setValue(latestExchangeRatesResponse);
        }
    }

    private void makeCall() {
        SettingsRepository.Settings settings = SettingsRepository.getInstance().getSettings().getValue();
        mRatesService.getLatestExchangeRates(settings.getBaseCurrencyCode()).enqueue(new Callback<LatestExchangeRatesResponse>() {
            @Override
            public void onResponse(Call<LatestExchangeRatesResponse> call, Response<LatestExchangeRatesResponse> response) {
                if (response.isSuccessful()) {
                    assert response.body() != null;

                    response.body().setTimestamp(new Date().getTime());
                    response.body().setProgressUntilNextCall(0);

                    mExchangeRatesAPIResponseMutableLiveData.setValue(response.body());
                    post(0);
                }
            }

            @Override
            public void onFailure(Call<LatestExchangeRatesResponse> call, Throwable t) {
                mExchangeRatesAPIResponseMutableLiveData.setValue(null);
                post(0);
            }
        });
        callHistoricalRates();
    }

    public MutableLiveData<HistoricalRatesResponse> getHistoricalRates() {
        callHistoricalRates();
        return mHistoricalRatesResponseMutableLiveData;
    }

    private void callHistoricalRates() {
        SettingsRepository.Settings settings = SettingsRepository.getInstance().getSettings().getValue();
        mRatesService.getHistoricalRates(settings.getBaseCurrencyCode(), getStartDate(), getEndDate()).enqueue(new Callback<HistoricalRatesResponse>() {
            @Override
            public void onResponse(Call<HistoricalRatesResponse> call, Response<HistoricalRatesResponse> response) {
                if (response.isSuccessful()) {
                    mHistoricalRatesResponseMutableLiveData.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<HistoricalRatesResponse> call, Throwable t) {
                mHistoricalRatesResponseMutableLiveData.setValue(null);
            }
        });
    }

    private String getStartDate() {
        Date today = new Date();
        Calendar cal = new GregorianCalendar();
        cal.setTime(today);
        cal.add(Calendar.DATE, -90);
        Date today30 = cal.getTime();
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(today30);
    }

    private String getEndDate() {
        Date today = new Date();
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(today);
    }
}
