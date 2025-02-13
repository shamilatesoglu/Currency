package msa.finance.currency.data.retrofit.historical;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.Set;

public class HistoricalRatesResponse {

    @SerializedName("base")
    @Expose
    private String mBaseCurrency;

    @SerializedName("start_at")
    @Expose
    private String mStartDateString;

    @SerializedName("end_at")
    @Expose
    private String mEndDateString;


    @SerializedName("rates")
    @Expose
    private Map<String, Map<String, Double>> mHistoricalRates;

    public String getBaseCurrency() {
        return mBaseCurrency;
    }

    public String getStartDateString() {
        return mStartDateString;
    }

    public String getEndDateString() {
        return mEndDateString;
    }

    public Map<String, Map<String, Double>> getHistoricalRates() {
        return mHistoricalRates;
    }

    public Set<String> getCurrencyCodeSet() {
        for (String date : mHistoricalRates.keySet()){
            if (mHistoricalRates.get(date) != null) {
                return mHistoricalRates.get(date).keySet();
            }
        }
        return null;
    }
}
