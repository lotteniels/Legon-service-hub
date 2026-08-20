
function getBaseUrl() {
  const el = document.getElementById('baseUrl');
  if (el && el.value.trim()) return el.value.trim().replace(/\/$/, '');
  return window.location.origin && window.location.origin !== 'null' && !window.location.origin.startsWith('file:') 
    ? window.location.origin 
    : 'http://localhost:8080';
}

function switchTab(name, event) {
  if (event && typeof event.preventDefault === 'function') {
    event.preventDefault();
  }

  document.querySelectorAll('.panel-section').forEach(s => s.classList.remove('active'));
  document.querySelectorAll('.nav-tab').forEach(t => {
    t.classList.remove('active');
    t.setAttribute('aria-selected', 'false');
  });
  const sec = document.getElementById('section-' + name);
  if (sec) sec.classList.add('active');
  const tab = document.getElementById('tab-' + name);
  if (tab) {
    tab.classList.add('active');
    tab.setAttribute('aria-selected', 'true');
  }
}

function enforceButtonDefaults() {
  document.querySelectorAll('button').forEach((button) => {
    if (!button.hasAttribute('type')) {
      button.type = 'button';
    }
  });
}

function setLoading(btnId, loading) {
  const btn = document.getElementById(btnId);
  if (!btn) return;
  btn.disabled = loading;
  btn.classList.toggle('loading', loading);
}

function showRawResult(prefix, data, isError, elapsed) {
  const box   = document.getElementById(prefix + 'Result');
  const label = document.getElementById(prefix + 'ResultLabel');
  const time  = document.getElementById(prefix + 'ResultTime');
  const body  = document.getElementById(prefix + 'ResultBody');

  if (!box) return;
  box.classList.add('visible');
  box.style.display = 'block';
  box.classList.toggle('error', !!isError);

  if (label) label.textContent = isError ? '⚠ Error' : '✓ Response';
  if (time)  time.textContent  = elapsed != null ? elapsed + ' ms' : '';
  if (body)  body.textContent  = typeof data === 'string' ? data : JSON.stringify(data, null, 2);
}

async function checkHealth() {
  const dot = document.getElementById('statusDot');
  const label = document.getElementById('statusLabel');
  const base = getBaseUrl();

  if (dot) dot.className = 'status-dot';
  if (label) label.textContent = 'Checking…';

  try {
    const res = await fetch(base + '/api/locations', { method: 'GET', signal: AbortSignal.timeout(3000) });
    if (res.ok) {
      if (dot) dot.className = 'status-dot online';
      if (label) label.textContent = 'Online';
      return;
    }
  } catch (err) {}

  try {
    const res = await fetch(base + '/search/locations', { method: 'GET', signal: AbortSignal.timeout(3000) });
    if (res.ok) {
      if (dot) dot.className = 'status-dot online';
      if (label) label.textContent = 'Online';
      return;
    }
  } catch (err) {}

  if (dot) dot.className = 'status-dot offline';
  if (label) label.textContent = 'Offline';
}

// ----------------------------------------------------
// INITIALIZATION & LOCATIONS DROPDOWN LOAD
// ----------------------------------------------------

window.addEventListener('load', async () => {
  enforceButtonDefaults();

  const baseInput = document.getElementById('baseUrl');
  if (baseInput) {
    if (!baseInput.value) {
      baseInput.value = 'http://localhost:8080';
    }
    // Auto-check health when backend URL is updated or typed
    baseInput.addEventListener('input', checkHealth);
    baseInput.addEventListener('change', checkHealth);
  }
  await checkHealth();
  await loadLocationsDropdown();
  await loadDashboardData();

  // Auto-ping every 15 seconds to keep health status updated
  setInterval(checkHealth, 15000);
});

enforceButtonDefaults();

