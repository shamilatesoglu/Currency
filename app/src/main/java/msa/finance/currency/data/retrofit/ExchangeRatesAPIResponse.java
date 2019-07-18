package msa.finance.currency.data.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ExchangeRatesAPIResponse {

    @SerializedName("base")
    @Expose
    private String mBaseCurrency;

    @SerializedName("date")
    @Expose
    private String mDateString;

    @SerializedName("rates")
    @Expose
    private Map<String, Double> mRates;

    public Double getRate(String name) {
        return mRates.get(name);
    }

    public int getNumberOfCurrencies() {
        return mRates.size();
    }

    public ArrayList<String> getCurrencyCodeList() {
        ArrayList<String> currencyCodeList = new ArrayList<>(mRates.keySet());
        currencyCodeList.add(mBaseCurrency);
        Set<String> unique = new HashSet<> (currencyCodeList);
        currencyCodeList = new ArrayList<>(unique);
        Collections.sort(currencyCodeList);
        return currencyCodeList;
    }

    public String getBaseCurrency() {
        return mBaseCurrency;
    }

    private long mTimestamp;

    public void setTimestamp(long timestamp) {
        mTimestamp = timestamp;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    private int mProgressUntilNextCall;

    public void setProgressUntilNextCall(int progress) {
        mProgressUntilNextCall = progress;
    }

    public int getProgressUntilNextCall() {
        return mProgressUntilNextCall;
    }

    public boolean isSuccessful () {
        return mRates != null && mRates.size() > 0;
    }

    public void setBaseCurrency(String baseCurrency) {
        mBaseCurrency = baseCurrency;
    }
}
