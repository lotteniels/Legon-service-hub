package com.campushub.model;

// Owner: Database and Data
// TODO: implement Resource
public class Resource {
    private int resourceId;
    private String type;
    private String name;
    private int homeLocationId;
    private int capacity;
    private String availabilityStatus;

    public Resource(int resourceId, String type, String name, int homeLocationId,
                    int capacity, String availabilityStatus) {
        this.resourceId = resourceId;
        this.type = type;
        this.name = name;
        this.homeLocationId = homeLocationId;
        this.capacity = capacity;
        this.availabilityStatus = availabilityStatus;
    }

    public int getResourceId() {
        return resourceId;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public int getHomeLocationId() {
        return homeLocationId;
    }

    public int getCapacity() {
        return capacity;
    }

    public String getAvailabilityStatus() {
        return availabilityStatus;
    }

    @Override
    public String toString() {
        return "Resource{" +
                "resourceId=" + resourceId +
                ", type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", homeLocationId=" + homeLocationId +
                ", capacity=" + capacity +
                ", availabilityStatus='" + availabilityStatus + '\'' +
                '}';
    }
}
