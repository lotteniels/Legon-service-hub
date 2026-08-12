# Frontend Build Guide — Legon Service Hub
**For:** Isabella (UI Developer)
**From:** Mike (Integration Lead)

---

## How Everything Works (Read This First)

The backend is a Java program that runs on your computer at `http://localhost:8080`. It does all the data processing. Your job is to build web pages that talk to it.

You talk to the backend using JavaScript's `fetch()`:
```javascript
fetch("http://localhost:8080/api/route?from=1&to=35")
  .then(res => res.json())
  .then(data => { /* put data on the page */ });
```

You only write **HTML, CSS, JavaScript**. No frameworks needed unless you want them.

---

## Pages You Must Build

### PAGE 1: Dashboard (Home Screen)
**File:** `index.html`
**Purpose:** The first screen the examiner sees. Shows an overview of the whole system.

**Must contain:**
- App title: "Legon Service Hub"
- A navigation menu linking to all other pages
- 4 summary stat cards showing:
  - Total locations in the system
  - Total pending service requests
  - Total available resources
  - System status ("Online")
- A "Quick Dispatch" button that fetches the next urgent job and shows it on screen
- A recent activity feed showing the last few audit/undo log entries

**Endpoints needed:**
| What | URL | Returns |
|---|---|---|
| Count of locations | `GET /api/locations` | List of all locations |
| Count of requests | `GET /api/requests` | List of all service requests |
| Count of resources | `GET /api/resources` | List of all resources |
| Recent audit log | `GET /api/audit` | List of recent system events |

---

### PAGE 2: Route Finder
**File:** `route.html`
**Purpose:** Lets the examiner find the shortest path between any two campus locations. This demonstrates Dijkstra's Algorithm, BFS, and DFS.

**Must contain:**
- Two dropdown menus — "From Location" and "To Location" (populated from the locations list)
- A "Find Shortest Path" button → calls Dijkstra endpoint → displays the route and total distance
- A "Check Reachability (BFS)" button → calls BFS endpoint → shows all locations reachable from the selected start point
- A "Run DFS Traversal" button → calls DFS endpoint → shows the traversal order from the selected start point
- A "Show Minimum Spanning Tree" button → calls MST endpoint → shows the cheapest way to connect all campus locations (Kruskal/Prim)
- A results panel below the buttons that displays the response text

**Endpoints needed:**
| What | URL | Returns |
|---|---|---|
| All locations (for dropdowns) | `GET /api/locations` | List of location names and IDs |
| Shortest path | `GET /api/route?from={id}&to={id}` | Path, distance, travel time |
| BFS reachability | `GET /api/bfs?start={id}` | List of reachable locations |
| DFS traversal | `GET /api/dfs?start={id}` | Traversal order |
| Minimum Spanning Tree | `GET /api/mst` | List of edges + total cost |

---

### PAGE 3: Dispatch Queue
**File:** `schedule.html`
**Purpose:** Shows all incoming service requests and lets the examiner dispatch them. This demonstrates the Priority Queue, FIFO Queue, Deque, and the Stack (undo).

**Must contain:**
- A toggle to switch between **Priority Mode** (urgent jobs first) and **FIFO Mode** (first-come first-served)
- A list of all pending service requests, each showing: ID, description, location, urgency level (HIGH/MEDIUM/LOW), time submitted
- A "Dispatch Next Job" button → calls the schedule endpoint → the top job moves to "In Progress" status
- An "Undo Last Dispatch" button → calls the undo endpoint → the last dispatched job is returned to pending
- A second list showing "In Progress" and "Resolved" jobs

**Endpoints needed:**
| What | URL | Returns |
|---|---|---|
| All requests | `GET /api/requests` | Full list of service requests with status |
| Next job (Priority) | `GET /api/schedule?mode=priority` | The single highest-urgency pending job |
| Next job (FIFO) | `GET /api/schedule?mode=fifo` | The single oldest pending job |
| Undo last action | `POST /api/undo` | The job that was just un-dispatched |

---

### PAGE 4: Search & Index
**File:** `search.html`
**Purpose:** Lets the examiner search for any record by ID. This demonstrates the BST and Hash Table index.

**Must contain:**
- A search bar at the top
- A dropdown to choose what to search: "Locations", "Service Requests", or "Resources"
- A "Search" button → calls the index endpoint → displays the full record found
- A results card showing all fields of the found record (ID, name, type, status, etc.)
- A small note below showing: "Found using: Binary Search Tree Index" or "Hash Table Lookup"

**Endpoints needed:**
| What | URL | Returns |
|---|---|---|
| Search by ID | `GET /api/index?type=location&id={id}` | Full record details |
| Search by ID | `GET /api/index?type=request&id={id}` | Full record details |
| Search by ID | `GET /api/index?type=resource&id={id}` | Full record details |

---

### PAGE 5: Sorting Demo
**File:** `sorting.html`
**Purpose:** Lets the examiner run the 4 sorting algorithms and compare their speed. This demonstrates Selection Sort, Insertion Sort, Merge Sort, and Quicksort.

**Must contain:**
- A slider or input box to choose the input size (e.g. 100 to 10,000 records)
- Four buttons: "Run Selection Sort", "Run Insertion Sort", "Run Merge Sort", "Run Quicksort"
- A "Run All & Compare" button that runs all four at once
- A results table showing: Algorithm Name | Input Size | Time Taken (ms)
- A line graph (use Chart.js) plotting all four algorithms side by side

