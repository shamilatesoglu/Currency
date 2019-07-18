package msa.finance.currency.data.retrofit;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ExchangeRatesService {
    @GET("/latest")
    Call<ExchangeRatesAPIResponse> getLatestExchangeRates(@Query("base") String base);
}
