<!-- Owner: Graphs and Optimization -->

# Dijkstra trace

Generated from `Dijkstra.Result.settleOrder()` on the shipped code, so this
table and the implementation cannot disagree.

**Graph:** 58 locations, 117 roads, cost = TIME_ADJUSTED  
**Cost function:** `TIME_ADJUSTED` = `travelTime_min` x `roadConditionWeight`, in minutes  
**Source:** 1, Great Hall

Every road weight is strictly positive, so Dijkstra's assumption holds and no
Bellman-Ford fallback is needed. `Graph.addRoad` rejects a non-positive weight
at load time rather than trusting the file.

## Settle order

Locations leave the frontier in non-decreasing cost. `Via` is the predecessor
recorded when the cost was last lowered.

| # | Settled | Location | Cost (min) | Via |
|---|---|---|---|---|
| 1 | 1 | Great Hall | 0.000 | - |
| 2 | 11 | Central Cafeteria Lecture Theatre | 3.000 | 1 (Great Hall) |
| 3 | 35 | University Square | 3.172 | 1 (Great Hall) |
| 4 | 39 | Central Administration Block | 4.000 | 1 (Great Hall) |
| 5 | 14 | Institute of African Studies Lecture Hall | 5.788 | 35 (University Square) |
| 6 | 34 | Bush Canteen | 6.000 | 11 (Central Cafeteria Lecture Theatre) |
| 7 | 6 | Classic Lecture Room (CLR) | 6.742 | 35 (University Square) |
| 8 | 27 | Department of Plant and Environmental Biology Laboratory | 6.788 | 35 (University Square) |
| 9 | 5 | Legon Centre for International Affairs and Diplomacy (LECIAD) | 6.948 | 35 (University Square) |
| 10 | 2 | Jones Quartey Building (JQB) - Ground Floor (Rooms 9-14) | 6.954 | 35 (University Square) |
| 11 | 10 | K. Folson Lecture Room | 6.985 | 35 (University Square) |
| 12 | 4 | K.A. Busia Lecture Theatre (KAB) | 7.313 | 35 (University Square) |
| 13 | 22 | School of Pharmacy Laboratory | 7.422 | 35 (University Square) |
| 14 | 3 | Jones Quartey Building (JQB) - First Floor (Rooms 18,19,22,23) | 7.454 | 2 (Jones Quartey Building (JQB) - Ground Floor (Rooms 9-14)) |
| 15 | 16 | University of Ghana Computing Systems (UGCS) | 7.498 | 35 (University Square) |
| 16 | 33 | Physical Development and Municipal Services Directorate (PDMSD) | 7.500 | 34 (Bush Canteen) |
| 17 | 13 | School of Law Lecture Rooms | 7.644 | 35 (University Square) |
| 18 | 20 | Ecology Laboratory (Centre for Ecological Research) | 7.732 | 35 (University Square) |
| 19 | 7 | FCOS / Home Science Annex | 7.805 | 35 (University Square) |
| 20 | 17 | Chemistry Department Laboratory | 8.100 | 35 (University Square) |
| 21 | 21 | Computer Engineering Laboratory | 8.698 | 16 (University of Ghana Computing Systems (UGCS)) |
| 22 | 26 | Department of Marine and Fisheries Sciences Laboratory | 8.788 | 27 (Department of Plant and Environmental Biology Laboratory) |
| 23 | 58 | Engineering Annex Workshop | 8.798 | 16 (University of Ghana Computing Systems (UGCS)) |
| 24 | 36 | University Avenue Junction | 8.922 | 35 (University Square) |
| 25 | 19 | Biochemistry Laboratory | 9.047 | 35 (University Square) |
| 26 | 8 | Top of Bookshop (Rooms E9, E10) | 9.172 | 35 (University Square) |
| 27 | 9 | N Block (NB1, NB2, NB3) | 9.222 | 35 (University Square) |
| 28 | 12 | Business School Lecture Rooms (UGBS) | 9.372 | 35 (University Square) |
| 29 | 24 | Department of Geology Laboratory | 9.474 | 35 (University Square) |
| 30 | 15 | College of Humanities Lecture Rooms | 9.598 | 35 (University Square) |
| 31 | 18 | Physics Department Laboratory | 9.600 | 17 (Chemistry Department Laboratory) |
| 32 | 23 | School of Nursing and Midwifery Laboratory | 10.422 | 22 (School of Pharmacy Laboratory) |
| 33 | 28 | Legon Second (Shuttle Stop) | 10.647 | 35 (University Square) |
| 34 | 25 | Department of Animal Biology Laboratory | 11.088 | 26 (Department of Marine and Fisheries Sciences Laboratory) |
| 35 | 30 | University Primary Roundabout (Shuttle Stop) | 11.572 | 35 (University Square) |
| 36 | 38 | University of Ghana Medical Centre | 13.972 | 35 (University Square) |
| 37 | 29 | Legon Police Station (Shuttle Stop) | 16.147 | 28 (Legon Second (Shuttle Stop)) |
| 38 | 40 | Legon Hill Junction | 18.522 | 36 (University Avenue Junction) |
| 39 | 32 | Banku Junction (Shuttle Stop) | 19.548 | 36 (University Avenue Junction) |
| 40 | 56 | Main Gate Security Post | 20.572 | 30 (University Primary Roundabout (Shuttle Stop)) |
| 41 | 37 | University of Ghana Stadium | 22.022 | 38 (University of Ghana Medical Centre) |
| 42 | 31 | Okponglo Junction (Shuttle Stop) | 23.862 | 36 (University Avenue Junction) |
| 43 | 46 | Mensah Sarbah Hall | 24.450 | 40 (Legon Hill Junction) |
| 44 | 43 | Akuafo Hall | 25.822 | 40 (Legon Hill Junction) |
| 45 | 44 | Commonwealth Hall | 25.946 | 40 (Legon Hill Junction) |
| 46 | 45 | Volta Hall | 26.002 | 40 (Legon Hill Junction) |
| 47 | 52 | Valco Trust Hostel | 26.922 | 40 (Legon Hill Junction) |
| 48 | 41 | Dodowa Road Junction | 27.122 | 36 (University Avenue Junction) |
| 49 | 42 | Legon Hall | 27.802 | 40 (Legon Hill Junction) |
| 50 | 57 | Night Market Junction | 27.946 | 44 (Commonwealth Hall) |
| 51 | 53 | International Students Hostel | 28.081 | 40 (Legon Hill Junction) |
| 52 | 47 | Jubilee Hall | 28.239 | 40 (Legon Hill Junction) |
| 53 | 54 | Pentagon Hostel (African Union Hall) | 28.422 | 52 (Valco Trust Hostel) |
| 54 | 50 | Elizabeth Sey Hall | 29.428 | 40 (Legon Hill Junction) |
| 55 | 55 | Bani Hostel | 29.481 | 53 (International Students Hostel) |
| 56 | 51 | Jean Nelson Aka Hall | 30.428 | 50 (Elizabeth Sey Hall) |
| 57 | 49 | Alex Kwapong Hall | 30.628 | 50 (Elizabeth Sey Hall) |
| 58 | 48 | Hilla Limann Hall | 30.828 | 50 (Elizabeth Sey Hall) |

## Route to 58, Engineering Annex Workshop

| Step | Location | Road cost | Running total |
|---|---|---|---|
| 0 | 1 Great Hall | - | 0.000 |
| 1 | 35 University Square | 3.172 | 3.172 |
| 2 | 16 University of Ghana Computing Systems (UGCS) | 4.326 | 7.498 |
| 3 | 58 Engineering Annex Workshop | 1.300 | 8.798 |

## Cost of stopping early

| | Locations settled | Roads examined | Cost to 58 |
|---|---|---|---|
| Full search | 58 | 234 | 8.798 |
| Stop at 58 | 23 | 102 | 8.798 |

Both agree on the cost, which is the point: everything settled before the
target is already final, so the remaining work is wasted for a single query.

## Compared with BFS

BFS answers a different question - fewest roads, not cheapest.

| Search | Route | Roads | Cost (min) |
|---|---|---|---|
| Dijkstra | 1 -> 35 -> 16 -> 58 | 3 | 8.798 |
| BFS | 1 -> 35 -> 16 -> 58 | 3 | 8.798 |