async function loadLocationsDropdown() {
  const srcSelect = document.getElementById('routeSource');
  const dstSelect = document.getElementById('routeDest');
  if (!srcSelect || !dstSelect) return;

  try {
    let locs = [];
    const res = await fetch(getBaseUrl() + '/api/locations', { method: 'GET' });
    if (res.ok) {
      locs = await res.json();
    } else {
      const alt = await fetch(getBaseUrl() + '/search/locations', { method: 'GET' });
      if (alt.ok) locs = await alt.json();
    }

    const items = Array.isArray(locs) ? locs : (locs.locations ?? locs.results ?? locs.data ?? []);

    if (Array.isArray(items) && items.length > 0) {
      let optionsHtml = '';
      items.forEach(l => {
        const id = l.locationId ?? l.id;
        const name = l.name ?? `Location #${id}`;
        optionsHtml += `<option value="${id}">${name} (ID: ${id})</option>`;
      });
      srcSelect.innerHTML = optionsHtml;
      dstSelect.innerHTML = optionsHtml;
      if (items.length > 1) dstSelect.selectedIndex = 1;

      const locStat = document.getElementById('dashStatLocations');
      if (locStat) locStat.textContent = items.length;
    } else {
      srcSelect.innerHTML = '<option value="">No locations returned from API</option>';
      dstSelect.innerHTML = '<option value="">No locations returned from API</option>';
    }
  } catch (err) {
    srcSelect.innerHTML = '<option value="">API Offline / Unreachable</option>';
    dstSelect.innerHTML = '<option value="">API Offline / Unreachable</option>';
  }
}

// ----------------------------------------------------
// PAGE 1: DASHBOARD
// ----------------------------------------------------

async function loadDashboardData() {
  const base = getBaseUrl();

  try {
    const resLoc = await fetch(base + '/api/locations');
    if (resLoc.ok) {
      const locs = await resLoc.json();
      const items = Array.isArray(locs) ? locs : (locs.locations ?? locs.data ?? []);
      const el = document.getElementById('dashStatLocations');
      if (el) el.textContent = items.length || locs.total || '--';
    }
  } catch (e) {}

  try {
    const resGraph = await fetch(base + '/api/route?mode=summary');
    if (resGraph.ok) {
      const summary = await resGraph.json();
      const text = summary.result ?? summary.summary ?? summary;
      const match = String(text).match(/(\d+)\s+roads?/i);
      const el = document.getElementById('dashStatRoads');
      if (el && match) el.textContent = match[1];
    }
  } catch (e) {}

  try {
    const resReq = await fetch(base + '/api/requests');
    if (resReq.ok) {
      const reqs = await resReq.json();
      const items = Array.isArray(reqs) ? reqs : (reqs.requests ?? reqs.items ?? []);
      const el = document.getElementById('dashStatRequests');
      if (el) el.textContent = items.length || reqs.total || '--';
    }
  } catch (e) {}

  try {
    const resRes = await fetch(base + '/api/resources');
    if (resRes.ok) {
      const resources = await resRes.json();
      const items = Array.isArray(resources) ? resources : (resources.resources ?? []);
      const el = document.getElementById('dashStatResources');
      if (el) el.textContent = items.length || resources.total || '--';
    }
  } catch (e) {}

  loadAuditFeed();
}

async function quickDispatchNextJob() {
  const box = document.getElementById('quickDispatchResult');
  const body = document.getElementById('quickDispatchResultBody');
  if (box) box.style.display = 'block';
  if (body) body.textContent = 'Calling GET /api/schedule?mode=priority...';

  setLoading('btnQuickDispatch', true);
  try {
    let res = await fetch(getBaseUrl() + '/api/schedule?mode=priority');
    if (!res.ok) {
      res = await fetch(getBaseUrl() + '/requests/prioritized?limit=1');
    }
    const data = await res.json();
    setLoading('btnQuickDispatch', false);

    if (!res.ok) {
      if (body) body.textContent = `API Error (${res.status}):\n${JSON.stringify(data, null, 2)}`;
      return;
    }

    if (body) body.textContent = JSON.stringify(data, null, 2);
    loadAuditFeed();
  } catch (err) {
    setLoading('btnQuickDispatch', false);
    if (body) body.textContent = `Could not reach backend API.\nError: ${err.message}`;
  }
}

async function loadAuditFeed() {
  const body = document.getElementById('auditFeedBody');
  if (!body) return;

  try {
    const res = await fetch(getBaseUrl() + '/api/audit');
    if (!res.ok) {
      body.textContent = `API Endpoint /api/audit returned status ${res.status}. Awaiting backend completion.`;
      return;
    }
    const data = await res.json();
    const logs = Array.isArray(data) ? data : (data.logs ?? data.auditLog ?? []);
    if (logs.length === 0) {
      body.textContent = 'No audit log entries returned by API.';
      return;
    }
    body.textContent = JSON.stringify(logs, null, 2);
  } catch (err) {
    body.textContent = `Audit feed offline. Backend API endpoint /api/audit is not reachable.`;
  }
}

