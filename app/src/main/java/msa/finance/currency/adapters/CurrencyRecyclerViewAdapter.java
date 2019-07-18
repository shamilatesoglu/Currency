package msa.finance.currency.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.squareup.picasso.Picasso;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import msa.finance.currency.R;
import msa.finance.currency.activities.main.MainActivity;
import msa.finance.currency.data.Rate;
import msa.finance.currency.data.repository.SettingsRepository;
import msa.finance.currency.util.Utilities;

import static msa.finance.currency.util.Constants.COUNTRY_FLAG_API_FORMAT;
import static msa.finance.currency.util.Constants.CURRENCY_CODE_TO_COUNTRY_CODE_MAP;


public class CurrencyRecyclerViewAdapter extends RecyclerView.Adapter<CurrencyRecyclerViewAdapter.CurrencyViewHolder> {

    private MainActivity mMainActivity;

    private String mBaseCurrencyCode;

    private List<Rate> mRateList;
    private Map<String, Map<String, Double>> mCurrencyToHistoricalRatesMap;

    private List<String> mExpandedItemCurrencyCodeList;

    public CurrencyRecyclerViewAdapter(MainActivity mainActivity) {
        mMainActivity = mainActivity;
        mRateList = new ArrayList<>();
        mExpandedItemCurrencyCodeList = new ArrayList<>();
    }

    public void setHistoricalRatesMap(Map<String, Map<String, Double>> currencyToHistoricalRatesMap) {
        mCurrencyToHistoricalRatesMap = currencyToHistoricalRatesMap;
    }

    public void setBaseCurrencyCode(String baseCurrencyCode) {
        mBaseCurrencyCode = baseCurrencyCode;
    }

