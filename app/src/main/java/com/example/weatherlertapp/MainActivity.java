package com.example.weatherlertapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 100;

    private TextView tvCurrentDay, tvLocation, tvWeatherCondition;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvCurrentDay = findViewById(R.id.tvCurrentDay);
        tvLocation = findViewById(R.id.tvLocation);
        tvWeatherCondition = findViewById(R.id.tvWeatherCondition);

        setCurrentDay();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
        } else {
            setLocationAndWeather();
        }
    }

    private void setCurrentDay() {
        Calendar calendar = Calendar.getInstance();
        String dayName = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
        tvCurrentDay.setText(dayName);
    }

    private void setLocationAndWeather() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permissions are required to fetch weather details.", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                fetchAddressFromLocation(location);
                fetchWeather(location.getLatitude(), location.getLongitude());
            }
        });
    }

    private void fetchWeather(double latitude, double longitude) {
        WeatherDataFetch.isWeatherBad(latitude, longitude, new WeatherDataFetch.WeatherCheckCallback() {
            @Override
            public void onChecked(boolean isBad, String weatherStatus) {
                tvWeatherCondition.setText(weatherStatus);
                if (isBad) {
                    tvWeatherCondition.setTextColor(Color.RED);
                } else {
                    tvWeatherCondition.setTextColor(Color.BLACK);
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(MainActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchAddressFromLocation(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (!addresses.isEmpty()) {
                String cityName = addresses.get(0).getLocality();
                String stateName = addresses.get(0).getAdminArea();
                tvLocation.setText(cityName + ", " + stateName);
            } else {
                Toast.makeText(this, "Unable to fetch location details", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Error fetching location details", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setLocationAndWeather();
            }
        }
    }
}
