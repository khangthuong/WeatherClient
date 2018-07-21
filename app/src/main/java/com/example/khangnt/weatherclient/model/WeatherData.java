package com.example.khangnt.weatherclient.model;

public class WeatherData {

    private String timeZone;
    private String weatherText;
    private String weatherIcon;
    private String temperature;
    private String realFeelTemperature;
    private String localObservationDateTime;
    private String relativeHumidity;
    private String windSpeed;
    private String mobileLink;
    private String time;
    private String temperatureMax;
    private String temperatureMin;

    /*public WeatherData(String weatherText, String weatherIcon, String temperature,
                             String realFeelTemperature, String localObservationDateTime,
                             String relativeHumidity, String windSpeed, String mobileLink) {

        this.weatherText = weatherText;
        this.weatherIcon = weatherIcon;
        this.temperature = temperature;
        this.realFeelTemperature = realFeelTemperature;
        this.localObservationDateTime = localObservationDateTime;
        this.relativeHumidity = relativeHumidity;
        this.windSpeed = windSpeed;
        this.mobileLink = mobileLink;
    }*/

    public String getWeatherText() {
        return weatherText;
    }

    public String getWeatherIcon() {
        return weatherIcon;
    }

    public String getTemperature() {
        return temperature;
    }

    public String getRealFeelTemperature() {
        return realFeelTemperature;
    }

    public String getLocalObservationDateTime() {
        return localObservationDateTime;
    }

    public String getRelativeHumidity() {
        return relativeHumidity;
    }

    public String getWindSpeed() {
        return windSpeed;
    }

    public String getMobileLink() {
        return mobileLink;
    }

    public void setWeatherText(String weatherText) {
        this.weatherText = weatherText;
    }

    public void setWeatherIcon(String weatherIcon) {
        this.weatherIcon = weatherIcon;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public void setRealFeelTemperature(String realFeelTemperature) {
        this.realFeelTemperature = realFeelTemperature;
    }

    public void setLocalObservationDateTime(String localObservationDateTime) {
        this.localObservationDateTime = localObservationDateTime;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public void setRelativeHumidity(String relativeHumidity) {
        this.relativeHumidity = relativeHumidity;

    }

    public void setWindSpeed(String windSpeed) {
        this.windSpeed = windSpeed;
    }

    public void setMobileLink(String mobileLink) {
        this.mobileLink = mobileLink;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTemperatureMax() {
        return temperatureMax;
    }

    public String getTemperatureMin() {
        return temperatureMin;
    }

    public void setTemperatureMax(String temperatureMax) {
        this.temperatureMax = temperatureMax;
    }

    public void setTemperatureMin(String temperatureMin) {
        this.temperatureMin = temperatureMin;
    }
}
