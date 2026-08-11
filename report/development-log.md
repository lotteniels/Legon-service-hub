# Development Log — Legon Service Hub

This log tracks weekly progress, decisions, and challenges per pod, per DCIT 204/308 requirements (section 15 of the brief).

## Pods
- Database and Data: Van-Kpikpi Vanessa Selasi, Akyea Benjamin Obeng
- UI: Isabella Asantewaa
- Linear Structures: Lorretta Opoku Nsiah
- Priority Structures: Michael Gorswin Achel, Quayson Isaac Awortwe
- Tree Structures: Arhinful Solomon Kwesi, Kwabena Awuah Bosompem
- Graphs and Optimization: Gideon Elorm Glago, Duah Ebenezer Ohene Amoako
- Searching, Sorting, and Testing: Kumah Michael Nhyira, Amenumey Jude Kwame Enam
- Integration: Michael Gorswin Achel
- Report: Bismark Asare, Asante Stephanhy

---

## Week of Jul 28 – Aug 3, 2026 (M1–M2: Setup and data-structure library)
### Repo setup
- Initial project structure with pod folders created (lotte343, Jul 28).
- Package renamed to com.campushub; README and package.json updated (lotte343/lotteniels, Jul 30).

### Linear Structures
- Queue data structure implemented (lotte343, Aug 3).

### Priority Structures
- Heap and PriorityQueue implementation completed with tests (Paropenta, Aug 3–4).
- Priority structures optimized (michaelachel43-alt, Aug 6).

### UI
- Frontend layout added (Isabella172, Aug 4).

### Database and Data
- Database schema added; seed data CSV files added (Vanessasvk, Jul 31).
- Database branch merged into main (obeng-21 PR #3, Aug 6).

### Integration
- HashTable, CustomMap, CustomSet implemented (michaelachel43-alt, Jul 31–Aug 4); AuditEvent package-rename conflict merged (michaelachel43-alt, Jul 31).
- Missing files and heap priority structure added to complete priority structure set (michaelachel43-alt, Jul 30).

### Challenges
- Package rename (com.campushub) caused a merge conflict on AuditEvent.java — resolved same day.

---

## Week of Aug 4 – Aug 11, 2026 (M2–M5: Structures, search/sort, database, integration)
### Linear Structures
- Unit tests added for linear data structures (lotte343, Aug 4).
- Linked list iterator added (michaelachel43-alt, Aug 4).

### Tree Structures
- Unit tests added for BST, B-Tree, and Red-Black Tree (Hypercs1, Aug 7).
- Red-Black Tree and Binary Search Tree implementation completed with tests (Hypercs1, Aug 7).
- B-Tree evidence and performance test progress added (Aug 8).

### Searching, Sorting, and Testing
- Binary search and linear search implemented (d-phantom05, Aug 8).
- Insertion sort, selection sort, merge sort, and quicksort all implemented (d-phantom05, Aug 8).

### Database and Data
- Dataset rebuilt for the service-hub context; DataGenerator added with evidence output; .idea/db files untracked (Vanessasvk, Aug 3).
- roads.csv fixed: 19 duplicate road pairs with inconsistent weights removed, caught during Vanessa's own review (Vanessasvk, Aug 6).
- DatabaseConnection.java added; Java 21 + SQLite pom conflicts resolved during merge; unused code cleaned up (michaelachel43-alt, Aug 11).

### Integration
- Integration work commenced (michaelachel43-alt, Aug 7).
- Database branch conflicts resolved; DatabaseConnection imports cleaned (michaelachel43-alt, Aug 11).

### Report
- individual-contributions.md corrected to reflect actual pod assignments (Aug 11).
- Development log created and populated with real commit history (this entry).

### Challenges
- Data quality issue caught before merge: 19 duplicate/inconsistent road-weight pairs found and fixed by Vanessa during review (Aug 6).
- Merge conflicts on the database branch (Java version + SQLite pom.xml) required resolution before integration could proceed (Aug 11).
- Pod-assignment file was inaccurate for two weeks (Report pod missing Bismark Asare) — corrected Aug 11.

### Still outstanding (per brief milestones)
- M4 Graph and optimisation: no BFS/DFS/Dijkstra/Prim/Kruskal/greedy/DP commits seen yet.
- M6 Efficiency study: performance/ folder exists but no CSV results or graphs committed yet.
- Trace tables, proof sketches, and counterexamples (required per section on correctness evidence) not yet visible in evidence/ folder.<!-- Owner: Report pod -->
