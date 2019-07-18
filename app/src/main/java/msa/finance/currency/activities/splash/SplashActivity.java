package msa.finance.currency.activities.splash;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import msa.finance.currency.R;
import msa.finance.currency.activities.main.MainActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkConnectionToAPI();
    }

    private void checkConnectionToAPI() {
        Context context = this;
        ViewModelProviders.of(this).get(SplashViewModel.class).getAPIAvailability().observe(this, available -> {
            Toast infoToast = Toast.makeText(this, R.string.error_api_not_available, Toast.LENGTH_LONG);
            if (available != null) {
                if (!available) {
                    infoToast.show();
                }
            }
            new Handler().postDelayed(() -> {
                infoToast.cancel();
                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("api_availability", available);
                startActivity(intent);
                finish();
            }, 1000);
        });
    }
}
