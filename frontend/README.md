# Frontend — Demo & Examiner Checklist

This file explains how to run the frontend for development, demo features for an examiner, and the quick checklist required for submission evidence.

## Quick Start (local static server)
1. From the repo root run:

```bash
python -m http.server 8000 --directory frontend
```

2. Open http://localhost:8000 in a browser.

3. Use the `Backend URL` field to point to a real backend (e.g. `http://localhost:8080/api`) or leave as-is.

4. For offline demos, enable `Mock mode` (checkbox in the header). Mock mode returns canned responses for all panels.

## Panels to demo (order suggested for an examiner)
- Health / Ping: click `Ping` and confirm `Online` (shows mock detail if mock mode enabled).
- Trace Tables: switch to **Trace Tables**, choose `Binary Search` (or other), click `Load Trace` and then `Export CSV`. Attach CSV to report.
- Search Locations: try browse-all (leave inputs blank) or search a term (e.g. `Balme`).
- Shortest Route: enter two different valid IDs (e.g. `1` and `35`), click `Find Route` and inspect path and timings.
- Request Queue: set `Limit` and `Status`, click `Get Requests` and review output.
- Dispatch: set max distance and toggle `Only use currently available resources`, click `Run Assignment` and inspect matches.
- Benchmark: click `Run Benchmark`, view results, and click `Export CSV`. Check the chart and attach CSV + screenshot.

## Examiner Checklist (tick during demo)
- [ ] Health check completed (backend reachable or mock mode enabled).
- [ ] Trace tables loaded and CSV exported for at least three algorithms.
- [ ] Shortest path demonstrated with a real-looking path and predecessor list.
- [ ] Reachability (BFS/DFS) demonstrated and traversal order shown.
- [ ] Dispatch algorithm demonstrated; at least one match shown and unmatched count reported.
- [ ] Benchmark run completed, CSV exported and plotted chart visible.
- [ ] Search / browse-all behaviour demonstrated (blank query returns all locations or appropriate guidance shown).
- [ ] CSV files saved and included in report (traces + benchmarks).

## Switching to the Real Backend
- Uncheck `Mock mode`.
- Set `Backend URL` to your running `ApiServer` base (e.g. `http://localhost:8080/api`).
- Click `Ping` and ensure endpoints respond. If some features fail, refer to `frontend/api-contract.md` for expected endpoints and payload shapes.

## Integration smoke tests (automated)

A simple Node script is provided to run a quick smoke test against a running backend.

Requirements:
- Node 18+ (global `fetch` support) or run with a fetch polyfill.

Run:

```bash
# from repo root
node frontend/integration-smoke.js http://localhost:8080/api
```

The script will attempt the main endpoints and print pass/fail results.

## Notes for Integration
- The frontend tolerates variations in response shapes but the canonical contract is in `frontend/api-contract.md`.
- Client-side validation uses `LOCATION_COUNT` (default 58). To avoid mismatches, either update the constant in `frontend/script.js` or run `Search Locations` once (or implement dynamic loading) before demos.

## Troubleshooting
- If Chart or CSV export does not appear, ensure the browser allows JavaScript and the page is loaded from `http` or `file` (some browsers block downloads from `file://`).
- If real backend returns unexpected JSON, adapt backend to the contract or add a small adapter server.

---
Keep this checklist as part of your submission evidence (screenshots of completed items, and the exported CSVs).
