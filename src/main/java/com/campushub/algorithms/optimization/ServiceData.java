package com.campushub.algorithms.optimization;

import com.campushub.model.Resource;
import com.campushub.model.ServiceRequest;
import com.campushub.structures.linear.DynamicArray;
import com.campushub.util.Csv;

import java.io.UncheckedIOException;
import java.nio.file.Path;

// Owner: Graphs and Optimization

/**
 * Loads service requests and resources from the seed CSVs into the Database pod's
 * {@link ServiceRequest} and {@link Resource} types, plus the small helpers the
 * optimisers need for ordering.
 *
 * <p>Exists because {@code DataLoader} reads from a live SQLite connection, which is
 * more than the optimisers, the tests, or a CLI demo need.
 */
public final class ServiceData {

    private ServiceData() {
    }

    /** Ordering rank for urgency: 3 high, 2 medium, 1 low, 0 unrecognised. */
    public static int urgencyRank(String urgency) {
        if (urgency == null) {
            return 0;
        }
        String label = urgency.trim().toLowerCase();
        if (label.equals("high") || label.equals("urgent")) {
            return 3;
        }
        if (label.equals("medium")) {
            return 2;
        }
        if (label.equals("low") || label.equals("normal")) {
            return 1;
        }
        return 0;
    }

    /** True if the request still needs a resource, meaning pending or overdue. */
    public static boolean awaitingService(ServiceRequest request) {
        String status = request.getStatus();
        return status != null
                && (status.equalsIgnoreCase("pending") || status.equalsIgnoreCase("overdue"));
    }

    /** Only the requests still awaiting service. */
    public static DynamicArray<ServiceRequest> outstanding(DynamicArray<ServiceRequest> requests) {
        DynamicArray<ServiceRequest> waiting = new DynamicArray<>();
        for (int index = 0; index < requests.size(); index++) {
            if (awaitingService(requests.get(index))) {
                waiting.add(requests.get(index));
            }
        }
        return waiting;
    }

    /** True if the resource can be assigned work. */
    public static boolean isAvailable(Resource resource) {
        String status = resource.getAvailabilityStatus();
        return status == null || status.equalsIgnoreCase("available");
    }

    /** Reads both files from the seed data directory. */
    public static DynamicArray<ServiceRequest> loadRequests(Path seedDirectory) {
        return readRequests(seedDirectory.resolve("service_requests.csv"));
    }

    /** @see #loadRequests */
    public static DynamicArray<Resource> loadResources(Path seedDirectory) {
        return readResources(seedDirectory.resolve("resources.csv"));
    }

    /**
     * Reads {@code service_requests.csv}.
     *
     * <p>The key column is looked up as {@code requestId} first and {@code Column1}
     * second. The current export still labels it {@code Column1} - a leftover from the
     * spreadsheet it came from - while {@code schema.sql} declares {@code requestId}.
     * Accepting both means this keeps working once the Database pod fixes the header.
     *
     * @throws UncheckedIOException if the file cannot be read
     * @throws IllegalArgumentException if a required column is missing
     */
    public static DynamicArray<ServiceRequest> readRequests(Path file) {
        DynamicArray<String[]> rows = Csv.rows(file);
        DynamicArray<ServiceRequest> requests = new DynamicArray<>();
        if (rows.isEmpty()) {
            return requests;
        }

        String[] header = rows.get(0);
        int idColumn = Csv.requiredColumn(header, file, "requestId", "Column1");
        int sourceColumn = Csv.requiredColumn(header, file, "sourceLocationId");
        int destinationColumn = Csv.requiredColumn(header, file, "destinationLocationId");
        int categoryColumn = Csv.column(header, "category");
        int urgencyColumn = Csv.requiredColumn(header, file, "urgency");
        int submittedColumn = Csv.column(header, "timeSubmitted");
        int deadlineColumn = Csv.column(header, "deadline");
        int statusColumn = Csv.column(header, "status");
        int fineColumn = Csv.column(header, "fineAmountGHS");

        for (int index = 1; index < rows.size(); index++) {
            String[] row = rows.get(index);
            if (Csv.isBlankRow(row)) {
                continue;
            }
            String fine = Csv.field(row, fineColumn);
            requests.add(new ServiceRequest(
                    Integer.parseInt(Csv.requiredField(row, idColumn, file, "requestId")),
                    Integer.parseInt(
                            Csv.requiredField(row, sourceColumn, file, "sourceLocationId")),
                    Integer.parseInt(
                            Csv.requiredField(row, destinationColumn, file, "destinationLocationId")),
                    Csv.field(row, categoryColumn),
                    Csv.requiredField(row, urgencyColumn, file, "urgency"),
                    Csv.field(row, submittedColumn),
                    Csv.field(row, deadlineColumn),
                    Csv.field(row, statusColumn),
                    fine == null ? 0 : Double.parseDouble(fine)));
        }
        return requests;
    }

    /**
     * Reads {@code resources.csv}.
     *
     * @throws UncheckedIOException if the file cannot be read
     * @throws IllegalArgumentException if a required column is missing
     */
    public static DynamicArray<Resource> readResources(Path file) {
        DynamicArray<String[]> rows = Csv.rows(file);
        DynamicArray<Resource> resources = new DynamicArray<>();
        if (rows.isEmpty()) {
            return resources;
        }

        String[] header = rows.get(0);
        int idColumn = Csv.requiredColumn(header, file, "resourceId");
        int typeColumn = Csv.column(header, "type");
        int nameColumn = Csv.column(header, "name");
        int homeColumn = Csv.requiredColumn(header, file, "homeLocationId");
        int capacityColumn = Csv.column(header, "capacity");
        int statusColumn = Csv.column(header, "availabilityStatus");

        for (int index = 1; index < rows.size(); index++) {
            String[] row = rows.get(index);
            if (Csv.isBlankRow(row)) {
                continue;
            }
            String capacity = Csv.field(row, capacityColumn);
            resources.add(new Resource(
                    Integer.parseInt(Csv.requiredField(row, idColumn, file, "resourceId")),
                    Csv.field(row, typeColumn),
                    Csv.field(row, nameColumn),
                    Integer.parseInt(Csv.requiredField(row, homeColumn, file, "homeLocationId")),
                    capacity == null ? 1 : Integer.parseInt(capacity),
                    Csv.field(row, statusColumn)));
        }
        return resources;
    }
}
