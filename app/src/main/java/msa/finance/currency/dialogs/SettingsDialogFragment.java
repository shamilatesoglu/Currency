package msa.finance.currency.dialogs;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import msa.finance.currency.R;

public class SettingsDialogFragment extends BottomSheetDialogFragment {
    public static SettingsDialogFragment newInstance() {
        return new SettingsDialogFragment();
    }

    @BindView(R.id.pref_precision_edittext)
    EditText precisionEditText;

    @BindView(R.id.pref_update_interval_edittext)
    EditText updateIntervalEditText;

    @BindView(R.id.save_button)
    Button saveButton;

    private List<View.OnClickListener> mAdditionalOnClickListeners = new ArrayList<>();

    public SettingsDialogFragment addOnClickListener(View.OnClickListener onClickListener) {
        mAdditionalOnClickListeners.add(onClickListener);
        return this;
    }

    @OnClick(R.id.save_button)
    public void save(View v) {
        String precisionStr = precisionEditText.getText().toString();
        String updateIntervalStr = updateIntervalEditText.getText().toString();
        precisionEditText.setError((!precisionStr.isEmpty()) ? null : "Field cannot be empty!");
        updateIntervalEditText.setError((!updateIntervalStr.isEmpty()) ? null : "Field cannot be empty!");

        if (precisionEditText.getError() == null && updateIntervalEditText.getError() == null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(getString(R.string.pref_key_precision), Integer.valueOf(precisionStr));
            editor.putInt(getString(R.string.pref_key_update_interval), Integer.valueOf(updateIntervalStr));
            editor.apply();
            dismiss();
            for (View.OnClickListener onClickListener : mAdditionalOnClickListeners) {
                onClickListener.onClick(v);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings_dialog, container, false);

        ButterKnife.bind(this, root);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        precisionEditText.setText(String.valueOf(sharedPreferences.getInt(getString(R.string.pref_key_precision), 5)));
        updateIntervalEditText.setText(String.valueOf(sharedPreferences.getInt(getString(R.string.pref_key_update_interval), 2)));

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            getDialog().getWindow().findViewById(R.id.design_bottom_sheet).setBackgroundResource(android.R.color.transparent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