// ----------------------------------------------------
// PAGE 2: ROUTE FINDER
// ----------------------------------------------------

async function findRouteShortestPath() {
  const from = document.getElementById('routeSource').value;
  const to = document.getElementById('routeDest').value;
  const crit = document.getElementById('routeCriteria').value;

  if (!from || !to) {
    showRawResult('route', 'Please select both From and To locations.', true, null);
    return;
  }

  const t0 = performance.now();
  try {
    let res = await fetch(getBaseUrl() + `/api/route?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}&criteria=${encodeURIComponent(crit)}`);
    if (!res.ok) {
      res = await fetch(getBaseUrl() + '/routes/shortest-path', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ sourceLocationId: Number(from), destinationLocationId: Number(to), optimizationCriteria: crit })
      });
    }
    const data = await res.json();
    const ms = Math.round(performance.now() - t0);
    showRawResult('route', data, !res.ok, ms);
  } catch (err) {
    showRawResult('route', `API Error: ${err.message}`, true, null);
  }
}

async function runRouteBFS() {
  const from = document.getElementById('routeSource').value;
  if (!from) {
    showRawResult('route', 'Please select a From location for BFS reachability.', true, null);
    return;
  }

  const t0 = performance.now();
  try {
    let res = await fetch(getBaseUrl() + `/api/bfs?start=${from}`);
    if (!res.ok) {
      res = await fetch(getBaseUrl() + `/graph/reachable?sourceId=${from}&algorithm=bfs`);
    }
    const data = await res.json();
    const ms = Math.round(performance.now() - t0);
    showRawResult('route', data, !res.ok, ms);
  } catch (err) {
    showRawResult('route', `BFS API Error: ${err.message}`, true, null);
  }
}

async function runRouteDFS() {
  const from = document.getElementById('routeSource').value;
  if (!from) {
    showRawResult('route', 'Please select a From location for DFS traversal.', true, null);
    return;
  }

  const t0 = performance.now();
  try {
    let res = await fetch(getBaseUrl() + `/api/dfs?start=${from}`);
    if (!res.ok) {
      res = await fetch(getBaseUrl() + `/graph/reachable?sourceId=${from}&algorithm=dfs`);
    }
    const data = await res.json();
    const ms = Math.round(performance.now() - t0);
    showRawResult('route', data, !res.ok, ms);
  } catch (err) {
    showRawResult('route', `DFS API Error: ${err.message}`, true, null);
  }
}

async function runRouteMST() {
  const t0 = performance.now();
  try {
    let res = await fetch(getBaseUrl() + '/api/mst');
    if (!res.ok) {
      res = await fetch(getBaseUrl() + '/graph/mst');
    }
    const data = await res.json();
    const ms = Math.round(performance.now() - t0);
    showRawResult('route', data, !res.ok, ms);
  } catch (err) {
    showRawResult('route', `MST API Error: ${err.message}`, true, null);
  }
}

// ----------------------------------------------------
// PAGE 3: DISPATCH QUEUE
// ----------------------------------------------------

async function fetchDispatchQueue() {
  const mode = document.getElementById('queueOrdering').value;
  const limit = document.getElementById('reqLimit').value || 10;
  const t0 = performance.now();

  try {
    let res = await fetch(getBaseUrl() + `/api/schedule?mode=${mode}&limit=${limit}`);
    if (!res.ok) {
      res = await fetch(getBaseUrl() + `/requests/prioritized?limit=${limit}&status=pending`);
    }
    const data = await res.json();
    const ms = Math.round(performance.now() - t0);
    showRawResult('requests', data, !res.ok, ms);
  } catch (err) {
    showRawResult('requests', `Dispatch Queue API Error: ${err.message}`, true, null);
  }
}

async function undoLastDispatchAction() {
  const t0 = performance.now();
  try {
    const res = await fetch(getBaseUrl() + '/api/undo', { method: 'POST' });
    const data = await res.json();
    const ms = Math.round(performance.now() - t0);
    showRawResult('requests', data, !res.ok, ms);
    loadAuditFeed();
  } catch (err) {
    showRawResult('requests', `Undo API Error: ${err.message}`, true, null);
  }
}

