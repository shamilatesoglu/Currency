package msa.finance.currency.adapters;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import butterknife.BindView;
import butterknife.ButterKnife;
import msa.finance.currency.R;
import msa.finance.currency.data.Rate;
import msa.finance.currency.data.repository.ExchangeRatesRepository;
import msa.finance.currency.util.Utilities;

import static msa.finance.currency.util.Constants.COUNTRY_FLAG_API_FORMAT;
import static msa.finance.currency.util.Constants.CURRENCY_CODE_TO_COUNTRY_CODE_MAP;


public class CurrencyRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;

    private String mBaseCurrencyCode;

    private List<Rate> mRateList;

    public CurrencyRecyclerViewAdapter(Context context) {
        mContext = context;
        mRateList = new ArrayList<>();
    }

    public void setBaseCurrencyCode(String baseCurrencyCode) {
        mBaseCurrencyCode = baseCurrencyCode;
    }

    public void checkIfDataSetChanged(List<Rate> newRateList) {
        List<Rate> oldRateList = new ArrayList<>(mRateList);
        mRateList = newRateList;
        if (mRateList.size() == 0){
            notifyDataSetChanged();
        }
        for (int i = 0; i < ((oldRateList.size() > mRateList.size()) ? oldRateList.size() : mRateList.size()); i++) {
            if (oldRateList.size() < mRateList.size()) {
                if (i < oldRateList.size()) {
                    if (!oldRateList.get(i).equals(mRateList.get(i)))
                        notifyItemChanged(i);
                } else notifyItemInserted(i);
            } else {
                if (i < mRateList.size()) {
                    if (!mRateList.get(i).equals(oldRateList.get(i)))
                        notifyItemChanged(i);
                } else notifyItemRemoved(i);
            }
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CurrencyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_currency, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (mRateList.size() > 0 && mRateList.get(position).isValid()) {
            ((CurrencyViewHolder) viewHolder).baseCurrencyCodeTextView.setText(String.format("1 %s", mBaseCurrencyCode));
            ((CurrencyViewHolder) viewHolder).valueTextView.setText(String.format(Locale.getDefault(), "%s %s",
                    Utilities.round(mRateList.get(position).getValue(),
                            PreferenceManager.getDefaultSharedPreferences(mContext).getInt(mContext.getString(R.string.pref_key_precision), 5)),
                    mRateList.get(position).getCurrencyCode()));

            // Country flags.
            Picasso.get().load(String.format(COUNTRY_FLAG_API_FORMAT,
                    CURRENCY_CODE_TO_COUNTRY_CODE_MAP.get(mBaseCurrencyCode)))
                    .into(((CurrencyViewHolder) viewHolder).sourceCurrencyFlagImageView);
            Picasso.get().load(String.format(COUNTRY_FLAG_API_FORMAT,
                    CURRENCY_CODE_TO_COUNTRY_CODE_MAP.get(mRateList.get(position).getCurrencyCode())))
                    .into(((CurrencyViewHolder) viewHolder).targetCurrencyFlagImageView);

            ((CurrencyViewHolder) viewHolder).valueTextView.setSelected(true);
            ((CurrencyViewHolder) viewHolder).baseCurrencyCodeTextView.setSelected(true);
        }
    }

    @Override
    public int getItemCount() {
        return mRateList.size();
    }

    public class CurrencyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.base_currency_code)
        TextView baseCurrencyCodeTextView;

        @BindView(R.id.value)
        TextView valueTextView;

        @BindView(R.id.source_currency_flag_imageview)
        ImageView sourceCurrencyFlagImageView;

        @BindView(R.id.target_currency_flag_imageview)
        ImageView targetCurrencyFlagImageView;

        public CurrencyViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
