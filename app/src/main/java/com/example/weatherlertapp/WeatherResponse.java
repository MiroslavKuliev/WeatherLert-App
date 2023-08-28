package com.example.weatherlertapp;

import java.util.List;

public class WeatherResponse {
    public Main main;
    public List<Weather> weather;

    public static class Main {
        public double temp;
    }

    public static class Weather {
        public String description;
    }
}
