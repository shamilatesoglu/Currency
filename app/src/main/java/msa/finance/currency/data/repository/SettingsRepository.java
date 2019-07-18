package msa.finance.currency.data.repository;

import android.arch.lifecycle.MutableLiveData;

public class SettingsRepository {
    private static SettingsRepository sSettingsRepository;

    public static SettingsRepository getInstance() {
        return (sSettingsRepository == null) ? sSettingsRepository = new SettingsRepository() : sSettingsRepository;
    }

    private SettingsRepository() {
        mSettingsMutableLiveData = new MutableLiveData<>();
        mSettingsMutableLiveData.setValue(new Settings("EUR", 5, 2));
    }

    private MutableLiveData<Settings> mSettingsMutableLiveData;

    public MutableLiveData<Settings> getSettings() {
        return mSettingsMutableLiveData;
    }

    public static class Settings {
        private int mPrecision;
        private String mBaseCurrencyCode;
        private int mUpdateInterval;

        public Settings(String baseCurrencyCode, int precision, int updateInterval) {
            mPrecision = precision;
            mBaseCurrencyCode = baseCurrencyCode;
            mUpdateInterval = updateInterval;
        }

        public int getPrecision() {
            return mPrecision;
        }

        public void setPrecision(int precision) {
            mPrecision = precision;
        }

        public String getBaseCurrencyCode() {
            return mBaseCurrencyCode;
        }

        public void setBaseCurrencyCode(String baseCurrencyCode) {
            mBaseCurrencyCode = baseCurrencyCode;
        }

        public int getUpdateInterval() {
            return mUpdateInterval;
        }

        public void setUpdateInterval(int updateInterval) {
            mUpdateInterval = updateInterval;
        }
    }
}
