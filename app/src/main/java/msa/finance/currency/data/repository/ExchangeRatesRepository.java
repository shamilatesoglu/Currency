package msa.finance.currency.data.repository;

import android.arch.lifecycle.MutableLiveData;
import android.os.Handler;
import android.util.Log;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import msa.finance.currency.data.retrofit.ExchangeRatesAPIResponse;
import msa.finance.currency.data.retrofit.ExchangeRatesAPIRetrofitFactory;
import msa.finance.currency.data.retrofit.ExchangeRatesService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExchangeRatesRepository {
    private static ExchangeRatesRepository sExchangeRatesRepository;

    public static ExchangeRatesRepository getInstance() {
        return (sExchangeRatesRepository == null) ? sExchangeRatesRepository = new ExchangeRatesRepository() : sExchangeRatesRepository;
    }

    private ExchangeRatesService mRatesService;

    private ExchangeRatesRepository() {
        mRatesService = ExchangeRatesAPIRetrofitFactory.getRetrofitInstance().create(ExchangeRatesService.class);
    }

    private MutableLiveData<ExchangeRatesAPIResponse> mExchangeRatesAPIResponseMutableLiveData = new MutableLiveData<>();

    public MutableLiveData<ExchangeRatesAPIResponse> getLatestRates() {
        makeCall();
        return mExchangeRatesAPIResponseMutableLiveData;
    }

    public MutableLiveData<Boolean> getAPIAvailability() {
        MutableLiveData<Boolean> booleanMutableLiveData = new MutableLiveData<>();

        SettingsRepository.Settings settings = SettingsRepository.getInstance().getSettings().getValue();
        mRatesService.getLatestExchangeRates(settings.getBaseCurrencyCode()).enqueue(new Callback<ExchangeRatesAPIResponse>() {
            @Override
            public void onResponse(Call<ExchangeRatesAPIResponse> call, Response<ExchangeRatesAPIResponse> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null)
                        booleanMutableLiveData.setValue(response.body().isSuccessful());
                    else booleanMutableLiveData.setValue(false);
                } else booleanMutableLiveData.setValue(false);
            }

            @Override
            public void onFailure(Call<ExchangeRatesAPIResponse> call, Throwable t) {
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
        ExchangeRatesAPIResponse exchangeRatesAPIResponse = mExchangeRatesAPIResponseMutableLiveData.getValue();
        if (exchangeRatesAPIResponse != null) {
            SettingsRepository.Settings settings = SettingsRepository.getInstance().getSettings().getValue();
            exchangeRatesAPIResponse.setProgressUntilNextCall(progress);
            exchangeRatesAPIResponse.setBaseCurrency(settings.getBaseCurrencyCode());
            mExchangeRatesAPIResponseMutableLiveData.setValue(exchangeRatesAPIResponse);
        }
    }

    private void makeCall() {
        SettingsRepository.Settings settings = SettingsRepository.getInstance().getSettings().getValue();
        mRatesService.getLatestExchangeRates(settings.getBaseCurrencyCode()).enqueue(new Callback<ExchangeRatesAPIResponse>() {
            @Override
            public void onResponse(Call<ExchangeRatesAPIResponse> call, Response<ExchangeRatesAPIResponse> response) {
                if (response.isSuccessful()) {
                    Log.d("CALL", "Successful.");
                    assert response.body() != null;

                    response.body().setTimestamp(new Date().getTime());
                    response.body().setProgressUntilNextCall(0);

                    mExchangeRatesAPIResponseMutableLiveData.setValue(response.body());
                    post(0);
                }
            }

            @Override
            public void onFailure(Call<ExchangeRatesAPIResponse> call, Throwable t) {
                mExchangeRatesAPIResponseMutableLiveData.setValue(null);
                post(0);
            }
        });

    }
}
