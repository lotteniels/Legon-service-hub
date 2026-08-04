
function getBaseUrl() {
  return document.getElementById('baseUrl').value.replace(/\/$/, '');
}


function switchTab(name) {
  document.querySelectorAll('.panel-section').forEach(s => s.classList.remove('active'));
  document.querySelectorAll('.nav-tab').forEach(t => {
    t.classList.remove('active');
    t.setAttribute('aria-selected', 'false');
  });
  document.getElementById('section-' + name).classList.add('active');
  const tab = document.getElementById('tab-' + name);
  tab.classList.add('active');
  tab.setAttribute('aria-selected', 'true');
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

  box.classList.add('visible');
  box.classList.toggle('error', !!isError);

  label.textContent = isError ? '⚠ Error' : '✓ Response';
  time.textContent  = elapsed != null ? elapsed + ' ms' : '';
  body.textContent  = typeof data === 'string' ? data : JSON.stringify(data, null, 2);
}


async function callApi(prefix, path, options) {
  const t0 = performance.now();
  showRawResult(prefix, 'Loading…', false, null);

  try {
    const res  = await fetch(getBaseUrl() + path, options);
    const data = await res.json();
    const ms   = Math.round(performance.now() - t0);

    if (!res.ok) {
      showRawResult(prefix, `Request failed (${res.status}):\n${JSON.stringify(data, null, 2)}`, true, ms);
      return null;
    }
    return { data, ms };
  } catch (err) {
    const ms = Math.round(performance.now() - t0);
    showRawResult(prefix, `Could not reach backend.\nIs ApiServer running on ${getBaseUrl()}?\n\n${err.message}`, true, ms);
    return null;
  }
}


async function checkHealth() {
  const dot   = document.getElementById('statusDot');
  const label = document.getElementById('statusLabel');
  dot.className   = 'status-dot';
  label.textContent = 'Checking…';

  try {
    
    const res = await fetch(getBaseUrl() + '/search/locations?query=Hall', { method: 'GET' });
    if (res.ok || res.status === 400) {
      dot.className   = 'status-dot online';
      label.textContent = 'Online';
    } else {
      dot.className   = 'status-dot offline';
      label.textContent = 'Error ' + res.status;
    }
  } catch {
    dot.className   = 'status-dot offline';
    label.textContent = 'Offline';
  }
}


window.addEventListener('load', () => {
  checkHealth();
});


async function findRoute() {
  const src    = Number(document.getElementById('routeSource').value);
  const dst    = Number(document.getElementById('routeDest').value);
  const crit   = document.getElementById('routeCriteria').value;

  if (!src || !dst) {
    showRawResult('route', 'Please enter both a source and destination location ID.', true, null);
    return;
  }
  if (src === dst) {
    showRawResult('route', 'Source and destination must be different locations.', true, null);
    return;
  }

  setLoading('btnRoute', true);
  const result = await callApi('route', '/routes/shortest-path', {
    method:  'POST',
    headers: { 'Content-Type': 'application/json' },
    body:    JSON.stringify({ sourceLocationId: src, destinationLocationId: dst, optimizationCriteria: crit })
  });
  setLoading('btnRoute', false);

  if (!result) return;
  renderRouteResult(result.data, result.ms);
}

function renderRouteResult(data, ms) {
  const box   = document.getElementById('routeResult');
  const label = document.getElementById('routeResultLabel');
  const time  = document.getElementById('routeResultTime');
  const body  = document.getElementById('routeResultBody');

  box.classList.add('visible');
  box.classList.remove('error');
  label.textContent = '✓ Route Found';
  time.textContent  = ms + ' ms';

  
  try {
    const dist    = data.totalDistanceMeters ?? data.distance ?? data.totalDistance ?? '—';
    const ttime   = data.estimatedTravelTimeMinutes ?? data.travelTime ?? data.time ?? '—';
    const path    = data.path ?? data.locations ?? data.route ?? [];
    const calcMs  = data.calculationTimeMs ?? data.computeTimeMs ?? '—';

    let html = '';

   
    html += `Distance: ${dist} m    `;
    html += `Est. Time: ${ttime} min    `;
    if (calcMs !== '---') html += `Algorithm time: ${calcMs} ms`;
    html += '\n\n';

    
    if (Array.isArray(path) && path.length > 0) {
      html += `Path (${path.length} stops):\n`;
      path.forEach((node, i) => {
        const name = typeof node === 'string' ? node
          : (node.name ?? node.locationName ?? node.id ?? JSON.stringify(node));
        const isFirst = i === 0;
        const isLast  = i === path.length - 1;
        const arrow   = isLast ? '' : '  ->  ';
        const prefix  = isFirst ? '[START] ' : (isLast ? '[ END ] ' : '        ');
        html += prefix + name + arrow;
        if ((i + 1) % 4 === 0 && !isLast) html += '\n        ';
      });
      html += '\n';
    }

    
    const known = new Set(['totalDistanceMeters','distance','totalDistance',
      'estimatedTravelTimeMinutes','travelTime','time','path','locations',
      'route','calculationTimeMs','computeTimeMs']);
    const extra = Object.fromEntries(Object.entries(data).filter(([k]) => !known.has(k)));
    if (Object.keys(extra).length) {
      html += '\n' + JSON.stringify(extra, null, 2);
    }

    body.innerHTML = '';
    body.textContent = html;
  } catch {
    body.textContent = JSON.stringify(data, null, 2);
  }
}


