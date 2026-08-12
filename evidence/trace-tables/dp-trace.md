<!-- Owner: Graphs and Optimization -->

# Dynamic programming trace: 0/1 knapsack

Generated from `KnapsackDP.Result.table()` on the shipped code.

## Why a DP and not a greedy pass

A crew has a fixed number of minutes. Each request costs travel time plus
service time and is worth the fine it avoids plus a bonus for urgency. Because
a request is taken whole or not at all, picking the best value-per-minute first
can lose. The instance below is the smallest case that shows it.

| Item | Weight | Value | Value per weight |
|---|---|---|---|
| 0 | 6 | 36 | 6.0 |
| 1 | 5 | 30 | 6.0 |
| 2 | 5 | 30 | 6.0 |

Capacity is 10. Greedy by value-per-weight takes item 0 first at 6.0 per
minute, then cannot fit either 5-weight item in the remaining 4, finishing on
36. The optimum takes items 1 and 2 for 60.

## The table

`table[i][w]` is the best value obtainable from the first `i` items within
weight `w`. Each row either copies the row above, meaning the item was not
worth taking at that budget, or improves on it.

| items \ budget | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 |
|---|---|---|---|---|---|---|---|---|---|---|---|
| **0** | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| **1** | 0 | 0 | 0 | 0 | 0 | 0 | 36 | 36 | 36 | 36 | 36 |
| **2** | 0 | 0 | 0 | 0 | 0 | 30 | 36 | 36 | 36 | 36 | 36 |
| **3** | 0 | 0 | 0 | 0 | 0 | 30 | 36 | 36 | 36 | 36 | 60 |

Reading back: `table[3][10]` is 60 and differs from `table[2][10]`, so item 2
was taken. Subtracting its weight leaves budget 5, where `table[2][5]` differs
from `table[1][5]`, so item 1 was taken too. Budget 0 remains and item 0 was
not taken. Chosen set: 1 -> 2.

## On the real data

One crew based at 33, Physical Development and Municipal Services Directorate (PDMSD), with a 240-minute shift, choosing among the
157 outstanding requests. Weight is travel time from the depot rounded up plus
15 minutes of service; value is the
avoided fine plus 20 per step of urgency.

| Measure | Value |
|---|---|
| Requests considered | 157 |
| Requests chosen | 10 |
| Minutes used | 238 of 240 |
| Total value | 835.0 |

| Chosen request | Urgency | Fine (GHS) | Source location |
|---|---|---|---|
| 8 | high | 0 | 17 Chemistry Department Laboratory |
| 52 | high | 50 | 25 Department of Animal Biology Laboratory |
| 76 | high | 15 | 26 Department of Marine and Fisheries Sciences Laboratory |
| 136 | high | 0 | 21 Computer Engineering Laboratory |
| 176 | medium | 50 | 17 Chemistry Department Laboratory |
| 198 | high | 15 | 10 K. Folson Lecture Room |
| 218 | medium | 30 | 16 University of Ghana Computing Systems (UGCS) |
| 252 | high | 50 | 24 Department of Geology Laboratory |
| 280 | high | 25 | 7 FCOS / Home Science Annex |
| 285 | high | 40 | 19 Biochemistry Laboratory |

The table is O(n x capacity) in both time and space. The full table is kept
rather than the single-row optimisation because reconstructing the chosen set
needs it, and this document reads rows straight off it.
