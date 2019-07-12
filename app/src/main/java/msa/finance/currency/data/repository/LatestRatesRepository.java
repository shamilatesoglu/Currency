package msa.finance.currency.data.repository;

import android.arch.lifecycle.MutableLiveData;
import android.os.Handler;
import android.util.Log;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import msa.finance.currency.data.retrofit.LatestRatesResponse;
import msa.finance.currency.data.retrofit.LatestRatesService;
import msa.finance.currency.data.retrofit.FixerRetrofitFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static msa.finance.currency.util.Constants.API_KEY;
import static msa.finance.currency.util.Constants.DELAY;
import static msa.finance.currency.util.Constants.JSON_FORMAT_COMPACT;

public class LatestRatesRepository {
    private static LatestRatesRepository sLatestRatesRepository;

    public static LatestRatesRepository getInstance() {
        return (sLatestRatesRepository == null) ? sLatestRatesRepository = new LatestRatesRepository() : sLatestRatesRepository;
    }

    private LatestRatesService mLatestRatesService;

    private LatestRatesRepository() {
        mLatestRatesService = FixerRetrofitFactory.getRetrofitInstance().create(LatestRatesService.class);
    }

    private MutableLiveData<LatestRatesResponse> mLatestRatesResponseMutableLiveData = new MutableLiveData<>();

    public MutableLiveData<LatestRatesResponse> getLatestRates() {
        makeCall();
        return mLatestRatesResponseMutableLiveData;
    }

    public MutableLiveData<Boolean> getFixerAPIAvailability() {
        MutableLiveData<Boolean> booleanMutableLiveData = new MutableLiveData<>();

        mLatestRatesService.getCurrencyRates(API_KEY, JSON_FORMAT_COMPACT).enqueue(new Callback<LatestRatesResponse>() {
            @Override
            public void onResponse(Call<LatestRatesResponse> call, Response<LatestRatesResponse> response) {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    booleanMutableLiveData.setValue(response.body().isSuccessful());
                }
            }

            @Override
            public void onFailure(Call<LatestRatesResponse> call, Throwable t) {
                booleanMutableLiveData.setValue(false);
            }
        });
        return booleanMutableLiveData;
    }

    private void post(long millisPassed) {
        AtomicLong millis = new AtomicLong(millisPassed);
        new Handler().postDelayed(() -> {
            millis.addAndGet(DELAY / 100);
            updateProgressUntilNextCall((int) (((float) millis.get() / ((float) DELAY)) * 100));
            if (millis.get() >= DELAY) {
                makeCall();
            } else post(millis.get());
            Log.i("CALL", "Loading: " + ((int) (((float) millis.get() / ((float) DELAY)) * 100)));
        }, DELAY / 100);
    }

    private void updateProgressUntilNextCall(int progress) {
        LatestRatesResponse latestRatesResponse = mLatestRatesResponseMutableLiveData.getValue();
        if (latestRatesResponse != null) {
            latestRatesResponse.setProgressUntilNextCall(progress);
            mLatestRatesResponseMutableLiveData.setValue(latestRatesResponse);
        }
    }

    private void makeCall() {
        mLatestRatesService.getCurrencyRates(API_KEY, JSON_FORMAT_COMPACT).enqueue(new Callback<LatestRatesResponse>() {
            @Override
            public void onResponse(Call<LatestRatesResponse> call, Response<LatestRatesResponse> response) {
                if (response.isSuccessful()) {
                    Log.i("CALL", "Successful.");
                    assert response.body() != null;

                    response.body().setTimestamp(new Date().getTime());
                    response.body().setProgressUntilNextCall(0);

                    mLatestRatesResponseMutableLiveData.setValue(response.body());
                    post(0);
                }
            }

            @Override
            public void onFailure(Call<LatestRatesResponse> call, Throwable t) {
                mLatestRatesResponseMutableLiveData.setValue(null);
                post(0);
            }
        });
    }
}
