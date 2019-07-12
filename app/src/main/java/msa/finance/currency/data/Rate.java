package msa.finance.currency.data;

public class Rate {
    private String mCurrencyCode;
    private Double mValue;

    public Rate(String currencyCode, Double value) {
        mCurrencyCode = currencyCode;
        mValue = value;
    }

    public String getCurrencyCode() {
        return mCurrencyCode;
    }

    public Double getValue() {
        return mValue;
    }
}
