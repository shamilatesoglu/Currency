package msa.finance.currency.activities.main;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.os.Handler;

import msa.finance.currency.data.retrofit.LatestRatesResponse;
import msa.finance.currency.data.repository.LatestRatesRepository;

public class CurrenciesViewModel extends ViewModel {
    private MutableLiveData<Boolean> mAPIAvailabilityLiveData;
    private MutableLiveData<LatestRatesResponse> mLatestRatesLiveData;

    public LiveData<LatestRatesResponse> getLatestRates() {
        if (mLatestRatesLiveData == null) {
            mLatestRatesLiveData = LatestRatesRepository.getInstance().getLatestRates();
        }
        return mLatestRatesLiveData;
    }

    public MutableLiveData<Boolean> getAPIAvailability() {
        if (mAPIAvailabilityLiveData == null) {
            mAPIAvailabilityLiveData = new MutableLiveData<>();
        }
        return mAPIAvailabilityLiveData;
    }
}
