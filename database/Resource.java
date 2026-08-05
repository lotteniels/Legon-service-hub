/**
 * Plain model class matching the `resources` table.
 * Represents a maintenance/service resource (e.g. PDMSD crew, equipment).
 * Rename fields to match schema.sql if they differ.
 */
public class Resource {

    private int resourceId;
    private String name;
    private String category;    // e.g. "Electrical", "Plumbing", "Cleaning"
    private int locationId;     // home base / current location
    private boolean available;

    public Resource(int resourceId, String name, String category, int locationId, boolean available) {
        this.resourceId = resourceId;
        this.name = name;
        this.category = category;
        this.locationId = locationId;
        this.available = available;
    }

    public int getResourceId() { return resourceId; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public int getLocationId() { return locationId; }
    public boolean isAvailable() { return available; }

    @Override
    public String toString() {
        return "Resource{" +
                "resourceId=" + resourceId +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", locationId=" + locationId +
                ", available=" + available +
                '}';
    }
}