async function getPrioritizedRequests() {
  const limit  = Number(document.getElementById('reqLimit').value) || 10;
  const status = document.getElementById('reqStatus').value;

  setLoading('btnRequests', true);
  const result = await callApi('requests',
    `/requests/prioritized?limit=${limit}&status=${status}`,
    { method: 'GET' }
  );
  setLoading('btnRequests', false);

  if (!result) return;
  renderRequestsResult(result.data, result.ms);
}

function renderRequestsResult(data, ms) {
  const box   = document.getElementById('requestsResult');
  const label = document.getElementById('requestsResultLabel');
  const time  = document.getElementById('requestsResultTime');
  const body  = document.getElementById('requestsResultBody');

  box.classList.add('visible');
  box.classList.remove('error');

  
  const items  = Array.isArray(data) ? data : (data.requests ?? data.items ?? data.data ?? []);
  const total  = data.total ?? data.count ?? items.length;

  label.textContent = `✓ ${total} request${total !== 1 ? 's' : ''}`;
  time.textContent  = ms + ' ms';

  if (!items.length) {
    body.textContent = '(no requests returned)';
    return;
  }

  let out = '';
  items.forEach((r, i) => {
    const urgencyMark = r.urgency === 'high' ? '[HIGH]  ' : r.urgency === 'medium' ? '[MED]   ' : '[LOW]   ';
    const fine = r.fineAmountGHS && r.fineAmountGHS > 0 ? `  Fine: GHS ${r.fineAmountGHS}` : '';
    out += `${i + 1}. [#${r.requestId}] ${urgencyMark}${r.category ?? '---'}\n`;
    out += `   Status: ${r.status ?? '---'}  |  From: ${r.sourceLocationId ?? '---'} -> To: ${r.destinationLocationId ?? '---'}\n`;
    if (r.deadline) out += `   Deadline: ${formatDate(r.deadline)}${fine}\n`;
    out += '\n';
  });

  body.textContent = out.trimEnd();
}

 function runDispatch() {
  const maxDistanceMeters = Number(document.getElementById('dispatchMaxDist').value) || 1000;
  const onlyAvailable     = document.getElementById('dispatchOnlyAvailable').checked;

  setLoading('btnDispatch', true);
  const result = await callApi('dispatch', '/optimization/dispatch', {
    method:  'POST',
    headers: { 'Content-Type': 'application/json' },
    body:    JSON.stringify({ maxDistanceMeters, onlyAvailable })
  });
  setLoading('btnDispatch', false);

  if (!result) return;
  renderDispatchResult(result.data, result.ms);
}



function renderDispatchResult(data, ms) {
  const box   = document.getElementById('dispatchResult');
  const label = document.getElementById('dispatchResultLabel');
  const time  = document.getElementById('dispatchResultTime');
  const body  = document.getElementById('dispatchResultBody');

  box.classList.add('visible');
  box.classList.remove('error');

  const matches     = data.matches ?? data.assignments ?? data.results ?? (Array.isArray(data) ? data : []);
  const unmatched   = data.unmatchedRequests ?? data.unmatched ?? data.unmatchedCount ?? 0;
  const matchCount  = matches.length;

  label.textContent = `✓ ${matchCount} matched · ${unmatched} unmatched`;
  time.textContent  = ms + ' ms';

  if (!matchCount) {
    body.textContent = 'No matches found within the given constraints.\n\nUnmatched requests: ' + unmatched;
    return;
  }

  let out = '';
  matches.forEach((m, i) => {
    const reqId  = m.requestId ?? m.request?.requestId ?? '?';
    const resId  = m.resourceId ?? m.resource?.resourceId ?? '?';
    const resName= m.resourceName ?? m.resource?.name ?? '—';
    const dist   = m.distanceMeters ?? m.distance ?? '?';
    const cat    = m.category ?? m.request?.category ?? '';
    out += `${i + 1}. Request #${reqId}`;
    if (cat) out += ` (${cat})`;
    out += `\n   -> Resource #${resId}: ${resName}  |  Distance: ${dist} m\n\n`;
  });

  if (unmatched > 0) {
    out += `-----------------------------------------\n`;
    out += `Note: ${unmatched} request(s) could not be matched within the constraints.\n`;
  }

  body.textContent = out.trimEnd();
}


