/**
 * Plain model class matching the `locations` table.
 * NOTE: field names below are best-guess based on the project description
 * (58 real UG facilities: lecture halls, labs, shuttle stops, hostels, PDMSD).
 * If your schema.sql uses different column names, rename the fields here
 * (and in DataLoader.loadLocations) to match exactly — case-sensitive.
 */
public class Location {

    private int locationId;
    private String name;
    private String type;        // e.g. "Lecture Hall", "Lab", "Hostel", "Shuttle Stop"
    private double latitude;
    private double longitude;

    public Location(int locationId, String name, String type, double latitude, double longitude) {
        this.locationId = locationId;
        this.name = name;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getLocationId() { return locationId; }
    public String getName() { return name; }
    public String getType() { return type; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    @Override
    public String toString() {
        return "Location{" +
                "locationId=" + locationId +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
