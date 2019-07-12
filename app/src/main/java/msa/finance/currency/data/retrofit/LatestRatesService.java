package msa.finance.currency.data.retrofit;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface LatestRatesService {
    @GET("/api/latest")
    Call<LatestRatesResponse> getCurrencyRates(@Query("access_key") String apiKey, @Query("format") int format);
}