async function searchLocations() {
  const query = document.getElementById('searchQuery').value.trim();
  const type  = document.getElementById('searchType').value;

  const params = new URLSearchParams();
  if (query) params.set('query', query);
  if (type)  params.set('type',  type);

  setLoading('btnSearch', true);
  const result = await callApi('search', `/search/locations?${params.toString()}`, { method: 'GET' });
  setLoading('btnSearch', false);

  if (!result) return;
  renderSearchResult(result.data, result.ms);
}

function renderSearchResult(data, ms) {
  const box   = document.getElementById('searchResult');
  const label = document.getElementById('searchResultLabel');
  const time  = document.getElementById('searchResultTime');
  const body  = document.getElementById('searchResultBody');

  box.classList.add('visible');
  box.classList.remove('error');

  const items = Array.isArray(data) ? data : (data.locations ?? data.results ?? data.data ?? []);
  label.textContent = `✓ ${items.length} location${items.length !== 1 ? 's' : ''}`;
  time.textContent  = ms + ' ms';

  if (!items.length) {
    body.textContent = '(no locations found — try a different query or type)';
    return;
  }

 
  const typeColors = {
    lecture_hall:       '#3fb950',
    lab:                '#58a6ff',
    shuttle_stop:       '#e3b341',
    connector:          '#bc8cff',
    hostel:             '#f0883e',
    maintenance_office: '#f85149'
  };

  let out = '';
  items.forEach((loc, i) => {
    const dot   = typeColors[loc.type] ? '●' : '○';
    const type  = (loc.type ?? '—').replace(/_/g, ' ');
    out += `${i + 1}. [ID ${loc.id}] ${loc.name ?? '—'}\n`;
    out += `   ${dot} ${type}  |  Area: ${loc.area ?? '—'}  |  Grid: ${loc.coordinates ?? '—'}\n\n`;
  });

  body.textContent = out.trimEnd();
}


async function runBenchmark() {
  setLoading('btnBenchmark', true);
  const result = await callApi('benchmark', '/efficiency/benchmark', { method: 'GET' });
  setLoading('btnBenchmark', false);

  if (!result) return;
  renderBenchmarkResult(result.data, result.ms);
}

function renderBenchmarkResult(data, ms) {
  const box   = document.getElementById('benchmarkResult');
  const label = document.getElementById('benchmarkResultLabel');
  const time  = document.getElementById('benchmarkResultTime');
  const body  = document.getElementById('benchmarkResultBody');

  box.classList.add('visible');
  box.classList.remove('error');
  label.textContent = '✓ Benchmark Complete';
  time.textContent  = ms + ' ms';

 
  try {
    const structs  = data.structures ?? data.comparison ?? data.results ?? [];
    const dataSize = data.datasetSize ?? data.size ?? '—';

    let out = `Dataset Size: ${dataSize} entries\n`;
    out += '═'.repeat(50) + '\n\n';

    if (Array.isArray(structs) && structs.length) {
      structs.forEach(s => {
        const name   = s.name ?? s.structure ?? s.structureName ?? '—';
        const ins    = s.insertTimeMs ?? s.insertTime ?? s.insert ?? '—';
        const search = s.searchTimeMs ?? s.searchTime ?? s.search ?? '—';
        out += `Structure: ${name}\n`;
        out += `  Insert:  ${ins} ms\n`;
        out += `  Search:  ${search} ms\n\n`;
      });

      if (data.comparison && !Array.isArray(data.comparison)) {
        out += '─'.repeat(40) + '\n';
        out += JSON.stringify(data.comparison, null, 2);
      }
    } else {
      out += JSON.stringify(data, null, 2);
    }

    body.textContent = out;
  } catch {
    body.textContent = JSON.stringify(data, null, 2);
  }
}


function formatDate(iso) {
  try {
    return new Date(iso).toLocaleString('en-GB', {
      day: '2-digit', month: 'short', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
  } catch {
    return iso;
  }
}
