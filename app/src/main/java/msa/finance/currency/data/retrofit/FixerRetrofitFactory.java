package msa.finance.currency.data.retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FixerRetrofitFactory {
    public static final String BASE_URL = "http://data.fixer.io/";
    public static Retrofit retrofit = null;

    private FixerRetrofitFactory() {
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