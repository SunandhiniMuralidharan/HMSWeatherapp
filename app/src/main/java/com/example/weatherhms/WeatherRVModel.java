package com.example.weatherhms;

/**
 * @author Sunandhini Muralidharan
 * @version 1.0
 * @since 23.02.2022
 *
 */

public class WeatherRVModel {

    private String date;
    private String temperature;
    private String icon;
    private String text;

    public WeatherRVModel(String date, String temperature, String icon, String text) {
        this.date = date;
        this.temperature = temperature;
        this.icon = icon;
        this.text = text;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

