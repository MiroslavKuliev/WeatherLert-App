package com.example.weatherlertapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 100;

    private TextView tvCurrentDay, tvLocation, tvWeatherCondition, tvTemperature;

    private FusedLocationProviderClient fusedLocationClient;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvCurrentDay = findViewById(R.id.tvCurrentDay);
        tvLocation = findViewById(R.id.tvLocation);
        tvWeatherCondition = findViewById(R.id.tvWeatherCondition);
        tvTemperature = findViewById(R.id.tvTemperature);
        ImageView menuIcon = findViewById(R.id.menuIcon);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showPopupMenu(v);
            }
        });

        setCurrentDay();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
        } else {
            setLocationAndWeather();
        }
        createNotificationChannel();
    }

    private void showPopupMenu(View v) {
        PopupMenu popup = new PopupMenu(MainActivity.this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_main, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.action_weekly_forecast) {
                    return true;
                } else if (id == R.id.action_map) {
                    return true;
                } else if (id == R.id.action_settings) {
                    Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });
        popup.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
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
    @Override
    protected void onResume() {
        super.onResume();
        setLocationAndWeather();
    }
    private void fetchWeather(double latitude, double longitude) {
        WeatherDataFetch.isWeatherBad(latitude, longitude, new WeatherDataFetch.WeatherCheckCallback() {

            @Override
            public void onChecked(boolean isBad, String weatherStatus, double temperature) {
                boolean isFahrenheit = sharedPreferences.getBoolean("temperature_unit", false);

                if (isFahrenheit) {
                    temperature = (temperature * 9/5) + 32;
                    tvTemperature.setText(String.format(Locale.getDefault(), "%.1f°F", temperature));
                } else {
                    tvTemperature.setText(String.format(Locale.getDefault(), "%.1f°C", temperature));
                }

                tvWeatherCondition.setText(weatherStatus);

                if (isBad) {
                    tvWeatherCondition.setTextColor(Color.RED);
                    showWeatherNotification("Bad Weather Alert: " + weatherStatus);
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

    @SuppressLint("MissingPermission")
    private void showWeatherNotification(String weatherStatus) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        if (!notificationManager.areNotificationsEnabled()) {
            Toast.makeText(this, "Please enable notifications for this app in settings.", Toast.LENGTH_LONG).show();
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "WEATHER_ALERT_CHANNEL")
                .setSmallIcon(R.drawable.img)
                .setContentTitle("Weather Alert!")
                .setContentText(weatherStatus)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(100, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Weather Alert Channel";
            String description = "Channel for Weather Alerts";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("WEATHER_ALERT_CHANNEL", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
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