**Endpoints needed:**
| What | URL | Returns |
|---|---|---|
| Run one sort | `GET /api/sort?algorithm=merge&size=1000` | Algorithm name, input size, time taken |
| Run all sorts | `GET /api/sort?algorithm=all&size=1000` | Results for all 4 algorithms |

---

### PAGE 6: Optimization Engine
**File:** `optimize.html`
**Purpose:** Shows the Greedy and Dynamic Programming algorithms solving the resource assignment problem.

**Must contain:**
- A panel showing available resources (staff/vehicles) and their capacity
- A panel showing pending jobs and their requirements
- A "Run Greedy Assignment" button → calls greedy endpoint → shows which resource is assigned to which job
- A "Run DP Optimization" button → calls DP endpoint → shows the optimal assignment
- A side-by-side comparison table showing the difference in outcome between Greedy and DP
- A text note below highlighting a scenario where Greedy gave the wrong answer (for the project brief counterexample requirement)

**Endpoints needed:**
| What | URL | Returns |
|---|---|---|
| Run Greedy | `GET /api/optimize?mode=greedy` | Resource-to-job assignment + total cost |
| Run DP | `GET /api/optimize?mode=dp` | Optimal assignment + total cost |
| All resources | `GET /api/resources` | List of resources |
| All requests | `GET /api/requests` | List of jobs |

---

### PAGE 7: Efficiency Lab (Analytics)
**File:** `efficiency.html`
**Purpose:** Shows the performance graphs for all algorithms. This is the empirical analysis section the project brief requires.

**Must contain:**
- A dropdown to select which experiment to view:
  - Search Comparison (Linear vs Binary Search)
  - Sorting Comparison (all 4 sorts)
  - Hash Table Load Factor
  - BST vs Red-Black Tree
  - Graph Algorithms (BFS/DFS/Dijkstra/MST)
- A large line graph (Chart.js) showing runtime (Y-axis) vs input size (X-axis)
- A raw data table below the graph showing every data point
- A "Run New Experiment" button → triggers a fresh run and saves it to the database
- A machine specs note at the bottom (e.g. "Tests run on: Intel i5, 8GB RAM, Windows 11")

**Endpoints needed:**
| What | URL | Returns |
|---|---|---|
| Get saved experiment results | `GET /api/efficiency?experiment=sort` | List of `{algorithmName, inputSize, timeNs}` objects |
| Run a new experiment | `POST /api/efficiency/run?experiment=sort` | Fresh timing results |

---

## Full List of All API Endpoints

Here is the complete reference for every single URL the backend will provide:

| URL | Method | Page that uses it |
|---|---|---|
| `/api/locations` | GET | Dashboard, Route Finder |
| `/api/requests` | GET | Dashboard, Dispatch Queue, Optimization |
| `/api/resources` | GET | Dashboard, Optimization |
| `/api/audit` | GET | Dashboard |
| `/api/route?from={id}&to={id}` | GET | Route Finder |
| `/api/bfs?start={id}` | GET | Route Finder |
| `/api/dfs?start={id}` | GET | Route Finder |
| `/api/mst` | GET | Route Finder |
| `/api/schedule?mode=priority` | GET | Dispatch Queue |
| `/api/schedule?mode=fifo` | GET | Dispatch Queue |
| `/api/undo` | POST | Dispatch Queue |
| `/api/index?type={type}&id={id}` | GET | Search & Index |
| `/api/sort?algorithm={name}&size={n}` | GET | Sorting Demo |
| `/api/optimize?mode=greedy` | GET | Optimization |
| `/api/optimize?mode=dp` | GET | Optimization |
| `/api/efficiency?experiment={name}` | GET | Efficiency Lab |
| `/api/efficiency/run?experiment={name}` | POST | Efficiency Lab |

---

## What Data Comes Back (Response Format)

All responses come back as **JSON**. Here are examples of what each type looks like:

**A Location:**
```json
{ "locationId": 5, "name": "Balme Library", "area": "Main Campus", "type": "Library" }
```

**A Service Request:**
```json
{ "requestId": 12, "description": "Burst pipe", "urgency": "HIGH", "status": "PENDING", "sourceLocationId": 3, "destinationLocationId": 7 }
```

**A Sort Result:**
```json
{ "algorithm": "MergeSort", "inputSize": 1000, "timeMs": 12 }
```

**An Efficiency Experiment:**
```json
[
  { "algorithm": "LinearSearch", "inputSize": 100, "timeNs": 45000 },
  { "algorithm": "BinarySearch", "inputSize": 100, "timeNs": 800 }
]
```

---

## Design Requirements

- **Theme:** Dark mode. Background `#0d1117`, accent color electric blue or purple.
- **Font:** Add this to every page's `<head>`:
  ```html
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">
  ```
- **Graphs:** Add Chart.js to every page that has a graph:
  ```html
  <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
  ```
- **Style:** Glassmorphism panels, smooth hover effects on all buttons, subtle animations.
- The examiner will see the UI first. Make it look like a professional product.

---

## What Isabella Needs From Mike Before She Can Finish

| What she needs | When |
|---|---|
| Confirmation that all endpoint URLs are live and returning JSON | When backend is complete |
| Confirmation of the exact JSON field names (e.g. `locationId` vs `location_id`) | Before she writes final JS parsing code |
| A heads-up if CORS errors appear (Mike will fix in 2 minutes) | Whenever she tests |
