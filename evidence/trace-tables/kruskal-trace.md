<!-- Owner: Graphs and Optimization -->

# Kruskal trace

Generated from `Kruskal.Result.steps()` on the shipped code.

**Graph:** 58 locations, 117 roads, cost = TIME_ADJUSTED  
**Cost function:** `TIME_ADJUSTED`, in minutes  
**Result:** 57 roads, 224.7090 minutes total

Roads are taken cheapest first. One is accepted when its endpoints are in
different fragments and rejected when they are already joined, since that road
would close a cycle. `DisjointSet.union` returning false is exactly that
signal. The loop stops once one fragment remains - every later road can only
close a cycle.

## Decisions

| # | Road | Cost (min) | Decision | Fragments left |
|---|---|---|---|---|
| 1 | 2-3 | 0.500 | accept | 57 |
| 2 | 58-21 | 1.000 | accept | 56 |
| 3 | 50-51 | 1.000 | accept | 55 |
| 4 | 48-49 | 1.100 | accept | 54 |
| 5 | 54-55 | 1.100 | accept | 53 |
| 6 | 16-21 | 1.200 | accept | 52 |
| 7 | 49-50 | 1.200 | accept | 51 |
| 8 | 52-53 | 1.200 | accept | 50 |
| 9 | 53-54 | 1.300 | accept | 49 |
| 10 | 49-51 | 1.300 | reject (cycle) | 49 |
| 11 | 58-16 | 1.300 | reject (cycle) | 49 |
| 12 | 48-50 | 1.400 | reject (cycle) | 49 |
| 13 | 53-55 | 1.400 | reject (cycle) | 49 |
| 14 | 17-18 | 1.500 | accept | 48 |
| 15 | 33-34 | 1.500 | accept | 47 |
| 16 | 52-54 | 1.500 | reject (cycle) | 47 |
| 17 | 4-13 | 1.575 | accept | 46 |
| 18 | 18-19 | 1.800 | accept | 45 |
| 19 | 57-44 | 2.000 | accept | 44 |
| 20 | 43-45 | 2.000 | accept | 43 |
| 21 | 45-46 | 2.000 | accept | 42 |
| 22 | 16-17 | 2.000 | accept | 41 |
| 23 | 26-27 | 2.000 | accept | 40 |
| 24 | 17-20 | 2.000 | accept | 39 |
| 25 | 33-58 | 2.000 | accept | 38 |
| 26 | 19-20 | 2.100 | reject (cycle) | 38 |
| 27 | 6-5 | 2.200 | accept | 37 |
| 28 | 25-26 | 2.300 | accept | 36 |
| 29 | 46-44 | 2.310 | accept | 35 |
| 30 | 57-42 | 2.400 | accept | 34 |
| 31 | 4-6 | 2.500 | accept | 33 |
| 32 | 11-35 | 2.575 | accept | 32 |
| 33 | 42-44 | 2.600 | reject (cycle) | 32 |
| 34 | 14-35 | 2.616 | accept | 31 |
| 35 | 42-43 | 2.625 | reject (cycle) | 31 |
| 36 | 14-5 | 2.700 | accept | 30 |
| 37 | 43-46 | 2.800 | reject (cycle) | 30 |
| 38 | 24-25 | 2.900 | accept | 29 |
| 39 | 20-27 | 2.940 | accept | 28 |
| 40 | 39-35 | 3.000 | accept | 27 |
| 41 | 1-11 | 3.000 | accept | 26 |
| 42 | 22-23 | 3.000 | accept | 25 |
| 43 | 11-34 | 3.000 | accept | 24 |
| 44 | 1-35 | 3.172 | reject (cycle) | 24 |
| 45 | 5-7 | 3.360 | accept | 23 |
| 46 | 45-47 | 3.360 | accept | 22 |
| 47 | 3-10 | 3.500 | accept | 21 |
| 48 | 6-35 | 3.570 | reject (cycle) | 21 |
| 49 | 34-39 | 3.600 | reject (cycle) | 21 |
| 50 | 27-35 | 3.616 | reject (cycle) | 21 |
| 51 | 5-35 | 3.776 | reject (cycle) | 21 |
| 52 | 2-35 | 3.782 | accept | 20 |
| 53 | 13-14 | 3.800 | reject (cycle) | 20 |
| 54 | 10-35 | 3.813 | reject (cycle) | 20 |
| 55 | 33-39 | 4.000 | reject (cycle) | 20 |
| 56 | 1-39 | 4.000 | reject (cycle) | 20 |
| 57 | 4-35 | 4.141 | reject (cycle) | 20 |
| 58 | 2-9 | 4.200 | accept | 19 |
| 59 | 22-35 | 4.250 | accept | 18 |
| 60 | 16-35 | 4.326 | reject (cycle) | 18 |
| 61 | 34-35 | 4.400 | reject (cycle) | 18 |
| 62 | 13-35 | 4.472 | reject (cycle) | 18 |
| 63 | 20-35 | 4.560 | reject (cycle) | 18 |
| 64 | 7-35 | 4.633 | reject (cycle) | 18 |
| 65 | 17-35 | 4.928 | reject (cycle) | 18 |
| 66 | 12-15 | 4.950 | accept | 17 |
| 67 | 28-29 | 5.500 | accept | 16 |
| 68 | 35-36 | 5.750 | accept | 15 |
| 69 | 19-35 | 5.875 | reject (cycle) | 15 |
| 70 | 46-40 | 5.928 | accept | 14 |
| 71 | 8-35 | 6.000 | accept | 13 |
| 72 | 9-35 | 6.050 | reject (cycle) | 13 |
| 73 | 36-39 | 6.050 | reject (cycle) | 13 |
| 74 | 12-35 | 6.200 | accept | 12 |
| 75 | 21-35 | 6.302 | reject (cycle) | 12 |
| 76 | 24-35 | 6.302 | reject (cycle) | 12 |
| 77 | 15-35 | 6.426 | reject (cycle) | 12 |
| 78 | 37-32 | 6.600 | accept | 11 |
| 79 | 3-35 | 6.612 | reject (cycle) | 11 |
| 80 | 29-30 | 7.056 | accept | 10 |
| 81 | 43-40 | 7.300 | reject (cycle) | 10 |
| 82 | 44-40 | 7.424 | reject (cycle) | 10 |
| 83 | 28-35 | 7.475 | accept | 9 |
| 84 | 45-40 | 7.480 | reject (cycle) | 9 |
| 85 | 26-35 | 7.752 | reject (cycle) | 9 |
| 86 | 23-35 | 7.918 | reject (cycle) | 9 |
| 87 | 25-35 | 8.001 | reject (cycle) | 9 |
| 88 | 37-38 | 8.050 | accept | 8 |
| 89 | 52-40 | 8.400 | accept | 7 |
| 90 | 35-30 | 8.400 | reject (cycle) | 7 |
| 91 | 56-30 | 9.000 | accept | 6 |
| 92 | 16-30 | 9.200 | reject (cycle) | 6 |
| 93 | 42-40 | 9.280 | reject (cycle) | 6 |
| 94 | 53-40 | 9.559 | reject (cycle) | 6 |
| 95 | 36-40 | 9.600 | accept | 5 |
| 96 | 47-40 | 9.717 | reject (cycle) | 5 |
| 97 | 21-30 | 9.775 | reject (cycle) | 5 |
| 98 | 28-36 | 10.560 | reject (cycle) | 5 |
| 99 | 32-36 | 10.626 | accept | 4 |
| 100 | 38-35 | 10.800 | reject (cycle) | 4 |
| 101 | 40-41 | 10.800 | accept | 3 |
| 102 | 50-40 | 10.906 | accept | 2 |
| 103 | 18-35 | 10.935 | reject (cycle) | 2 |
| 104 | 31-32 | 12.000 | accept | 1 |

## Cross-check against Prim

| Algorithm | Roads | Total (min) | Work |
|---|---|---|---|
| Kruskal | 57 | 224.7090 | 638 cost comparisons, 208 union-find lookups |
| Prim | 57 | 224.7090 | 234 roads examined, 458 heap comparisons |

The two build different trees edge-by-edge but must agree on total cost, and
they do to within floating-point tolerance. `MinimumSpanningTreeTest` pins that
under all three weight modes.
