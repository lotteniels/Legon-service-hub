package com.campushub.model;

// Owner: Database and Data
// TODO: implement Location
public class Location {
    private int locationId;
    private String name;
    private String area;
    private String type;
    private String coordinates;

    public Location(int locationId, String name, String area, String type, String coordinates) {
        this.locationId = locationId;
        this.name = name;
        this.area = area;
        this.type = type;
        this.coordinates = coordinates;
    }

    public int getLocationId() {
        return locationId;
    }

    public String getName() {
        return name;
    }

    public String getArea() {
        return area;
    }

    public String getType() {
        return type;
    }

    public String getCoordinates() {
        return coordinates;
    }

    @Override
    public String toString() {
        return "Location{" +
                "locationId=" + locationId +
                ", name='" + name + '\'' +
                ", area='" + area + '\'' +
                ", type='" + type + '\'' +
                ", coordinates='" + coordinates + '\'' +
                '}';
    }
}
