package msa.finance.currency.activities.splash;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import msa.finance.currency.data.repository.LatestRatesRepository;

public class SplashViewModel extends ViewModel {
    private MutableLiveData<Boolean> mFixerAPIAvailabilityMutableLiveData;

    public LiveData<Boolean> getFixerAPIAvailability() {
        Log.d("SPLASH", "Checking API availability.");
        if (mFixerAPIAvailabilityMutableLiveData == null) {
            mFixerAPIAvailabilityMutableLiveData = LatestRatesRepository.getInstance().getFixerAPIAvailability();
        }
        return mFixerAPIAvailabilityMutableLiveData;
    }
}
