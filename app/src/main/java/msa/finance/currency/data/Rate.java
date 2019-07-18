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

    @Override
    public boolean equals(Object o) {
        if (o instanceof Rate) {
            return ((Rate) o).mCurrencyCode.equals(this.mCurrencyCode) && ((Rate) o).mValue.equals(getValue());
        }
        return false;
    }

    public boolean isValid() {
        return mValue != null && mCurrencyCode.length() == 3;
    }
}
