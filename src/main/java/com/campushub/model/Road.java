package com.campushub.model;

// Owner: Database and Data
// TODO: implement Road
public class Road {
    private int fromLocationId;
    private int toLocationId;
    private double distance_m;
    private double travelTime_min;
    private double roadConditionWeight;

    public Road(int fromLocationId, int toLocationId, double distance_m, double travelTime_min, double roadConditionWeight) {
        this.fromLocationId = fromLocationId;
        this.toLocationId = toLocationId;
        this.distance_m = distance_m;
        this.travelTime_min = travelTime_min;
        this.roadConditionWeight = roadConditionWeight;
    }

    public int getFromLocationId() {
        return fromLocationId;
    }

    public int getToLocationId() {
        return toLocationId;
    }

    public double getDistance_m() {
        return distance_m;
    }

    public double getTravelTime_min() {
        return travelTime_min;
    }

    public double getRoadConditionWeight() {
        return roadConditionWeight;
    }

    @Override
    public String toString() {
        return "Road{" +
                "fromLocationId=" + fromLocationId +
                ", toLocationId=" + toLocationId +
                ", distance_m=" + distance_m +
                ", travelTime_min=" + travelTime_min +
                ", roadConditionWeight=" + roadConditionWeight +
                '}';
    }
}