// ----------------------------------------------------
// PAGE 4: OPTIMIZATION ENGINE
// ----------------------------------------------------

async function runGreedyOptimization() {
  const maxDist = document.getElementById('dispatchMaxDist').value || 1000;
  const onlyAvailable = document.getElementById('dispatchOnlyAvailable').checked;
  const t0 = performance.now();

  try {
    let res = await fetch(getBaseUrl() + `/api/optimize?mode=greedy&maxDistance=${encodeURIComponent(maxDist)}&onlyAvailable=${onlyAvailable}`);
    if (!res.ok) {
      res = await fetch(getBaseUrl() + '/optimization/dispatch', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ maxDistanceMeters: Number(maxDist), onlyAvailable })
      });
    }
    const data = await res.json();
    const ms = Math.round(performance.now() - t0);
    showRawResult('dispatch', data, !res.ok, ms);
  } catch (err) {
    showRawResult('dispatch', `Greedy Optimization API Error: ${err.message}`, true, null);
  }
}

async function runDPOptimization() {
  const t0 = performance.now();
  try {
    let res = await fetch(getBaseUrl() + '/api/optimize?mode=dp&depot=1&shift=240');
    if (!res.ok) {
      res = await fetch(getBaseUrl() + '/optimization/dp');
    }
    const data = await res.json();
    const ms = Math.round(performance.now() - t0);
    showRawResult('dispatch', data, !res.ok, ms);
  } catch (err) {
    showRawResult('dispatch', `DP Optimization API Error: ${err.message}`, true, null);
  }
}

// ----------------------------------------------------
// PAGE 5: SEARCH & INDEX
// ----------------------------------------------------

async function searchByIdIndex() {
  const id = document.getElementById('searchEntityId').value.trim();
  const type = document.getElementById('searchEntityType').value;

  if (!id) {
    showRawResult('search', 'Please enter a Record ID to search.', true, null);
    return;
  }

  const t0 = performance.now();
  try {
    const res = await fetch(getBaseUrl() + `/api/index?type=${type}&id=${id}`);
    const data = await res.json();
    const ms = Math.round(performance.now() - t0);
    showRawResult('search', data, !res.ok, ms);
  } catch (err) {
    showRawResult('search', `Index Search API Error: ${err.message}`, true, null);
  }
}

async function searchLocationsByName() {
  const query = document.getElementById('searchQuery').value.trim();
  const type = document.getElementById('searchType').value;

  const t0 = performance.now();
  try {
    const res = await fetch(getBaseUrl() + '/api/locations');
    if (!res.ok) {
      throw new Error(`HTTP ${res.status}`);
    }

    const raw = await res.json();
    const items = Array.isArray(raw) ? raw : (raw.locations ?? raw.results ?? raw.data ?? []);

    const filtered = items.filter((loc) => {
      const name = (loc.name ?? '').toString().toLowerCase();
      const locType = (loc.type ?? '').toString().toLowerCase();
      const matchesQuery = !query || name.includes(query.toLowerCase());
      const matchesType = !type || locType === type;
      return matchesQuery && matchesType;
    });

    const ms = Math.round(performance.now() - t0);
    const payload = filtered.length > 0 ? filtered : {
      status: 'no_results',
      query: query || '(empty)',
      type: type || 'any',
      matches: 0
    };

    showRawResult('search', payload, false, ms);
  } catch (err) {
    showRawResult('search', `Location Search API Error: ${err.message}`, true, null);
  }
}

// ----------------------------------------------------
// PAGE 6: SORTING DEMO & CHART.JS
// ----------------------------------------------------

let sortingChartInstance = null;

async function runSortAlgo(algorithm) {
  const size = document.getElementById('sortingInputSize').value || 1000;
  const t0 = performance.now();

  try {
    const res = await fetch(getBaseUrl() + `/api/sort?algorithm=${algorithm}&size=${size}`);
    const data = await res.json();
    const ms = Math.round(performance.now() - t0);

    showRawResult('sorting', data, !res.ok, ms);
    if (res.ok && data) {
      updateSortingChart([data]);
    }
  } catch (err) {
    showRawResult('sorting', `Sort API Error (${algorithm}): ${err.message}`, true, null);
  }
}

