# DCIT 204/308 Project — AI Acknowledgement & Compliance Statement

In accordance with the DCIT 204/308 project brief requirements regarding academic integrity and AI assistance:

## AI Usage Disclosure
This project utilised an AI coding assistant (Google Gemini / Antigravity) during development. The AI was used for:
1. **Gap Analysis & Code Auditing:** Reviewing the initial codebase against the strict 10-module rubric.
2. **Refactoring & Wiring:** Connecting isolated data structures (like `BTree`, `CircularQueue`, `Deque`) to the application engines (`IndexingEngine`, `RequestSchedulingEngine`).
3. **Graph Algorithm Integration:** Helping to wire the pre-existing graph traversal algorithms (BFS, DFS, Dijkstra, Kruskal, Prim) to the `RouteEngine` and CLI.
4. **Efficiency Lab Expansion:** Upgrading the benchmarking engine to perform 3-run averages and proper JVM memory measurements across dynamic input sizes.

## Student Accountability
All AI-assisted code was thoroughly reviewed, tested, and integrated by the team. The team members retain full understanding of the underlying data structures and algorithms, including:
- How the generic `BTree` derives its minimum degree (t-value) from the team's index numbers.
- How the `RedBlackTree` performs colour flipping and rotations.
- How `Dijkstra's` shortest path algorithm resolves the cheapest route.
- How the `KnapsackDP` (0/1 Knapsack) optimises resource shifts via a DP table.

The team is fully prepared to explain, trace by hand, and modify any part of this implementation during the final oral defence, as required by the grading rubric.