    public void checkIfDataSetChanged(List<Rate> newRateList) {
        List<Rate> oldRateList = new ArrayList<>(mRateList);
        mRateList = newRateList;
        if (mRateList.size() == 0) {
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
    public CurrencyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CurrencyViewHolder(LayoutInflater.from(mMainActivity).inflate(R.layout.item_currency, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CurrencyViewHolder viewHolder, int position) {
        if (mRateList.size() > 0 && mRateList.get(position).isValid()) {
            viewHolder.currency = mRateList.get(position).getCurrencyCode();
            viewHolder.baseCurrencyCodeTextView.setText(String.format("1 %s", mBaseCurrencyCode));
            viewHolder.valueTextView.setText(String.format(Locale.getDefault(), "%s %s",
                    Utilities.round(mRateList.get(position).getValue(),
                            SettingsRepository.getInstance().getSettings().getValue().getPrecision()),
                    mRateList.get(position).getCurrencyCode()));

            // Country flags
            Picasso.get().load(String.format(COUNTRY_FLAG_API_FORMAT,
                    CURRENCY_CODE_TO_COUNTRY_CODE_MAP.get(mBaseCurrencyCode)))
                    .into(viewHolder.sourceCurrencyFlagImageView);
            Picasso.get().load(String.format(COUNTRY_FLAG_API_FORMAT,
                    CURRENCY_CODE_TO_COUNTRY_CODE_MAP.get(mRateList.get(position).getCurrencyCode())))
                    .into(viewHolder.targetCurrencyFlagImageView);

            viewHolder.valueTextView.setSelected(true);
            viewHolder.baseCurrencyCodeTextView.setSelected(true);

            viewHolder.graphExpandableLayout.setExpanded(mExpandedItemCurrencyCodeList.contains(mRateList.get(position).getCurrencyCode()));

            configureLineChart(viewHolder, mRateList.get(position).getCurrencyCode());
        }
    }

    private void configureLineChart(CurrencyViewHolder holder, String targetCurrencyCode) {
        try {
            List<Entry> rateEntries = new ArrayList<>();

            List<String> dateStringList = new ArrayList<>(mCurrencyToHistoricalRatesMap.get(targetCurrencyCode).keySet());

            Collections.sort(dateStringList, (o1, o2) -> {
                try {
                    Date date1 = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(o1);
                    Date date2 = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(o2);
                    return date1.compareTo(date2);

                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return 0;
            });

            int i = 0;
            for (String dateStr : dateStringList) {
                rateEntries.add(new Entry(new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateStr).getTime(),
                        mCurrencyToHistoricalRatesMap.get(targetCurrencyCode).get(dateStr).floatValue()));
            }

            LineDataSet rateLineDataSet = new LineDataSet(rateEntries, targetCurrencyCode);
            rateLineDataSet.setColor(mMainActivity.getResources().getColor(R.color.colorPrimary));
            rateLineDataSet.setFillColor(mMainActivity.getResources().getColor(R.color.colorPrimary));
            rateLineDataSet.setFillAlpha(25);
            rateLineDataSet.setDrawValues(false);
            rateLineDataSet.setDrawFilled(true);
            LineData rateLineData = new LineData(rateLineDataSet);
            rateLineData.setValueTextColor(mMainActivity.getResources().getColor(R.color.colorPrimary));
            holder.historicalRatesLineChart.setData(rateLineData);
            rateLineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            rateLineDataSet.setCircleColor(mMainActivity.getResources().getColor(R.color.colorPrimary));
            rateLineDataSet.setCircleHoleColor(mMainActivity.getResources().getColor(R.color.colorAccent));
            holder.historicalRatesLineChart.getDescription().setText(String.format("%s to %s", mBaseCurrencyCode, targetCurrencyCode));
        } catch (Exception e) {
            e.printStackTrace();
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

        @BindView(R.id.graph_expandablelayout)
        ExpandableLayout graphExpandableLayout;
        @BindView(R.id.historical_rates_linechart)
        LineChart historicalRatesLineChart;

        String currency;

        public CurrencyViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            configureLineChart();
        }

        @OnClick(R.id.currency_container_framelayout)
        public void toggleExpandableLayout() {
            graphExpandableLayout.toggle();
            graphExpandableLayout.setOnExpansionUpdateListener((expansionFraction, state) -> {
                switch (state) {
                    case ExpandableLayout.State.COLLAPSING:
                        mExpandedItemCurrencyCodeList.remove(currency);
                        break;
                    case ExpandableLayout.State.EXPANDING:
                        mExpandedItemCurrencyCodeList.add(currency);
                        break;
                }
            });
        }

        private void configureLineChart() {
            historicalRatesLineChart.getXAxis().setTextColor(mMainActivity.getResources().getColor(R.color.colorPrimary));
            historicalRatesLineChart.getAxisLeft().setEnabled(false);
            historicalRatesLineChart.getAxisRight().setTextColor(mMainActivity.getResources().getColor(R.color.colorPrimaryDark));
            Description description = new Description();
            description.setYOffset(-10);
            description.setTextColor(mMainActivity.getResources().getColor(R.color.colorPrimaryDark));
            historicalRatesLineChart.setDescription(description);
            historicalRatesLineChart.getLegend().setTextColor(mMainActivity.getResources().getColor(R.color.colorPrimaryDark));
            historicalRatesLineChart.getXAxis().setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return new SimpleDateFormat("dd MMM", Locale.US).format(new Date(Float.valueOf(value).longValue()));
                }
            });
            historicalRatesLineChart.setMarker(new CustomMarkerView(mMainActivity, R.layout.marker_view));
        }


    }

    private static class CustomMarkerView extends MarkerView {

        private TextView markerTextView;

        public CustomMarkerView(Context context, int layoutResource) {
            super(context, layoutResource);
            markerTextView = findViewById(R.id.marker_text);
        }

        // callbacks everytime the MarkerView is redrawn, can be used to update the
        // content (user-interface)
        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            markerTextView.setText(String.format(Locale.getDefault(), "%.10f", e.getY())); // set the entry-value as the display text
        }

    }
}