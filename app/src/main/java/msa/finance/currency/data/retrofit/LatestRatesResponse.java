package msa.finance.currency.data.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class LatestRatesResponse {
    @SerializedName("success")
    @Expose
    private boolean mSuccess;


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


    public boolean isSuccessful() {
        return mSuccess;
    }


}
