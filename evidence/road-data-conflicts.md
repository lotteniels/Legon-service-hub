<!-- Owner: Graphs and Optimization -->

# Road data: duplicate rows, and how they were resolved

## Resolved

`database/seed-data/roads.csv` now holds **117 rows for 117 distinct pairs of
locations** - no duplicates, no disagreements. Loading it is order-independent,
so route costs and spanning-tree weights are reproducible.

| Property | Value |
|---|---|
| Locations | 58 (ids 1-58, no gaps) |
| Roads | 117 |
| Duplicate rows collapsed on load | 0 |
| Connected components | 1 |
| Busiest location | 35, University Square (degree 33) |
| Spanning tree size | 57 roads |

## What the problem was

An earlier revision of the file held 136 rows for the same 117 pairs. Nineteen
pairs appeared twice and **seventeen of those disagreed about their weights**,
sometimes by a factor of four - `40<->42` was listed as both 100 m / 2.0 min and
455 m / 8.0 min. Fourteen of the seventeen involved location 40, Legon Hill
Junction, which suggested a block of hall connections had been appended on top
of rows that already existed.

That mattered because loading both rows of a pair as separate edges makes the
cost of any path through it depend on which row the loader reached first.
Dijkstra would return different distances between runs and Prim and Kruskal
would disagree about the minimum spanning tree, making every number in the
trace tables unreproducible.

The Database pod resolved it by keeping the **first** row of each pair and
deleting the rest.

## The guard that stayed

`Graph.addRoad` still collapses a road that duplicates an existing pair,
keyed on the unordered pair so `40,42` and `42,40` are recognised as the same
road. The first row loaded wins, matching the Database pod's own resolution, and
any discarded row whose weights disagreed is recorded in `Graph.conflicts()`
rather than dropped silently. On the current file nothing triggers it -
`GraphTest.realSeedDataLoadsCleanlyWithNoDuplicatesLeft` asserts exactly that -
but it means a regression in the data surfaces as a reported conflict instead of
as quietly non-deterministic routing.

## Still open

`service_requests.csv` labels its key column `Column1`, while `schema.sql`
declares `requestId`. `ServiceData.readRequests` accepts either, so nothing here
breaks, but a direct CSV-to-database import would.
