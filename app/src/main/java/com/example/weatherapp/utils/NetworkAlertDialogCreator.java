package com.example.weatherapp.utils;


import android.content.Context;

import androidx.appcompat.app.AlertDialog;

public class NetworkAlertDialogCreator {

    public static AlertDialog.Builder createNetworkAlertDialog(Context context) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle("Weather App")
                .setMessage("No internet connection, please check your connection")
                .setPositiveButton("Ok", (dialog, which) -> {
                    dialog.dismiss();
                });

        return builder;
    }
}
