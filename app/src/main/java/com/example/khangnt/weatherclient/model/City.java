package com.example.khangnt.weatherclient.model;

public class City {

    private String name;
    private String detail;
    private String placeID;

    public String getName() {
        return name;
    }

    public String getDetail() {
        return detail;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getPlaceID() {
        return placeID;
    }

    public void setPlaceID(String placeID) {
        this.placeID = placeID;
    }
}
