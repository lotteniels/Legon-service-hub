/**
 * Plain model class matching the `roads` table.
 * Represents an edge between two locations (used by the Graphs team).
 * Rename fields to match schema.sql if they differ.
 */
public class Road {

    private int roadId;
    private int fromLocationId;
    private int toLocationId;
    private double distance;    // or "weight" — adjust name if schema differs
    private boolean walkable;   // drop this field if your schema doesn't have it

    public Road(int roadId, int fromLocationId, int toLocationId, double distance, boolean walkable) {
        this.roadId = roadId;
        this.fromLocationId = fromLocationId;
        this.toLocationId = toLocationId;
        this.distance = distance;
        this.walkable = walkable;
    }

    public int getRoadId() { return roadId; }
    public int getFromLocationId() { return fromLocationId; }
    public int getToLocationId() { return toLocationId; }
    public double getDistance() { return distance; }
    public boolean isWalkable() { return walkable; }

    @Override
    public String toString() {
        return "Road{" +
                "roadId=" + roadId +
                ", fromLocationId=" + fromLocationId +
                ", toLocationId=" + toLocationId +
                ", distance=" + distance +
                ", walkable=" + walkable +
                '}';
    }
}
