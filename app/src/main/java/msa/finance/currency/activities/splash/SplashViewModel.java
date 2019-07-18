package msa.finance.currency.activities.splash;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import msa.finance.currency.data.repository.ExchangeRatesRepository;

public class SplashViewModel extends ViewModel {
    private MutableLiveData<Boolean> mExchangeRatesAPIAvailabilityMutableLiveData;

    public LiveData<Boolean> getAPIAvailability() {
        Log.d("SPLASH", "Checking API availability.");
        if (mExchangeRatesAPIAvailabilityMutableLiveData == null) {
            mExchangeRatesAPIAvailabilityMutableLiveData = ExchangeRatesRepository.getInstance().getAPIAvailability();
        }
        return mExchangeRatesAPIAvailabilityMutableLiveData;
    }
}
