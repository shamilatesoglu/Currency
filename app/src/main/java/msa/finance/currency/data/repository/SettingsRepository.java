package msa.finance.currency.data.repository;

import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

public class SettingsRepository {
    private static SettingsRepository sSettingsRepository;
    private MutableLiveData<Settings> mSettingsMutableLiveData;

    private SettingsRepository() {
        mSettingsMutableLiveData = new MutableLiveData<>();
        mSettingsMutableLiveData.setValue(new Settings("EUR", 5, 2));
    }

    public static SettingsRepository getInstance() {
        return (sSettingsRepository == null) ? sSettingsRepository = new SettingsRepository() : sSettingsRepository;
    }

    @NonNull
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
