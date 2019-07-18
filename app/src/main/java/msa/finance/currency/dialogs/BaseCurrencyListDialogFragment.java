package msa.finance.currency.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import msa.finance.currency.R;
import msa.finance.currency.data.repository.SettingsRepository;

import static msa.finance.currency.util.Constants.COUNTRY_FLAG_API_FORMAT;
import static msa.finance.currency.util.Constants.CURRENCY_CODE_TO_COUNTRY_CODE_MAP;

public class BaseCurrencyListDialogFragment extends BottomSheetDialogFragment {
    private static final String ARG_CURRENCY_CODES = "currency_codes";
    private BaseCurrencyEditFinishedListener mListener;
    private String mNewBaseCurrencyCode;

    public static BaseCurrencyListDialogFragment newInstance(ArrayList<String> currencyCodeList) {
        final BaseCurrencyListDialogFragment fragment = new BaseCurrencyListDialogFragment();
        final Bundle args = new Bundle();
        args.putStringArrayList(ARG_CURRENCY_CODES, currencyCodeList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        mListener.onFinishEditingBaseCurrency(mNewBaseCurrencyCode);
        super.onDismiss(dialog);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (BaseCurrencyEditFinishedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement BaseCurrencyEditFinishedListener");
        }
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_base_currency_list_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        final RecyclerView recyclerView = view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        assert getArguments() != null;
        ArrayList<String> currencyCodeList = getArguments().getStringArrayList(ARG_CURRENCY_CODES);

        if (currencyCodeList != null) {
            int itemCount = currencyCodeList.size();
            recyclerView.setAdapter(new BaseCurrencyListDialogFragment.CurrencyListAdapter(itemCount, currencyCodeList));
        } else dismiss();
    }

    public interface BaseCurrencyEditFinishedListener {
        void onFinishEditingBaseCurrency(String newBaseCurrencyCode);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_layout)
        ConstraintLayout itemLayout;

        @BindView(R.id.country_flag_imageview)
        ImageView countryFlagImageView;

        @BindView(R.id.currency_code_textview)
        TextView currencyCodeTextView;

        @BindView(R.id.currency_radiobutton)
        RadioButton currencyRadioButton;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    private class CurrencyListAdapter extends RecyclerView.Adapter<BaseCurrencyListDialogFragment.ViewHolder> {

        private final int mItemCount;
        private List<String> mCurrencyCodeList;
        private ArrayList<CurrencyState> mStateList;


        CurrencyListAdapter(int itemCount, List<String> currencyCodeList) {
            mItemCount = itemCount;
            mCurrencyCodeList = currencyCodeList;
            mStateList = new ArrayList<>();
            SettingsRepository.Settings settings = SettingsRepository.getInstance().getSettings().getValue();
            if (settings != null) {
                mNewBaseCurrencyCode = settings.getBaseCurrencyCode();
            }
            for (String currencyCode : mCurrencyCodeList) {
                mStateList.add(new CurrencyState(currencyCode, currencyCode.equals(mNewBaseCurrencyCode)));
            }
        }

        @Override
        public BaseCurrencyListDialogFragment.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new BaseCurrencyListDialogFragment.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_base_currency_list_dialog_item, parent, false));
        }

        @Override
        public void onBindViewHolder(BaseCurrencyListDialogFragment.ViewHolder holder, int position) {
            String currencyCode = mCurrencyCodeList.get(position);
            holder.countryFlagImageView.setImageDrawable(null);
            Picasso.get().load(String.format(COUNTRY_FLAG_API_FORMAT, CURRENCY_CODE_TO_COUNTRY_CODE_MAP.get(currencyCode)))
                    .into(holder.countryFlagImageView);
            holder.currencyCodeTextView.setText(currencyCode);
            holder.currencyRadioButton.setChecked(mStateList.get(position).isCurrentBase);

            holder.itemLayout.setOnClickListener(v -> {
                holder.currencyRadioButton.setChecked(true);
                mNewBaseCurrencyCode = currencyCode;
                // set others false
                for (int i = 0; i < mStateList.size(); i++) {
                    CurrencyState currencyState = mStateList.get(i);
                    if (currencyState.isCurrentBase != currencyState.currencyCode.equals(currencyCode)) {
                        currencyState.isCurrentBase = currencyState.currencyCode.equals(currencyCode);
                        notifyItemChanged(i);
                    }
                }

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(getString(R.string.pref_key_base_currency), mNewBaseCurrencyCode);
                editor.apply();
            });
        }

        @Override
        public int getItemCount() {
            return mItemCount;
        }

    }

    private class CurrencyState {
        Boolean isCurrentBase;
        String currencyCode;

        public CurrencyState(String currencyCode, Boolean state) {
            this.currencyCode = currencyCode;
            isCurrentBase = state;
        }
    }

}
