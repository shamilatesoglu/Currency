package msa.finance.currency.activities.main;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import msa.finance.currency.data.repository.SettingsRepository;

public class SettingsViewModel extends ViewModel {
    private MutableLiveData<SettingsRepository.Settings> mSettingsMutableLiveData;

    public MutableLiveData<SettingsRepository.Settings> getSettings() {
        if (mSettingsMutableLiveData == null) {
            mSettingsMutableLiveData = SettingsRepository.getInstance().getSettings();
        }
        return mSettingsMutableLiveData;
    }
}
