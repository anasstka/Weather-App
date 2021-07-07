package com.example.weatherapp;

import java.io.Serializable;

public class CurrentWeather implements Serializable {
    private int temperature;        // температура
    private int feelsLike;         // по ощущениям
    private int humidity;           // влажность
    private int pressure;           // давление в мм рт ст
    private int windSpeed;         // скорость ветра в м/с
    private String windDirection;  // направление ветра (одна/две буквы)
    private String precipitation;   // осадки
    private String weather;         // описание погоды
    private String idIcon;          // id иконки
    private int icon;               // икнока

    public CurrentWeather(int temperature, int feelsLike, int humidity, int pressure, int windSpeed, String windDirection, String precipitation, String weather, String idIcon, int icon) {
        this.temperature = temperature;
        this.feelsLike = feelsLike;
        this.humidity = humidity;
        this.pressure = pressure;
        this.windSpeed = windSpeed;
        this.windDirection = windDirection;
        this.precipitation = precipitation;
        this.weather = weather;
        this.idIcon = idIcon;
        this.icon = icon;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public int getFeelsLike() {
        return feelsLike;
    }

    public void setFeelsLike(int feelsLike) {
        this.feelsLike = feelsLike;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public int getPressure() {
        return pressure;
    }

    public void setPressure(int pressure) {
        this.pressure = pressure;
    }

    public int getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(int windSpeed) {
        this.windSpeed = windSpeed;
    }

    public String getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(String windDirection) {
        this.windDirection = windDirection;
    }

    public String getPrecipitation() {
        return precipitation;
    }

    public void setPrecipitation(String precipitation) {
        this.precipitation = precipitation;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getIdIcon() {
        return idIcon;
    }

    public void setIdIcon(String idIcon) {
        this.idIcon = idIcon;
    }
}
