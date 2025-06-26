package com.example.weatherapp.view;

import android.annotation.SuppressLint;

import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.weatherapp.R;
import com.example.weatherapp.databinding.ActivityWeatherBinding;
import com.example.weatherapp.model.WeatherModel;
import com.example.weatherapp.utils.Constants;
import com.example.weatherapp.utils.NetworkAlertDialogCreator;
import com.example.weatherapp.utils.networkutil.NetworkConnectionObserver;
import com.example.weatherapp.utils.networkutil.NetworkStatusListener;
import com.example.weatherapp.viewmodel.WeatherViewModel;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class WeatherActivity extends AppCompatActivity implements NetworkStatusListener {

    ActivityWeatherBinding weatherBinding;
    String prefer;
    WeatherViewModel weatherViewModel;

    LocationManager locationManager;
    LocationListener locationListener;
    double lat;
    double lon;

    AlertDialog dialog;
    NetworkConnectionObserver networkConnectionObserver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        weatherBinding = ActivityWeatherBinding.inflate(getLayoutInflater());
        setContentView(weatherBinding.getRoot());

        weatherViewModel = new ViewModelProvider(this).get(WeatherViewModel.class);

        weatherBinding.linearLayoutWeatherData.setVisibility(View.INVISIBLE);
        prefer = getIntent().getStringExtra(Constants.intentName);

        if (prefer != null) {
            if (prefer.equals(Constants.byLocation)) {
                getWeatherDataByLocation();
            } else {
                weatherBinding.progressBarWeatherData.setVisibility(View.INVISIBLE);
            }
        }

        weatherBinding.toolbar.setNavigationOnClickListener(v -> {
            finish();
        });

        weatherBinding.search.setOnClickListener(v -> {

            getWeatherDataByCityName();

        });

        weatherBinding.refreshLayout.setOnRefreshListener(() -> {

            if (prefer.equals(Constants.byLocation)) {
                getWeatherDataByLocation();
            } else {
                getWeatherDataByCityName();
            }

            weatherBinding.refreshLayout.setRefreshing(false);

        });

        dialog = NetworkAlertDialogCreator.createNetworkAlertDialog(this).create();
        networkConnectionObserver = new NetworkConnectionObserver(this, this);
        weatherViewModel.setNetworkConnectionObserver(networkConnectionObserver);

    }

    private void getWeatherDataByCityName() {

        String cityName = weatherBinding.editTextCityName.getText().toString();

        if (cityName.isEmpty()) {
            Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT).show();
        } else {

            weatherViewModel.getProgressBarLiveData().observe(WeatherActivity.this, progressState -> {

                if (progressState) {
                    weatherBinding.progressBarWeatherData.setVisibility(View.VISIBLE);
                    weatherBinding.linearLayoutWeatherData.setVisibility(View.INVISIBLE);
                } else {
                    weatherBinding.progressBarWeatherData.setVisibility(View.INVISIBLE);

                }

            });

            weatherViewModel.sendRequestByCityName(getApplicationContext(), cityName);
            weatherViewModel.getWeatherResponseLiveData().observe(WeatherActivity.this, this::showWeatherData);


        }

    }


    @SuppressLint("MissingPermission")
    public void getWeatherDataByLocation() {
        weatherBinding.linearLayoutSearch.setVisibility(View.INVISIBLE);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationListener = location -> {

            lat = location.getLatitude();
            lon = location.getLongitude();

            Log.d("userslatitude:", String.valueOf(lat));
            Log.d("userslongitude:", String.valueOf(lon));

            weatherViewModel.getProgressBarLiveData().observe(WeatherActivity.this, progressState -> {

                if (progressState) {
                    weatherBinding.progressBarWeatherData.setVisibility(View.VISIBLE);
                    weatherBinding.linearLayoutWeatherData.setVisibility(View.INVISIBLE);
                } else {
                    weatherBinding.progressBarWeatherData.setVisibility(View.INVISIBLE);

                }

            });

            // call the sendRequestByLocation method of the WeatherViewModel
            weatherViewModel.sendRequestByLocation(getApplicationContext(), lat, lon);
            // show data on UI
            weatherViewModel.getWeatherResponseLiveData().observe(WeatherActivity.this, this::showWeatherData);


        };

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 50, locationListener);

    }

    public void showWeatherData(WeatherModel response) {

        weatherBinding.textViewCityName.setText(response.getName() + " , " + response.getSys().getCountry());
        weatherBinding.textViewTemperature.setText(response.getMain().getTemp() + " °C");
        weatherBinding.textViewDescription.setText(response.getWeather().get(0).getDescription());
        weatherBinding.textViewHumidity.setText(" : " + response.getMain().getHumidity() + "%");
        weatherBinding.textViewMaxTemp.setText(" : " + response.getMain().getTemp_max() + " °C");
        weatherBinding.textViewMinTemp.setText(" : " + response.getMain().getTemp_max() + " °C");
        weatherBinding.textViewPressure.setText(" : " + response.getMain().getPressure());
        weatherBinding.textViewWind.setText(" : " + response.getWind().getSpeed());

        weatherBinding.linearLayoutWeatherData.setVisibility(View.VISIBLE);
        weatherBinding.progressBarWeatherIcon.setVisibility(View.VISIBLE);

        // https://openweathermap.org/img/wn/03d@2x.png
        String iconCode = response.getWeather().get(0).getIcon();
        Picasso.get().load("https://openweathermap.org/img/wn/" + iconCode + "@2x.png")
                .into(weatherBinding.imageViewWeatherIcon, new Callback() {
                    @Override
                    public void onSuccess() {
                        weatherBinding.progressBarWeatherIcon.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onError(Exception e) {

                        weatherBinding.imageViewWeatherIcon.setImageResource(R.drawable.partly_cloudy_day);
                        Log.d("iconerrror", e.getLocalizedMessage());
                        weatherBinding.progressBarWeatherIcon.setVisibility(View.INVISIBLE);
                    }
                });


    }

    @Override
    public void onNetworkAvailable() {


        this.runOnUiThread(() -> {

            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }

        });


    }

    @Override
    public void onNetworkLost() {

        this.runOnUiThread(() -> {

            if (dialog != null && !dialog.isShowing()) {
                dialog.show();
            }

        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        networkConnectionObserver.registerCallback();
        networkConnectionObserver.checkNetworkConnection();
    }

    @Override
    protected void onPause() {
        super.onPause();
        networkConnectionObserver.unregisterCallback();
    }

}