async function runAllSortAlgos() {
  const size = document.getElementById('sortingInputSize').value || 1000;
  const t0 = performance.now();

  try {
    const res = await fetch(getBaseUrl() + `/api/sort?algorithm=all&size=${size}`);
    const data = await res.json();
    const ms = Math.round(performance.now() - t0);

    showRawResult('sorting', data, !res.ok, ms);
    const items = Array.isArray(data) ? data : (data.results ?? []);
    if (items.length > 0) {
      updateSortingChart(items);
    }
  } catch (err) {
    showRawResult('sorting', `Run All Sort API Error: ${err.message}`, true, null);
  }
}

function updateSortingChart(results) {
  const ctx = document.getElementById('sortingChart')?.getContext('2d');
  if (!ctx) return;

  const labels = results.map(r => r.algorithm || r.name || 'Algorithm');
  const dataPoints = results.map(r => {
    const value = r.timeMs ?? r.timeNs ?? 0;
    const numeric = Number(value);
    return Number.isFinite(numeric) ? numeric : 0;
  });

  if (sortingChartInstance) {
    sortingChartInstance.destroy();
  }

  sortingChartInstance = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: labels,
      datasets: [{
        label: 'Execution Time (ms)',
        data: dataPoints,
        backgroundColor: ['#2563eb', '#10b981', '#f59e0b', '#ef4444'],
        borderRadius: 6
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        y: { beginAtZero: true, title: { display: true, text: 'Time (ms)' } }
      }
    }
  });
}

// ----------------------------------------------------
// PAGE 7: EFFICIENCY LAB & CHART.JS
// ----------------------------------------------------

let efficiencyChartInstance = null;

async function fetchEfficiencyExperiment(event) {
  if (event && typeof event.preventDefault === 'function') {
    event.preventDefault();
  }

  const exp = document.getElementById('efficiencyExperiment').value;
  const t0 = performance.now();

  try {
    const res = await fetch(getBaseUrl() + `/api/efficiency?experiment=${encodeURIComponent(exp)}`);
    const data = await res.json();
    const ms = Math.round(performance.now() - t0);

    showRawResult('efficiency', data, !res.ok, ms);
    const items = Array.isArray(data) ? data : (data.results ?? []);
    if (items.length > 0) {
      updateEfficiencyChart(items);
    }
  } catch (err) {
    showRawResult('efficiency', `Efficiency API Error (/api/efficiency?experiment=${exp}): ${err.message}`, true, null);
  }
}

async function runNewEfficiencyExperiment(event) {
  if (event && typeof event.preventDefault === 'function') {
    event.preventDefault();
  }

  const exp = document.getElementById('efficiencyExperiment').value;
  const t0 = performance.now();

  try {
    const res = await fetch(getBaseUrl() + `/api/efficiency/run?experiment=${encodeURIComponent(exp)}`, { method: 'POST' });
    const data = await res.json();
    const ms = Math.round(performance.now() - t0);

    showRawResult('efficiency', data, !res.ok, ms);
    const items = Array.isArray(data) ? data : (data.results ?? []);
    if (items.length > 0) {
      updateEfficiencyChart(items);
    }
  } catch (err) {
    showRawResult('efficiency', `Run Experiment API Error (/api/efficiency/run?experiment=${exp}): ${err.message}`, true, null);
  }
}

function updateEfficiencyChart(items) {
  const ctx = document.getElementById('efficiencyChart')?.getContext('2d');
  if (!ctx) return;

  const labels = items.map(i => i.algorithmName ?? i.algorithm ?? `N=${i.inputSize ?? i.size ?? '—'}`);
  const dataPoints = items.map(i => i.timeMs ?? (i.timeNs ? i.timeNs / 1000000 : 0));

  if (efficiencyChartInstance) {
    efficiencyChartInstance.destroy();
  }

  efficiencyChartInstance = new Chart(ctx, {
    type: 'line',
    data: {
      labels: labels,
      datasets: [{
        label: 'Runtime (ms)',
        data: dataPoints,
        borderColor: '#2563eb',
        backgroundColor: 'rgba(37, 99, 235, 0.1)',
        fill: true,
        tension: 0.3
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        y: { beginAtZero: true, title: { display: true, text: 'Time (ms)' } }
      }
    }
  });
}
