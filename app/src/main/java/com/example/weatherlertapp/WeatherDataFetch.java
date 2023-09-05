package com.example.weatherlertapp;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WeatherDataFetch {

    private static final String BASE_URL = "https://api.openweathermap.org/";
    private static final String API_KEY = "e46312fa90c63380d4fa7ba2e50f7c2a";

    public interface WeatherCheckCallback {
        void onChecked(boolean isBad, String weatherStatus);
        void onError(String errorMessage);
    }

    public static void isWeatherBad(double latitude, double longitude, WeatherCheckCallback callback) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        apiService.getCurrentWeatherData(latitude, longitude, API_KEY).enqueue(new retrofit2.Callback<WeatherResponse>() {
            @Override
            public void onResponse(retrofit2.Call<WeatherResponse> call, retrofit2.Response<WeatherResponse> response) {
                if (response.body() != null && response.body().weather != null && !response.body().weather.isEmpty()) {
                    String weatherDescription = response.body().weather.get(0).description;

                    if (weatherDescription.contains("rain") || weatherDescription.contains("storm")) {
                        callback.onChecked(true, weatherDescription);
                    } else {
                        callback.onChecked(false, weatherDescription);
                    }
                } else {
                    callback.onError("Failed to retrieve weather data.");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<WeatherResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
}
