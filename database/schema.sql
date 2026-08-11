CREATE TABLE locations (
                           locationId INTEGER PRIMARY KEY,
                           name TEXT, area TEXT, type TEXT, coordinates TEXT
);
CREATE TABLE roads (
                       fromLocationId INTEGER, toLocationId INTEGER,
                       distance_m REAL, travelTime_min REAL, roadConditionWeight REAL,
                       FOREIGN KEY (fromLocationId) REFERENCES locations(locationId),
                       FOREIGN KEY (toLocationId) REFERENCES locations(locationId)
);
CREATE TABLE service_requests (
                                  requestId INTEGER PRIMARY KEY,
                                  sourceLocationId INTEGER, destinationLocationId INTEGER,
                                  category TEXT, urgency TEXT, timeSubmitted TEXT, deadline TEXT,
                                  status TEXT, fineAmountGHS REAL,
                                  FOREIGN KEY (sourceLocationId) REFERENCES locations(locationId),
                                  FOREIGN KEY (destinationLocationId) REFERENCES locations(locationId)
);
CREATE TABLE resources (
                           resourceId INTEGER PRIMARY KEY,
                           type TEXT, name TEXT, homeLocationId INTEGER,
                           capacity INTEGER, availabilityStatus TEXT,
                           FOREIGN KEY (homeLocationId) REFERENCES locations(locationId)
);
CREATE TABLE algorithm_runs (
                                runId INTEGER PRIMARY KEY,
                                algorithmName TEXT, inputSize INTEGER,
                                timeNs INTEGER, memoryKb INTEGER, dateRun TEXT
);
CREATE TABLE audit_events (
                              eventId INTEGER PRIMARY KEY,
                              eventType TEXT, description TEXT, timestamp TEXT
);