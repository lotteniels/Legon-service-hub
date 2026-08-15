# Legon Service Hub - DCIT 204/308 DSA Project

A practical project integrating algorithm design, empirical analysis, custom data structures, graph algorithms, and resource optimisation to solve service operations problems for the University of Ghana campus.

## Team Pods & Folder Structure
This repository is mapped to team pod assignments:
- **Database and Data:** `com.campushub.db`, `com.campushub.model`
- **Linear Structures:** `com.campushub.structures.linear`
- **Priority Structures:** `com.campushub.structures.priority`
- **Tree Structures:** `com.campushub.structures.tree`
- **Graphs and Optimization:** `com.campushub.structures.graph`, `com.campushub.algorithms.graph`, `com.campushub.algorithms.optimization`
- **Searching, Sorting, and Testing:** `com.campushub.algorithms.search`, `com.campushub.algorithms.sort`
- **Integration:** `com.campushub.engine`, `com.campushub.cli`, `com.campushub.ApiServer`
- **Report & Evidence:** `report/`, `evidence/`

*See `report/individual-contributions.md` for who owns what.*

## Academic Integrity
This project strictly adheres to the DCIT 204/308 guidelines. 
- All data structures and algorithms are custom-built from scratch without the use of `java.util.*` collections.
- For our AI usage disclosure and accountability statement, see [AI_ACKNOWLEDGEMENT.md](./AI_ACKNOWLEDGEMENT.md).

## Getting Started

### 1. Build and Run Tests
Ensure all 191 custom unit tests pass cleanly:
```bash
mvn clean test
```

### 2. Run the Application
The application can be run in CLI mode or as a REST API server.
```bash
mvn exec:java -Dexec.mainClass="com.campushub.Main"
```

From the CLI menu, you can explore:
- Dispatch / Schedule Requests (Priority, FIFO, Circular, Deque)
- Route Finder (Dijkstra, BFS, MST)
- Resource Optimisation (Greedy, Knapsack DP)
- Efficiency Lab (Empirical benchmarking with 3-run averages)
- Search / Indexing (BST, Red-Black Tree, B-Tree, Hash Table)
- Graph Traversal (BFS & DFS Reachability)

### 3. Efficiency Lab & Performance Export
To run the empirical efficiency experiments and export the results to CSV for your report's trace graphs:
1. Run the system and select **Efficiency Lab** from the menu.
2. The engine will run sorts, searches, and graph traversals across variable input sizes.
3. The results (time in nanoseconds and memory in KB) are saved to the database.
4. The output is exported to `database/algorithm_runs_export.csv`. Use this file in Excel or Python to generate the final plots for the report.
