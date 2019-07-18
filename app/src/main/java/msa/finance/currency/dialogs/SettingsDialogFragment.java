package msa.finance.currency.dialogs;

import android.content.Context;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import msa.finance.currency.R;
import msa.finance.currency.data.repository.SettingsRepository;

import static msa.finance.currency.util.Constants.COUNTRY_FLAG_API_FORMAT;
import static msa.finance.currency.util.Constants.CURRENCY_CODE_TO_COUNTRY_CODE_MAP;

public class SettingsDialogFragment extends BottomSheetDialogFragment {
    @BindView(R.id.pref_precision_edittext)
    EditText precisionEditText;
    @BindView(R.id.pref_update_interval_edittext)
    EditText updateIntervalEditText;
    @BindView(R.id.pref_display_base_currency_textview)
    TextView baseCurrencyTextView;
    @BindView(R.id.save_button)
    Button saveButton;
    @BindView(R.id.base_currency_imageview)
    ImageView baseCurrencyImageView;
    private BaseCurrencyPreferenceClickListener mListener;

    public static SettingsDialogFragment newInstance() {
        return new SettingsDialogFragment();
    }

    @OnClick(R.id.pref_base_currency)
    public void onBaseCurrencyPreferenceClick() {
        mListener.onBaseCurrencyPreferenceClick();
        dismiss();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (BaseCurrencyPreferenceClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement BaseCurrencyPreferenceClickListener");
        }
    }

    @OnClick(R.id.save_button)
    public void save(View v) {
        String precisionStr = precisionEditText.getText().toString();
        String updateIntervalStr = updateIntervalEditText.getText().toString();
        precisionEditText.setError((!precisionStr.isEmpty()) ? null : "Field cannot be empty!");
        updateIntervalEditText.setError((!updateIntervalStr.isEmpty()) ? null : "Field cannot be empty!");

        updateIntervalEditText.setError((Integer.valueOf(updateIntervalStr) >= 2) ? null : "The value must be greater than 1!");

        if (precisionEditText.getError() == null && updateIntervalEditText.getError() == null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(getString(R.string.pref_key_precision), Integer.valueOf(precisionStr));
            editor.putInt(getString(R.string.pref_key_update_interval), Integer.valueOf(updateIntervalStr));
            editor.apply();

            SettingsRepository.getInstance().getSettings().setValue(new SettingsRepository.Settings(
                    sharedPreferences.getString(getString(R.string.pref_key_base_currency), "EUR"),
                    Integer.valueOf(precisionStr),
                    Integer.valueOf(updateIntervalStr))
            );

            dismiss();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings_dialog, container, false);

        ButterKnife.bind(this, root);

        SettingsRepository.Settings settings = SettingsRepository.getInstance().getSettings().getValue();
        precisionEditText.setText(String.valueOf(settings.getPrecision()));
        updateIntervalEditText.setText(String.valueOf(settings.getUpdateInterval()));
        baseCurrencyTextView.setText(settings.getBaseCurrencyCode());

        Picasso.get().load(String.format(COUNTRY_FLAG_API_FORMAT, CURRENCY_CODE_TO_COUNTRY_CODE_MAP.get(settings.getBaseCurrencyCode())))
                .into(baseCurrencyImageView);

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

    public interface BaseCurrencyPreferenceClickListener {
        void onBaseCurrencyPreferenceClick();
    }
}
