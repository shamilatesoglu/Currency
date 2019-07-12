package msa.finance.currency.adapters;

import android.content.Context;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import msa.finance.currency.R;
import msa.finance.currency.data.Rate;

import static msa.finance.currency.util.Constants.COUNTRY_FLAG_API_FORMAT;
import static msa.finance.currency.util.Constants.CURRENCY_CODE_TO_COUNTRY_CODE_MAP;
import static msa.finance.currency.util.Constants.TR;


public class CurrencyRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;

    private String mBaseRateCode;

    private List<Rate> mRateList;

    public CurrencyRecyclerViewAdapter(Context context) {
        mContext = context;
        mRateList = new ArrayList<>();
    }

    public void setRateList(List<Rate> rateList) {
        mRateList = rateList;
    }

    public void setBaseRateCode(String baseRateCode) {
        mBaseRateCode = baseRateCode;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CurrencyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_currency, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (mRateList.size() > 0) {
            ((CurrencyViewHolder) viewHolder).baseCurrencyCodeTextView.setText(String.format("1 %s", mBaseRateCode));
            ((CurrencyViewHolder) viewHolder).valueTextView.setText(String.format(Locale.getDefault(), "%f %s",
                    mRateList.get(position).getValue(), mRateList.get(position).getCurrencyCode()));
            Picasso.get().load(String.format(COUNTRY_FLAG_API_FORMAT, CURRENCY_CODE_TO_COUNTRY_CODE_MAP.get(mBaseRateCode)))
                    .into(((CurrencyViewHolder) viewHolder).sourceCurrencyFlagImageView);
            Picasso.get().load(String.format(COUNTRY_FLAG_API_FORMAT, CURRENCY_CODE_TO_COUNTRY_CODE_MAP.get(mRateList.get(position).getCurrencyCode())))
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
