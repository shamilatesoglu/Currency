package msa.finance.currency.activities.main;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import msa.finance.currency.data.repository.ExchangeRatesRepository;
import msa.finance.currency.data.retrofit.historical.HistoricalRatesResponse;
import msa.finance.currency.data.retrofit.latest.LatestExchangeRatesResponse;

public class CurrenciesViewModel extends ViewModel {
    private MutableLiveData<Boolean> mAPIAvailabilityLiveData;
    private MutableLiveData<LatestExchangeRatesResponse> mLatestExchangeRatesLiveData;
    private MutableLiveData<HistoricalRatesResponse> mHistoricalRatesLiveData;

    public LiveData<LatestExchangeRatesResponse> getLatestRates() {
        if (mLatestExchangeRatesLiveData == null) {
            mLatestExchangeRatesLiveData = ExchangeRatesRepository.getInstance().getLatestRates();
        }
        return mLatestExchangeRatesLiveData;
    }

    public MutableLiveData<Boolean> getAPIAvailability() {
        if (mAPIAvailabilityLiveData == null) {
            mAPIAvailabilityLiveData = ExchangeRatesRepository.getInstance().getAPIAvailability();
        }
        return mAPIAvailabilityLiveData;
    }

    public LiveData<HistoricalRatesResponse> getHistoricalRates() {
        if (mHistoricalRatesLiveData == null)
            mHistoricalRatesLiveData = ExchangeRatesRepository.getInstance().getHistoricalRates();
        return mHistoricalRatesLiveData;
    }
}
