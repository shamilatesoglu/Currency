package msa.finance.currency.data.retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ExchangeRatesAPIRetrofitFactory {
    public static final String BASE_URL = "https://api.exchangeratesapi.io/";
    public static Retrofit retrofit = null;

    private ExchangeRatesAPIRetrofitFactory() {
    }

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
