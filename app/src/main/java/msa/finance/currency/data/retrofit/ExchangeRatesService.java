package msa.finance.currency.data.retrofit;

import msa.finance.currency.data.retrofit.historical.HistoricalRatesResponse;
import msa.finance.currency.data.retrofit.latest.LatestExchangeRatesResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ExchangeRatesService {
    @GET("/latest")
    Call<LatestExchangeRatesResponse> getLatestExchangeRates(@Query("base") String base);

    @GET("/history")
    Call<HistoricalRatesResponse> getHistoricalRates(@Query("base") String base, @Query("start_at") String startAt, @Query("end_at") String endAt);
}
