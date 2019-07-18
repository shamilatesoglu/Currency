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
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import msa.finance.currency.R;

import static msa.finance.currency.util.Constants.COUNTRY_FLAG_API_FORMAT;
import static msa.finance.currency.util.Constants.CURRENCY_CODE_TO_COUNTRY_CODE_MAP;

/**
 * <p>A fragment that shows a list of items as a modal bottom sheet.</p>
 * <p>You can show this modal bottom sheet from your activity like this:</p>
 * <pre>
 *     CurrencyListDialogFragment.newInstance(30).show(getSupportFragmentManager(), "dialog");
 * </pre>
 * <p>You activity (or fragment) needs to implement {@link CurrencyListDialogFragment}.</p>
 * <p>
 * TODO: Add onDismissListener
 */
public class CurrencyListDialogFragment extends BottomSheetDialogFragment {

    private static final String ARG_CURRENCY_CODES = "currency_codes";

    public static CurrencyListDialogFragment newInstance(ArrayList<String> currencyCodeList) {
        final CurrencyListDialogFragment fragment = new CurrencyListDialogFragment();
        final Bundle args = new Bundle();
        args.putStringArrayList(ARG_CURRENCY_CODES, currencyCodeList);
        fragment.setArguments(args);
        return fragment;
    }

    private CurrencyListEditFinishedListener mListener;

    private List<String> mNewCurrenciesToShowList = new ArrayList<>();

    public CurrencyListDialogFragment setInitialCurrenciesToShowList(List<String> initialCurrenciesToShowList) {
        mNewCurrenciesToShowList = new ArrayList<>(initialCurrenciesToShowList);
        return this;
    }

    public interface CurrencyListEditFinishedListener {
        void onFinishEditingCurrencyList(List<String> newCurrenciesToShowList);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        mListener.onFinishEditingCurrencyList(mNewCurrenciesToShowList);
        super.onDismiss(dialog);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_currency_list_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        final RecyclerView recyclerView = view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        assert getArguments() != null;
        ArrayList<String> currencyCodeList = getArguments().getStringArrayList(ARG_CURRENCY_CODES);

        if (currencyCodeList != null) {
            int itemCount = currencyCodeList.size();
            recyclerView.setAdapter(new CurrencyListAdapter(itemCount, currencyCodeList));
        } else dismiss();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (CurrencyListEditFinishedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement CurrencyListEditFinishedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
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

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_layout)
        ConstraintLayout itemLayout;

        @BindView(R.id.country_flag_imageview)
        ImageView countryFlagImageView;

        @BindView(R.id.currency_code_textview)
        TextView currencyCodeTextView;

        @BindView(R.id.currency_checkbox)
        CheckBox currencyCheckBox;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    private class CurrencyListAdapter extends RecyclerView.Adapter<ViewHolder> {

        private final int mItemCount;
        private List<String> mCurrencyCodeList;

        CurrencyListAdapter(int itemCount, List<String> currencyCodeList) {
            mItemCount = itemCount;
            mCurrencyCodeList = currencyCodeList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_currency_list_dialog_item, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String currencyCode = mCurrencyCodeList.get(position);
            holder.countryFlagImageView.setImageDrawable(null);
            Picasso.get().load(String.format(COUNTRY_FLAG_API_FORMAT, CURRENCY_CODE_TO_COUNTRY_CODE_MAP.get(currencyCode)))
                    .into(holder.countryFlagImageView);
            holder.currencyCodeTextView.setText(currencyCode);
            holder.currencyCheckBox.setChecked(mNewCurrenciesToShowList.contains(currencyCode));

            holder.itemLayout.setOnClickListener(v -> {
                holder.currencyCheckBox.toggle();
                if (holder.currencyCheckBox.isChecked()) {
                    if (!mNewCurrenciesToShowList.contains(currencyCode))
                        mNewCurrenciesToShowList.add(currencyCode);
                } else mNewCurrenciesToShowList.remove(currencyCode);

                StringBuilder currencies = new StringBuilder();
                for (String c : mNewCurrenciesToShowList) {
                    currencies.append(c).append(",");
                }

                if (currencies.length() > 0)
                    currencies = currencies.deleteCharAt(currencies.length() - 1);

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(getString(R.string.pref_key_currencies_to_show), currencies.toString());
                editor.apply();

            });
        }

        @Override
        public int getItemCount() {
            return mItemCount;
        }

    }

}
