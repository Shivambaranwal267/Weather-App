package com.example.weatherapp.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.weatherapp.databinding.ActivityMainBinding;
import com.example.weatherapp.databinding.BottomSheetDialogBinding;
import com.example.weatherapp.utils.Constants;
import com.example.weatherapp.utils.NetworkAlertDialogCreator;
import com.example.weatherapp.utils.networkutil.NetworkConnectionObserver;
import com.example.weatherapp.utils.networkutil.NetworkStatusListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class MainActivity extends AppCompatActivity implements NetworkStatusListener {

    ActivityMainBinding mainBinding;
    ActivityResultLauncher<String[]> permissionResultLauncher;

    BottomSheetDialogBinding bottomSheetDialogBinding;
    int deniedALLPermissionsCount;
    int deniedOnlyFinePermissionsCount;
    SharedPreferences sharedPreferences;

    AlertDialog dialog;
    NetworkConnectionObserver networkConnectionObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());

        sharedPreferences = this.getSharedPreferences(Constants.nameOfSharedPreferences, Context.MODE_PRIVATE);
        deniedALLPermissionsCount = sharedPreferences.getInt(Constants.keyForDeniedAllPermissionCount, 0);
        deniedOnlyFinePermissionsCount = sharedPreferences.getInt(Constants.keyForDeniedOnlyPermissionCount, 0);


        // register
        registerForPermission();

        mainBinding.buttonWeatherByCityName.setOnClickListener(v -> {
            // open the second activity
            openWeatherActivity(Constants.byCityName);
        });

        mainBinding.buttonWeatherByLocation.setOnClickListener(v -> {
            // ask for permission, open the second activity
            if (hasFineLocationPermission()) {
                // check location, get weather data
                checkLocationSetting();
            } else if (hasCoarseLocationPermission()) {

                saveDeniedOnlyFinePermissionCount();
                if (deniedOnlyFinePermissionsCount > 2) {
                    checkLocationSetting();
                } else {
                    // bottom sheet dialog for precise location
                    showBottomSheetDialog("Give precise location permisison for better results.",
                            "fine", "permission");
                }
            } else {
                // launch the permissionResultLauncher to request permission dialog
                permissionResultLauncher.launch(new String[]{Constants.FINE_LOCATION, Constants.COARSE_LOCATION});
            }
        });

        dialog = NetworkAlertDialogCreator.createNetworkAlertDialog(this).create();
        networkConnectionObserver = new NetworkConnectionObserver(this, this);

    }

    public void registerForPermission() {
        permissionResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            Boolean isFineLocationGranted = result.getOrDefault(Constants.FINE_LOCATION, false);
            Boolean isCoarseLocationGranted = result.getOrDefault(Constants.COARSE_LOCATION, false);

            if (isFineLocationGranted) {
                // check location, get weather data
                checkLocationSetting();
            } else if (isCoarseLocationGranted) {
                saveDeniedOnlyFinePermissionCount();
                // bottom sheet dialog for precise location
                showBottomSheetDialog("Give precise location permisison for better results.",
                        "fine", "permission");
            } else {

                deniedALLPermissionsCount++;
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(Constants.keyForDeniedAllPermissionCount, 0);
                editor.apply();

                // bottom sheet dialog for permissions
                showBottomSheetDialog("To get the weather By location, you need to enable location permission",
                        "all", "permission");
            }

        });
    }

    private boolean hasFineLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Constants.FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasCoarseLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Constants.COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public void showBottomSheetDialog(String message, String deniedPermission, String useFor) {

        bottomSheetDialogBinding = BottomSheetDialogBinding.inflate(getLayoutInflater());
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this);
        bottomSheetDialog.setContentView(bottomSheetDialogBinding.getRoot());

        if (useFor.equals("location")) {
            bottomSheetDialogBinding.buttonAllow.setText("Go");
            bottomSheetDialogBinding.textViewTitle.setText("Location");
        } else {
            if (deniedALLPermissionsCount > 2 || deniedOnlyFinePermissionsCount > 2) {
                bottomSheetDialogBinding.buttonAllow.setText("Open");
                bottomSheetDialogBinding.textViewMessage.setText("Open the app settings to give the precise location permission.");
            } else {
                bottomSheetDialogBinding.textViewMessage.setText(message);
            }
        }

        bottomSheetDialogBinding.buttonAllow.setOnClickListener(v -> {

            if (useFor.equals("location")) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            } else {

                if (deniedALLPermissionsCount > 2 || deniedOnlyFinePermissionsCount > 2) {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null); //package com.example.weatherapp
                    intent.setData(uri);
                    startActivity(intent);
                } else {
                    // launch the permissionResultLauncher to request permission dialog
                    permissionResultLauncher.launch(new String[]{Constants.FINE_LOCATION, Constants.COARSE_LOCATION});
                }
            }

            bottomSheetDialog.dismiss();
        });

        bottomSheetDialogBinding.buttonDeny.setOnClickListener(v -> {
            if (deniedPermission.equals("fine")) {
                checkLocationSetting();
            }
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();

    }

    public void checkLocationSetting() {

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            // open second activity
            openWeatherActivity(Constants.byLocation);
            //Toast.makeText(this, "Second Activity", Toast.LENGTH_SHORT).show();

        } else {
            showBottomSheetDialog("Go to location settings to turn on the locaton",
                    null,
                    "location");
        }

    }

    public void saveDeniedOnlyFinePermissionCount() {

        deniedOnlyFinePermissionsCount++;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Constants.keyForDeniedOnlyPermissionCount, 0);
        editor.apply();

    }

    public void openWeatherActivity(String prefer) {

        Intent intent = new Intent(MainActivity.this, WeatherActivity.class);
        intent.putExtra(Constants.intentName, prefer);
        startActivity(intent);

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